package com.example.space_invaders.entities.byakhee

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.example.space_invaders.backgrounds.Background
import kotlin.math.sin
import kotlin.random.Random

class Byakhee(var x: Float, var y: Float, val width: Float, val height: Float, level: Int) {
    var hitsToDestroy = 5//3 + level
    private var dx = Random.nextFloat() * 8 - 4 // Vitesse horizontale aléatoire entre -4 et 4
    private var dy = Random.nextFloat() * 8 - 4 // Vitesse verticale aléatoire entre -4 et 4

    private var rotationAngle = 0f
    private var rotationSpeed = 2f // Ajustez la vitesse de rotation selon vos préférences

    private lateinit var gradient: LinearGradient

    private val baseColor = when (level) {
        1 -> Color.RED
        2 -> Color.BLUE
        else -> Color.rgb(128,0,128)
    }

    private val eye = ByakheeEye(x + width / 2, y + height / 3, width / 8)

    private val numTrianglesPerWing = 7  // Nombre de triangles par aile

    private val triangleOffsets = Array(numTrianglesPerWing * 2) {
        TriangleOffset(
            offsetX = 0f,
            offsetY = 0f,
            speedX = Random.nextFloat() * 15 - 2,
            speedY = Random.nextFloat() * 15 - 2,
            amplitude = Random.nextFloat() * 20 + 5 // Amplitude
        )
    }


    // New tentacles collection
    private val tentacles = List(5) {
        ByakheeArtifact(
            x + width / 2,
            y + height,
            width / 10,
            height / 3,
            // Different shades of green
            when(it) {
                0 -> Color.rgb(0, 128, 0)    // Dark green
                1 -> Color.rgb(34, 139, 34)  // Forest green
                2 -> Color.rgb(50, 205, 50)  // Lime green
                3 -> Color.rgb(144, 238, 144) // Light green
                else -> Color.rgb(0, 255, 0)  // Bright green
            }
        )
    }

    init {
        updateGradient()
    }

