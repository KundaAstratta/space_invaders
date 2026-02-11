package com.outer.invaders.entities.serpentman

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.outer.invaders.entities.player.Player
import kotlin.random.Random

class SerpentManProjectile(
    var x: Float,
    var y: Float,
    private val velocityX: Float,
    private val velocityY: Float,
    val radius: Float
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var rotation = 0f

    init {
        val gradient = RadialGradient(
            0f, 0f, radius,
            intArrayOf(Color.CYAN, Color.MAGENTA, Color.TRANSPARENT),
            floatArrayOf(0f, 0.6f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = gradient
    }

    fun move() {
        x += velocityX
        y += velocityY
        rotation += 10f // Effet de rotation
    }

    fun draw(canvas: Canvas) {
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(rotation)
        canvas.drawCircle(0f, 0f, radius, paint)
        canvas.restore()
    }

    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x < -radius || x > screenWidth + radius || y < -radius || y > screenHeight + radius
    }

    fun intersectsPlayer(player: Player): Boolean {
        val distance = kotlin.math.hypot((x - player.x).toDouble(), (y - player.y).toDouble()).toFloat()
        return distance < radius + player.size / 2
    }
}
