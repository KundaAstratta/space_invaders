/*
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

 */
package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class HealthBar(private val x: Float, private val y: Float, private val width: Float, private val height: Float) {
    // MODIFIÉ : maxLives correspond maintenant à la vie du joueur
    private val maxLives = 200
    private val paint = Paint()
    private val backgroundPaint = Paint()

    init {
        backgroundPaint.color = Color.DKGRAY
    }

    fun draw(canvas: Canvas, lives: Int) {
        canvas.drawRect(x, y, x + width, y + height, backgroundPaint)

        val fillWidth = (lives.toFloat() / maxLives) * width

        // MODIFIÉ : Seuils ajustés pour une vie sur 200
        paint.color = when {
            lives >= 100 -> Color.GREEN
            lives in 50..99 -> Color.rgb(255, 165, 0) // Orange
            else -> Color.RED
        }

        canvas.drawRect(x, y, x + fillWidth, y + height, paint)
    }
}