    private fun updateGradient() {
        val startColor = when (hitsToDestroy) {
            5 -> Color.argb(255, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            4 -> Color.argb(230, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            3 -> Color.argb(200, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            2 -> Color.argb(170, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            1 -> Color.WHITE
            else -> Color.TRANSPARENT
        }
        val endColor = Color.argb(
            Color.alpha(startColor),
            (Color.red(startColor) * 0.7f).toInt(),
            (Color.green(startColor) * 0.7f).toInt(),
            (Color.blue(startColor) * 0.7f).toInt()
        )
        gradient = LinearGradient(
            x, y, x + width, y + height,
            startColor, endColor, Shader.TileMode.CLAMP
        )
    }

    private data class TriangleOffset(
        var offsetX: Float,
        var offsetY: Float,
        val speedX: Float,
        val speedY: Float,
        val amplitude: Float
    ) {
        fun update() {
            offsetX = (offsetX + speedX) % (Math.PI * 2).toFloat()
            offsetY = (offsetY + speedY) % (Math.PI * 2).toFloat()
        }

        fun getCurrentOffset(): Pair<Float, Float> {
            return Pair(
                sin(offsetX) * amplitude,
                sin(offsetY) * amplitude
            )
        }
    }

    private fun updateTriangleOffsets() {
        triangleOffsets.forEach { it.update() }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        updateGradient()
        updateTriangleOffsets()

        paint.shader = gradient
        paint.style = Paint.Style.FILL

        canvas.save()
        canvas.rotate(rotationAngle, x + width / 2, y + height / 2)

        val path = Path()
        val centerX = x + width / 2
        val centerY = y + height / 2
        val wingSpan = width / 2
        val maxHeight = height / 2

        // Aile gauche
        for (i in 0 until numTrianglesPerWing) {
            val offset = triangleOffsets[i].getCurrentOffset()
            val angle = Math.PI * i / (numTrianglesPerWing - 1)
            val triangleHeight = maxHeight * (1 - 0.2f * i / numTrianglesPerWing)

            val baseX = centerX - 10 + offset.first
            val baseY = centerY + offset.second

            val tipX = centerX - wingSpan * Math.cos(angle).toFloat() + offset.first
            val tipY = centerY - wingSpan * Math.sin(angle).toFloat() + offset.second

            path.moveTo(baseX, baseY)
            path.lineTo(tipX, tipY)
            path.lineTo(
                tipX + offset.first,
                tipY + triangleHeight + offset.second
            )
            path.close()
        }

        // Aile droite
        for (i in 0 until numTrianglesPerWing) {
            val offset = triangleOffsets[i + numTrianglesPerWing].getCurrentOffset()
            val angle = Math.PI * i / (numTrianglesPerWing - 1)
            val triangleHeight = maxHeight * (1 - 0.2f * i / numTrianglesPerWing)

            val baseX = centerX + 10 + offset.first
            val baseY = centerY + offset.second

            val tipX = centerX + wingSpan * Math.cos(angle).toFloat() + offset.first
            val tipY = centerY - wingSpan * Math.sin(angle).toFloat() + offset.second

            path.moveTo(baseX, baseY)
            path.lineTo(tipX, tipY)
            path.lineTo(
                tipX + offset.first,
                tipY + triangleHeight + offset.second
            )
            path.close()
        }

        // Premier passage : dessiner l'ombre
        paint.shader = null  // Désactiver le gradient pour l'ombre
        paint.style = Paint.Style.FILL
        paint.setShadowLayer(
            30f,  // Rayon du flou réduit pour plus de netteté
            5f,   // Décalage X plus subtil
            5f,   // Décalage Y plus subtil
            Color.argb(150, 0, 255, 0)  // Vert plus opaque
        )
        canvas.drawPath(path, paint)

        // Second passage : dessiner le Byakhee avec son gradient
        paint.clearShadowLayer()
        paint.shader = gradient
        canvas.drawPath(path, paint)

        canvas.restore()

        // Réinitialiser pour les autres éléments
        paint.shader = null
        paint.clearShadowLayer()

        // Dessiner l'œil avec son propre effet de lueur
        eye.x = x + width / 2
        eye.y = y + height / 2

        // Ajouter une lueur à l'œil
        paint.setShadowLayer(
            15f,  // Rayon du flou pour l'œil
            0f,   // Pas de décalage X
            0f,   // Pas de décalage Y
            Color.argb(180, 0, 255, 0)  // Lueur verte intense
        )
        eye.draw(canvas, paint)
        paint.clearShadowLayer()

        // Dessiner les tentacules avec leur propre effet de lueur
        paint.setShadowLayer(
            20f,  // Rayon du flou pour les tentacules
            2f,   // Léger décalage X
            2f,   // Léger décalage Y
            Color.argb(130, 0, 255, 0)  // Lueur verte semi-transparente
        )
        tentacles.forEach { tentacle ->
            tentacle.x = x + width / 2
            tentacle.y = y + height / 2 + eye.radius
            tentacle.update()
            tentacle.draw(canvas, paint)
        }

        rotationAngle = (rotationAngle + rotationSpeed) % 360
    }
    fun hit(): Boolean {
        hitsToDestroy--
        updateGradient()  // Mettre à jour le dégradé après chaque coup
        return hitsToDestroy <= 0
    }

    fun move(screenWidth: Float, screenHeight: Float, speedMultiplier: Float, structures: List<Background.Structure>) {
        val newX = x + dx * speedMultiplier
        val newY = y + dy * speedMultiplier

        x = newX
        y = newY

        // Gestion de la sortie d'écran
        when {
            x < -width -> {
                x = screenWidth
                y = Random.nextFloat() * (screenHeight * 0.7f)
            }
            x > screenWidth -> {
                x = -width
                y = Random.nextFloat() * (screenHeight * 0.7f)
            }
            y < -height -> {
                y = screenHeight * 0.7f
                x = Random.nextFloat() * screenWidth
            }
            y > screenHeight -> {
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
