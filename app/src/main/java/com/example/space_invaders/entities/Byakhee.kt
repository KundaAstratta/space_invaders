package com.example.space_invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.example.space_invaders.backgrounds.Background
import kotlin.random.Random

class Byakhee(var x: Float, var y: Float, val width: Float, val height: Float, level: Int) {
    var hitsToDestroy = 3 + level
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

    init {
        // ... autre code d'initialisation ...
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

    fun draw(canvas: Canvas, paint: Paint) {
        updateGradient()
        paint.shader = gradient
        paint.style = Paint.Style.FILL

        canvas.save()

        canvas.rotate(rotationAngle, x + width / 2, y + height / 2)

        val path = Path()
        path.moveTo(x + width / 2, y)
        path.lineTo(x, y + height)
        path.lineTo(x + width, y + height)
        path.close()

        canvas.drawPath(path, paint)

        canvas.restore()

        rotationAngle = (rotationAngle + rotationSpeed) % 360

        paint.shader = null  // Réinitialiser le shader pour ne pas affecter d'autres dessins
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
