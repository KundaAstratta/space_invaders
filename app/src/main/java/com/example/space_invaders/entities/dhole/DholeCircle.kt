package com.example.space_invaders.entities.dhole

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.entities.Player

class DholeCircle(var x: Float, var y: Float, val size: Float) {
    private var hits = 0
    private val maxHits = 3

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (hits) {
            0 -> Color.rgb(128, 0, 128) // Violet foncé
            1 -> Color.rgb(160, 32, 240) // Violet moyen
            else -> Color.rgb(192, 64, 255) // Violet clair
        }
        canvas.drawCircle(x, y, size / 2, paint)
    }

    fun hit(bulletX: Float, bulletY: Float): Boolean {
        if (hits < maxHits && kotlin.math.hypot(x - bulletX, y - bulletY) <= size / 2) {
            hits++
            return true
        }
        return false
    }

    fun isDestroyed(): Boolean = hits >= maxHits

    fun intersectsPlayer(player: Player): Boolean {
        return kotlin.math.hypot(x - player.x, y - player.y) <= (size / 2 + player.size / 2)
    }
}
