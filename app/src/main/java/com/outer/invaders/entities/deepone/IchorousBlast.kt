package com.outer.invaders.entities.deepone

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class IchorousBlast(
    x: Float,
    y: Float,
    val dx: Float,
    val dy: Float,
    val color: Int = Color.GREEN
) {
    var x = x
        private set
    var y = y
        private set
    val radius = 15f // Ajustez la taille du projectile

    fun move() {
        x += dx
        y += dy
    }

    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x < 0 || x > screenWidth || y < 0 || y > screenHeight
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = color
        canvas.drawCircle(x, y, radius, paint)
    }
}