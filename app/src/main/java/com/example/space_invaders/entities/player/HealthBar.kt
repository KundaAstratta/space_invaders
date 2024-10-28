package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class HealthBar(private val x: Float, private val y: Float, private val width: Float, private val height: Float) {
    private val maxLives = 10
    private val paint = Paint()
    private val backgroundPaint = Paint()

    init {
        backgroundPaint.color = Color.DKGRAY
    }

    fun draw(canvas: Canvas, lives: Int) {
        // Draw background
        canvas.drawRect(x, y, x + width, y + height, backgroundPaint)

        // Calculate fill width based on remaining lives
        val fillWidth = (lives.toFloat() / maxLives) * width

        // Set color based on remaining lives
        paint.color = when {
            lives >= 6 -> Color.GREEN
            lives in 3..5 -> Color.rgb(255, 165, 0) // Orange
            else -> Color.RED
        }

        // Draw filled portion
        canvas.drawRect(x, y, x + fillWidth, y + height, paint)
    }
}