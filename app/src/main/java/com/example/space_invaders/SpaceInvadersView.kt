package com.example.space_invaders
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random


class SpaceInvadersView(context: Context, private val onGameOver: () -> Unit) : View(context) {
    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private val numEnemyRows = 5
    private val numEnemyCols = 5
    private val enemyPadding = 10f
    private var enemyWidth = 0f
    private var enemyHeight = 0f

    private var speedMultiplier = 1f
    private var enemiesDestroyed = 0

    private val explosions = mutableListOf<List<ExplosionParticle>>()

    private val duplicationInterval = 5000L // Intervalle de duplication en millisecondes (5 secondes)
    private var lastDuplicationTime = 0L // Temps de la dernière duplication

    init {
        // L'initialisation se fera dans onSizeChanged
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        screenWidth = w.toFloat()
        screenHeight = h.toFloat()

        // Calculer la taille des ennemis
        enemyWidth = (screenWidth - (numEnemyCols + 1) * enemyPadding) / numEnemyCols
        enemyHeight = enemyWidth / 2 // Ajustez le ratio selon vos préférences

        // Initialiser le joueur
        val playerSize = screenWidth / 10
        player = Player(screenWidth / 2, screenHeight - playerSize, playerSize)

        // Initialiser les ennemis
        val startX = (screenWidth - (numEnemyCols * (enemyWidth + enemyPadding) - enemyPadding)) / 2
        val startY = screenHeight / 4

        for (row in 0 until numEnemyRows) {
            for (col in 0 until numEnemyCols) {
                val x = startX + col * (enemyWidth + enemyPadding)
                val y = startY + row * (enemyHeight + enemyPadding)
                enemies.add(Enemy(x, y, enemyWidth, enemyHeight))
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)

        player.draw(canvas, paint)
        enemies.forEach { it.draw(canvas, paint) }
        bullets.forEach { it.draw(canvas, paint) }

        explosions.forEach { explosion ->
            explosion.forEach { it.draw(canvas, paint) }
        }

        update()
        invalidate()
    }

    private fun createExplosion(x: Float, y: Float, width: Float, height: Float, color: Int) {
        val particleCount = 50
        val particles = List(particleCount) {
            ExplosionParticle(
                x + Random.nextFloat() * width,
                y + Random.nextFloat() * height,
                20f, // taille des particules
                Random.nextFloat() * 10 - 5, // vitesse x aléatoire
                Random.nextFloat() * 10 - 5 , // vitesse y aléatoire
                color
            )
        }
        explosions.add(particles)
    }

    private fun createPlayerExplosion() {
        val particleCount = 50
        val particles = List(particleCount) {
            ExplosionParticle(
                player.x + Random.nextFloat() * player.size - player.size / 2,
                player.y + Random.nextFloat() * player.size - player.size / 2,
                20f,
                Random.nextFloat() * 20 - 10,
                Random.nextFloat() * 20 - 10,
                Color.RED
            )
        }
        explosions.add(particles)
    }

    private fun checkGameOver() {
        if (player.lives <= 0) {
            // Attendez un peu pour que l'explosion soit visible
            postDelayed({
                onGameOver()
            }, 2000) // 2 secondes de délai
        }
    }

    private fun update() {
        bullets.forEach { it.move() }
        bullets.removeAll { it.y < 0 }

        enemies.forEach { enemy ->
            enemy.move(screenWidth, screenHeight, speedMultiplier)
            enemy.changeDirection(speedMultiplier)
        }

        if (enemiesDestroyed > 0 && enemiesDestroyed % 5 == 0) {
            speedMultiplier *= 1.1f // Augmentation de 10%
            enemiesDestroyed++
        }

        val bulletsToRemove = mutableListOf<Bullet>()
        val enemiesToRemove = mutableListOf<Enemy>()

        for (enemy in enemies) {
            if (player.intersects(enemy)) {
                if (player.hit()) {
                    createPlayerExplosion()
                    checkGameOver()

                    // Gérer la fin du jeu ici
                }
                enemies.remove(enemy)
                break
            }
        }

        for (enemy in enemies) {
            for (bullet in bullets) {
                if (bullet.intersects(enemy)) {
                    if (enemy.hit()) {
                        createExplosion(enemy.x, enemy.y, enemy.width, enemy.height, Color.WHITE)
                        enemiesToRemove.add(enemy)
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        enemies.removeAll { it in enemiesToRemove }
        bullets.removeAll { it in bulletsToRemove }

        // Duplication des ennemis
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDuplicationTime > duplicationInterval) {
            duplicateEnemies()
            lastDuplicationTime = currentTime
        }

        explosions.forEach { explosion ->
            explosion.forEach { it.update() }
        }
        explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

    }

    private fun duplicateEnemies() {
        val enemiesToDuplicate = enemies.filter { Random.nextFloat() < 0.2f } // 20% de chance de duplication pour chaque ennemi

        for (enemy in enemiesToDuplicate) {
            val newEnemy = Enemy(enemy.x, enemy.y, enemy.width, enemy.height)
            newEnemy.hitsToDestroy = enemy.hitsToDestroy  // Copie les points de vie
            enemies.add(newEnemy)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> player.moveTo(event.x.coerceIn(0f, screenWidth))
            MotionEvent.ACTION_DOWN -> bullets.add(Bullet(player.x, player.y - player.size / 2))
        }
        return true
    }


    private fun removeEnemy(enemy: Enemy) {
        enemies.remove(enemy)
        enemiesDestroyed++
    }



}

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 3

    fun draw(canvas: Canvas, paint: Paint) {

        paint.color = when (lives) {
            3 -> Color.GREEN
            2 -> Color.rgb(255, 165, 0) // Orange
            1 -> Color.rgb(252, 14, 14)
            else -> Color.TRANSPARENT
        }

        //canvas.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, paint)
        // Épaisseur du trait (ajustez la valeur selon vos préférences)
        paint.strokeWidth = 5f

        // Calcul des coordonnées des sommets du triangle
        val halfSize = size / 2
        val topX = x
        val topY = y - halfSize
        val bottomLeftX = x - halfSize
        val bottomLeftY = y + halfSize
        val bottomRightX = x + halfSize
        val bottomRightY = y + halfSize

        // Dessin du triangle
        canvas.drawLine(topX, topY, bottomLeftX, bottomLeftY, paint)
        canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY, paint)
        canvas.drawLine(bottomRightX, bottomRightY, topX, topY, paint)
    }

    fun moveTo(newX: Float) {
        x = newX
    }

    fun hit(): Boolean {
        lives--
        return lives <= 0
    }

    fun intersects(enemy: Enemy): Boolean {
        return x + size / 2 > enemy.x && x - size / 2 < enemy.x + enemy.width &&
                y + size / 2 > enemy.y && y - size / 2 < enemy.y + enemy.height
    }


}

class Enemy(var x: Float, var y: Float, val width: Float, val height: Float) {
    var hitsToDestroy = 3
    private var dx = Random.nextFloat() * 8 - 4 // Vitesse horizontale aléatoire entre -4 et 4
    private var dy = Random.nextFloat() * 8 - 4 // Vitesse verticale aléatoire entre -4 et 4

    private var rotationAngle = 0f
    private var rotationSpeed = 2f // Ajustez la vitesse de rotation selon vos préférences

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (hitsToDestroy) {
            3 -> Color.rgb(252, 14, 14) // Rouge vif (255, 0, 0)
            2 -> Color.rgb(255, 165, 0) // Orange
            1 -> Color.WHITE
            else -> Color.TRANSPARENT
        }

        // Épaisseur du trait (ajustez la valeur selon vos préférences)
        paint.strokeWidth = 5f

        canvas.save() // Sauvegarder l'état du canvas

        // Rotation autour du centre du triangle
        canvas.rotate(rotationAngle, x + width / 2, y + height / 2)

        // Calculer les coordonnées des sommets du triangle après rotation
        val halfWidth = width / 2
        val topX = x + halfWidth
        val topY = y
        val bottomLeftX = x
        val bottomLeftY = y + height
        val bottomRightX = x + width
        val bottomRightY = y + height

        // Dessiner le triangle en utilisant drawLine()
        canvas.drawLine(topX, topY, bottomLeftX, bottomLeftY, paint)
        canvas.drawLine(bottomLeftX, bottomLeftY, bottomRightX, bottomRightY, paint)
        canvas.drawLine(bottomRightX, bottomRightY, topX, topY, paint)

        canvas.restore() // Restaurer l'état du canvas

        // Mise à jour de l'angle de rotation
        rotationAngle = (rotationAngle + rotationSpeed) % 360
    }

    fun hit(): Boolean {
        hitsToDestroy--
        return hitsToDestroy <= 0
    }

    fun move(screenWidth: Float, screenHeight: Float, speedMultiplier: Float) {
        x += dx * speedMultiplier
        y += dy * speedMultiplier

        // Gestion de la sortie d'écran
        when {
            x < -width -> x = screenWidth + Random.nextFloat() * screenWidth
            x > screenWidth -> x = -width + Random.nextFloat() * screenWidth
            y < -height -> y = screenHeight + Random.nextFloat() * screenHeight
            y > screenHeight -> y = -height + Random.nextFloat() * screenHeight
        }
    }

    fun changeDirection(speedMultiplier: Float) {
        if (Random.nextFloat() < 0.02) { // 2% de chance de changer de direction à chaque frame
            dx = (Random.nextFloat() * 8 - 4) * speedMultiplier
            dy = (Random.nextFloat() * 8 - 4) * speedMultiplier
        }
    }
}

class Bullet(var x: Float, var y: Float) {
    private val radius = 5f
    private val speed = 10f

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, radius, paint)
    }

    fun move() {
        y -= speed
    }

    fun intersects(enemy: Enemy): Boolean {
        return x > enemy.x && x < enemy.x + enemy.width &&
                y > enemy.y && y < enemy.y + enemy.height
    }
}

class ExplosionParticle(
    var x: Float,
    var y: Float,
    private val size: Float,
    private val dx: Float,
    private val dy: Float,
    private val color: Int

) {
    var alpha = 255

    fun update(): Boolean {
        x += dx
        y += dy
        alpha -= 2  // Réduisez cette valeur pour une disparition plus lente
        return alpha > 0
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        canvas.drawRect(x - size/2, y - size/2, x + size/2, y + size/2, paint)
    }

    fun isAlive(): Boolean = alpha > 0
}