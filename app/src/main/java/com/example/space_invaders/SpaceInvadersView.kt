package com.example.space_invaders
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random


class SpaceInvadersView(context: Context) : View(context) {
    private lateinit var player: Player
    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private val numEnemyRows = 3
    private val numEnemyCols = 5
    private val enemyPadding = 10f
    private var enemyWidth = 0f
    private var enemyHeight = 0f

    private var speedMultiplier = 1f
    private var enemiesDestroyed = 0

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
        val startY = screenHeight / 8

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

        update()
        invalidate()
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
            for (bullet in bullets) {
                if (bullet.intersects(enemy)) {
                    if (enemy.hit()) {
                        enemiesToRemove.add(enemy)
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        enemies.removeAll { it in enemiesToRemove }
        bullets.removeAll { it in bulletsToRemove }
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
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.GREEN
        canvas.drawRect(x - size / 2, y - size / 2, x + size / 2, y + size / 2, paint)
    }

    fun moveTo(newX: Float) {
        x = newX
    }
}

class Enemy(var x: Float, var y: Float, val width: Float, val height: Float) {
    var hitsToDestroy = 3
    private var dx = Random.nextFloat() * 8 - 4 // Vitesse horizontale aléatoire entre -4 et 4
    private var dy = Random.nextFloat() * 8 - 4 // Vitesse verticale aléatoire entre -4 et 4

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (hitsToDestroy) {
            3 -> Color.RED
            2 -> Color.rgb(255, 165, 0) // Orange
            1 -> Color.WHITE
            else -> Color.TRANSPARENT
        }
        canvas.drawRect(x, y, x + width, y + height, paint)
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