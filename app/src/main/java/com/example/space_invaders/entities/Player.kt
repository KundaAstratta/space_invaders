package com.example.space_invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.backgrounds.Background
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

    init {
        generateVeins()
    }

    private fun generateVeins() {
        for (i in 0 until numVeins) {
            veins.add(Vein(
                Random.nextFloat() * 2 * PI.toFloat(),
                Random.nextFloat() * (size / 2 - textureSize),
                Random.nextFloat() * (size / 8) + size / 16,
                Random.nextFloat() * 2 + 1
            ))
        }
    }

    data class Vein(
        val angle: Float,
        val distanceFromCenter: Float,
        val length: Float,
        val width: Float
    )

    fun draw(canvas: Canvas, paint: Paint) {
        // Couleur de la boule en fonction des vies restantes
        val baseColor = when (lives) {
            3 -> Color.rgb(0, 255, 255)  // Cyan
            2 -> Color.rgb(255, 255, 0)  // Jaune
            1 -> Color.rgb(255, 0, 255)  // Magenta
            else -> Color.TRANSPARENT
        }

        // Dessiner la boule principale (l'oeil)
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, size / 2, paint)

        // Dessiner les veines
        paint.color = Color.RED
        for (vein in veins) {
            val startX = x + cos(vein.angle) * vein.distanceFromCenter
            val startY = y + sin(vein.angle) * vein.distanceFromCenter
            val endX = startX + cos(vein.angle) * vein.length
            val endY = startY + sin(vein.angle) * vein.length
            paint.strokeWidth = vein.width
            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Dessiner la boule  (l'iris) AVANT la boule noire
        paint.color = baseColor
        val eyeRadius = textureSize / 2 + 10f // Légèrement plus grand que la boule noire
        val eyeX = x + cos(rotation) * (size / 2 - eyeRadius)
        val eyeY = y + sin(rotation) * (size / 2 - eyeRadius)
        canvas.drawCircle(eyeX, eyeY, eyeRadius, paint)

        // Dessiner la texture (petit cercle noir)
        paint.color = Color.BLACK
        val textureX = x + cos(rotation) * (size / 2 - textureSize / 2)
        val textureY = y + sin(rotation) * (size / 2 - textureSize / 2)
        canvas.drawCircle(textureX, textureY, textureSize / 2, paint)

        // Mise à jour de la rotation seulement si la boule a bougé
        val movement = x - lastX
        if (abs(movement) > 0.1f) {  // Seuil pour éviter les micro-mouvements
            rotation += movement * rotationSpeed
            if (rotation > 2 * Math.PI) {
                rotation -= 2 * Math.PI.toFloat()
            } else if (rotation < 0) {
                rotation += 2 * Math.PI.toFloat()
            }
        }
        lastX = x
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
        }
        return !isAlive
    }

    fun intersects(byakhee: Byakhee): Boolean {
        val distance = kotlin.math.hypot(x - byakhee.x - byakhee.width / 2, y - byakhee.y - byakhee.height / 2)
        return distance < size / 2 + kotlin.math.min(byakhee.width, byakhee.height) / 2
    }
}
