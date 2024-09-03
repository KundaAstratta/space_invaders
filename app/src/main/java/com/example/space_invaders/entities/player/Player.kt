package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.space_invaders.backgrounds.Background
import com.example.space_invaders.entities.ExplosionParticle
import com.example.space_invaders.entities.byakhee.Byakhee
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 3
    var isAlive = true
    private var rotation = 0f
    private val rotationSpeed = 0.1f
    private val textureSize = size / 4f
    private var lastX = x
    private val veins = mutableListOf<Vein>()
    private val numVeins = 15
    private var lifeLossAnimation: LifeLossAnimation? = null

    init {
        generateVeins()
    }

    private fun generateVeins() {
        for (i in 0 until numVeins) {
            veins.add(
                Vein(
                Random.nextFloat() * 2 * PI.toFloat(),
                Random.nextFloat() * (size / 2 - textureSize),
                Random.nextFloat() * (size / 8) + size / 16,
                Random.nextFloat() * 2 + 1
            )
            )
        }
    }

    data class Vein(
        val angle: Float,
        val distanceFromCenter: Float,
        val length: Float,
        val width: Float
    )

    fun draw(canvas: Canvas, paint: Paint) {
        // ... (code pour la couleur en fonction des vies inchangé)
        val baseColor = when (lives) {
            3 -> Color.rgb(0, 255, 255)  // Cyan
            2 -> Color.rgb(255, 255, 0)  // Jaune
            1 -> Color.rgb(255, 0, 255)  // Magenta
            else -> Color.TRANSPARENT
        }

        // Dessiner la soucoupe volante (corps principal)
        paint.color = Color.GRAY
        val saucerRadius = size / 2
        canvas.drawCircle(x, y, saucerRadius, paint)

        // Ajouter un effet 3D à la soucoupe
        val path = Path()
        path.addCircle(x, y, saucerRadius, Path.Direction.CW)
        paint.setShadowLayer(10f, 0f, 5f, Color.DKGRAY) // Ombre portée pour l'effet 3D
        canvas.drawPath(path, paint)
        paint.clearShadowLayer()

        // Ajouter des points noirs le long du bord
        paint.color = Color.WHITE
        val numPoints = 20 // Nombre de points à dessiner
        val angleIncrement = 2 * PI / numPoints
        val pointRadius = 3f // Rayon de chaque point
        for (i in 0 until numPoints) {
            val angle = i * angleIncrement
            val pointX = x + cos(angle) * saucerRadius
            val pointY = y + sin(angle) * saucerRadius
            canvas.drawCircle(pointX.toFloat(), pointY.toFloat(), pointRadius, paint)
        }

        // Dessiner le dôme central
        paint.color = Color.LTGRAY
        val domeRadius = saucerRadius / 3
        canvas.drawCircle(x, y, domeRadius, paint)

        // Dessiner l'oeil qui tourne au centre du dôme
        paint.color = baseColor
        val eyeRadius = textureSize / 2 + 10f
        val eyeX = x + cos(rotation) * (domeRadius - eyeRadius)
        val eyeY = y + sin(rotation) * (domeRadius - eyeRadius)
        canvas.drawCircle(eyeX, eyeY, eyeRadius, paint)

        // Dessiner la pupille de l'oeil
        paint.color = Color.BLACK
        val pupilRadius = eyeRadius / 3
        canvas.drawCircle(eyeX, eyeY, pupilRadius, paint)

        // Mise à jour de la rotation seulement si la soucoupe a bougé
        val movement = x - lastX
        if (abs(movement) > 0.1f) {
            rotation += movement * rotationSpeed
            if (rotation > 2 * Math.PI) {
                rotation -= 2 * Math.PI.toFloat()
            } else if (rotation < 0) {
                rotation += 2 * Math.PI.toFloat()
            }
        }
        lastX = x

        // Dessiner l'animation de perte de vie si elle existe
        lifeLossAnimation?.let { animation ->
            animation.draw(canvas, paint)
            animation.update()
            if (animation.isFinished()) {
                lifeLossAnimation = null
            }
        }

    }

    fun moveTo(newX: Float, newY: Float, screenWidth: Float, screenHeight: Float, structures: List<Background.Structure>) {
        //x = newX.coerceIn(0f, screenWidth)
        //y = newY.coerceIn(size / 2, screenHeight - size / 2)

        val potentialX = newX.coerceIn(size / 2, screenWidth - size / 2)
        val potentialY = newY.coerceIn(size / 2, screenHeight - size / 2)

        var canMove = true
        for (structure in structures) {
            if (structure.intersectsPlayerVersusStruct(potentialX, potentialY, size)) {
                canMove = false
                break
            }
        }

        if (canMove) {
            x = potentialX
            y = potentialY
        }
    }

    fun hit(): Boolean {
        lives--
        if (lives <= 0) {
            isAlive = false
        } else {
            // Créer une nouvelle animation de perte de vie
            lifeLossAnimation = LifeLossAnimation(x + size / 2, y - size / 2)
        }
        return !isAlive
    }

    fun intersects(byakhee: Byakhee): Boolean {
        val distance = kotlin.math.hypot(x - byakhee.x - byakhee.width / 2, y - byakhee.y - byakhee.height / 2)
        return distance < size / 2 + kotlin.math.min(byakhee.width, byakhee.height) / 2
    }

    // Créer l'explosion du joueur
    fun createPlayerExplosion(explosions: MutableList<List<ExplosionParticle>>) {
        val rippleCount = 5
        val baseColor = Color.YELLOW

        val particles = List(rippleCount) {
            ExplosionParticle(
                x,
                y,
                initialSize = 20f,
                maxSize = 200f,
                growthRate = Random.nextFloat() * 5 + 2,
                color = baseColor
            )
        }
        explosions.add(particles)
    }


}