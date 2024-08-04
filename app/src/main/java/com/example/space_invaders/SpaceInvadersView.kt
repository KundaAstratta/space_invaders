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
    private val cosmicHorrors = mutableListOf<CosmicHorror>()
    private val bullets = mutableListOf<Bullet>()
    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private val numCosmicHorrorRows = 4 // 5 ou 3 lignes
    private val numCosmicHorrorCols = 5
    private val cosmicHorrorPadding = 10f
    private var cosmicHorrorWidth = 0f
    private var cosmicHorrorHeight = 0f

    private var speedMultiplier = 1f
    private var cosmicHorrorsDestroyed = 0

    private val explosions = mutableListOf<List<ExplosionParticle>>()

    private val duplicationInterval = 5000L // Intervalle de duplication en millisecondes (5 secondes)
    private var lastDuplicationTime = 0L // Temps de la dernière duplication

    private var level = 1

    init {
        // L'initialisation se fera dans onSizeChanged
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        screenWidth = w.toFloat()
        screenHeight = h.toFloat()

        // Calculer la taille des ennemis
        cosmicHorrorWidth = (screenWidth - (numCosmicHorrorCols + 1) * cosmicHorrorPadding) / numCosmicHorrorCols
        cosmicHorrorHeight = cosmicHorrorWidth / 2 // Ajustez le ratio selon vos préférences

        // Initialiser le joueur
        val playerSize = screenWidth / 10
        player = Player(screenWidth / 2, screenHeight - playerSize, playerSize)

        // Initialiser les ennemis
        val startX = (screenWidth - (numCosmicHorrorCols * (cosmicHorrorWidth + cosmicHorrorPadding) - cosmicHorrorPadding)) / 2
        val startY = screenHeight / 4

        for (row in 0 until numCosmicHorrorRows) {
            for (col in 0 until numCosmicHorrorCols) {
                val x = startX + col * (cosmicHorrorWidth + cosmicHorrorPadding)
                val y = startY + row * (cosmicHorrorHeight + cosmicHorrorPadding)
                cosmicHorrors.add(CosmicHorror(x, y, cosmicHorrorWidth, cosmicHorrorHeight, level))
            }
        }

    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.BLACK)

        player.draw(canvas, paint)
        cosmicHorrors.forEach { it.draw(canvas, paint) }
        bullets.forEach { it.draw(canvas, paint) }

        explosions.forEach { explosion ->
            explosion.forEach { it.draw(canvas, paint) }
        }

        update()
        invalidate()
    }

    private fun createExplosion(x: Float, y: Float, width: Float, height: Float) {
        val baseColor = Color.rgb(57, 255, 20)  // Vert phosphorescent de base
        val particleCount = 50
        val particles = List(particleCount) {
            val colorVariation = Random.nextInt(-20, 21)
            val particleColor = Color.rgb(
                (Color.red(baseColor) + colorVariation).coerceIn(0, 255),
                (Color.green(baseColor) + colorVariation).coerceIn(0, 255),
                (Color.blue(baseColor) + colorVariation).coerceIn(0, 255)
            )
            ExplosionParticle(
                x + Random.nextFloat() * width,
                y + Random.nextFloat() * height,
                20f,
                Random.nextFloat() * 10 - 5,
                Random.nextFloat() * 10 - 5,
                particleColor
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
                Color.YELLOW
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

        cosmicHorrors.forEach { cosmicHorror ->
            cosmicHorror.move(screenWidth, screenHeight, speedMultiplier)
            cosmicHorror.changeDirection(speedMultiplier)
        }

        if (cosmicHorrorsDestroyed > 0 && cosmicHorrorsDestroyed % 5 == 0) {
            speedMultiplier *= 1.1f // Augmentation de 10%
            cosmicHorrorsDestroyed++
        }

        val bulletsToRemove = mutableListOf<Bullet>()
        val cosmicHorrorToRemove = mutableListOf<CosmicHorror>()

        for (cosmicHorror in cosmicHorrors) {
            if (player.intersects(cosmicHorror)) {
                if (player.hit()) {
                    createPlayerExplosion()
                    checkGameOver()

                    // Gérer la fin du jeu ici
                }
                cosmicHorrors.remove(cosmicHorror)
                break
            }
        }

        for (cosmicHorror in cosmicHorrors) {
            for (bullet in bullets) {
                if (bullet.intersects(cosmicHorror)) {
                    if (cosmicHorror.hit()) {
                        createExplosion(cosmicHorror.x, cosmicHorror.y, cosmicHorror.width, cosmicHorror.height)
                        cosmicHorrorToRemove.add(cosmicHorror)
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        cosmicHorrors.removeAll { it in cosmicHorrorToRemove }
        bullets.removeAll { it in bulletsToRemove }

        // Duplication des ennemis
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDuplicationTime > duplicationInterval) {
            duplicateCosmicHorror()
            lastDuplicationTime = currentTime
        }

        explosions.forEach { explosion ->
            explosion.forEach { it.update() }
        }
        explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

        if (cosmicHorrors.isEmpty()) {
            startNextLevel()
        }

    }

    private fun startNextLevel() {
        level++
        speedMultiplier = 1f
        cosmicHorrorsDestroyed = 0

        // Réinitialiser les ennemis
        cosmicHorrors.clear()
        val startX = (screenWidth - (numCosmicHorrorCols * (cosmicHorrorWidth + cosmicHorrorPadding) - cosmicHorrorPadding)) / 2
        val startY = screenHeight / 4

        for (row in 0 until numCosmicHorrorRows) {
            for (col in 0 until numCosmicHorrorCols) {
                val x = startX + col * (cosmicHorrorWidth + cosmicHorrorPadding)
                val y = startY + row * (cosmicHorrorHeight + cosmicHorrorPadding)
                cosmicHorrors.add(CosmicHorror(x, y, cosmicHorrorWidth, cosmicHorrorHeight, level))
            }
        }
    }

    private fun duplicateCosmicHorror() {
        val cosmicHorrorsToDuplicate = cosmicHorrors.filter { Random.nextFloat() < 0.2f } // 20% de chance de duplication pour chaque ennemi

        for (cosmicHorror in cosmicHorrorsToDuplicate) {
            val newCosmicHorror = CosmicHorror(cosmicHorror.x, cosmicHorror.y, cosmicHorror.width, cosmicHorror.height, level)
            newCosmicHorror.hitsToDestroy = cosmicHorror.hitsToDestroy  // Copie les points de vie
            cosmicHorrors.add(newCosmicHorror)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE -> player.moveTo(event.x.coerceIn(0f, screenWidth))
            MotionEvent.ACTION_DOWN -> bullets.add(Bullet(player.x, player.y - player.size / 2))
        }
        return true
    }

    private fun removeCosmicHorror(cosmicHorror: CosmicHorror) {
        cosmicHorrors.remove(cosmicHorror)
        cosmicHorrorsDestroyed++
    }

}

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 3
    private var auraRadius = size / 1.5f
    private var auraGrowing = true
    private var auraGrowthSpeed = 1f // Ajustez la vitesse de pulsation

    fun draw(canvas: Canvas, paint: Paint) {
        when (lives) {
            3 -> {
                paint.color = Color.rgb(0, 255, 0)
                 auraGrowthSpeed =1f
            }// Vert pour 3 vies
            2 -> {
                paint.color = Color.rgb(255, 165, 0) // Orange pour 2 vies
                auraGrowthSpeed =2f
            }
            1 -> {
                paint.color = Color.rgb(255, 0, 0) // Rouge pour 1 vie
                auraGrowthSpeed =3f
            }

            else -> {
                Color.TRANSPARENT
            }
        }

        // Épaisseur du trait (ajustez la valeur selon vos préférences)
        paint.strokeWidth = 5f

        // Mise à jour de la taille du triangle
        val currentSize = size * (1 + (auraRadius - size / 1.5f) / (size * 0.5f)) // Calcul d'un facteur d'échelle basé sur l'aura

        // Calcul des coordonnées des sommets du triangle avec la nouvelle taille
        val halfSize = currentSize / 2
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

        // Mise à jour du rayon de l'aura
        if (auraGrowing) {
            auraRadius += auraGrowthSpeed
            if (auraRadius > size) {
                auraGrowing = false
            }
        } else {
            auraRadius -= auraGrowthSpeed
            if (auraRadius < size / 1.5f) {
                auraGrowing = true
            }
        }

        paint.style = Paint.Style.FILL // Rétablir le style de remplissage

    }

    fun moveTo(newX: Float) {
        x = newX
    }

    fun hit(): Boolean {
        lives--
        return lives <= 0
    }

    fun intersects(cosmicHorror: CosmicHorror): Boolean {
        return x + size / 2 > cosmicHorror.x && x - size / 2 < cosmicHorror.x + cosmicHorror.width &&
                y + size / 2 > cosmicHorror.y && y - size / 2 < cosmicHorror.y + cosmicHorror.height
    }


}

class CosmicHorror(var x: Float, var y: Float, val width: Float, val height: Float, level: Int) {
    var hitsToDestroy = 3 + level
    private var dx = Random.nextFloat() * 8 - 4 // Vitesse horizontale aléatoire entre -4 et 4
    private var dy = Random.nextFloat() * 8 - 4 // Vitesse verticale aléatoire entre -4 et 4

    private var rotationAngle = 0f
    private var rotationSpeed = 2f // Ajustez la vitesse de rotation selon vos préférences

    private val baseColor = when (level) {
        1 -> Color.RED
        2 -> Color.BLUE
        else -> Color.rgb(128,0,128)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (hitsToDestroy) {
            5 -> Color.argb(255, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            4 -> Color.argb(230, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            3 -> Color.argb(200, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            2 -> Color.argb(170, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
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
            x < -width -> {
                // Réapparition par la droite
                x = screenWidth
                y = Random.nextFloat() * (screenHeight * 0.7f) // Limite à 70% de la hauteur de l'écran
            }
            x > screenWidth -> {
                // Réapparition par la gauche
                x = -width
                y = Random.nextFloat() * (screenHeight * 0.7f) // Limite à 70% de la hauteur de l'écran
            }
            y < -height -> {
                // Réapparition par le bas de l'écran
                y = screenHeight * 0.7f // Réapparition à 70% de la hauteur de l'écran
                x = Random.nextFloat() * screenWidth
            }
            y > screenHeight -> {
                // Si l'ennemi sort par le bas, le faire réapparaître en haut
                y = -height
                x = Random.nextFloat() * screenWidth
            }
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
    private val speed = 15f
    private val boltLength = 150f
    private val boltWidth = 5f
    private val flickerSpeed = 5 // Contrôle la vitesse de scintillement

    //private val baseColor = Color.rgb(128, 0, 255) // Violet de base pour l'énergie occulte
    private  var baseColor = Color.YELLOW
    private var currentColor = baseColor
    private val random = Random

    fun draw(canvas: Canvas, paint: Paint) {
        // Faire scintiller la couleur
        if (random.nextInt(flickerSpeed) == 0) {
            currentColor = getFlickeringColor()
        }
        paint.color = currentColor
        paint.strokeWidth = boltWidth

        // Dessiner le corps principal de l'éclair avec un zigzag
        val zigzagWidth = 10f
        var lastX = x
        var lastY = y
        for (i in 0..5) {
            val newX = x + (if (i % 2 == 0) zigzagWidth else -zigzagWidth)
            val newY = y - (i + 1) * boltLength / 6
            canvas.drawLine(lastX, lastY, newX, newY, paint)
            lastX = newX
            lastY = newY
        }

    }

    private fun getFlickeringColor(): Int {
        val flickerIntensity = 50
        return Color.rgb(
            (Color.red(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255),
            (Color.green(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255),
            (Color.blue(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255)
        )
    }

    fun move() {
        y -= speed
    }

    fun intersects(cosmicHorror: CosmicHorror): Boolean {
        return x > cosmicHorror.x && x < cosmicHorror.x + cosmicHorror.width &&
                y > cosmicHorror.y && y < cosmicHorror.y + cosmicHorror.height
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