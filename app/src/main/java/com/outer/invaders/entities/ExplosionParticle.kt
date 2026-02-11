package com.outer.invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ExplosionParticle(
    private var x: Float,
    private var y: Float,
    private var initialSize: Float,
    private var maxSize: Float,
    private val growthRate: Float,
    private val color: Int
) {
    private var currentSize = initialSize
    private var alpha = 255

    fun update(): Boolean {
        currentSize += growthRate
        alpha -= 5

        return currentSize < maxSize && alpha > 0
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        canvas.drawCircle(x, y, currentSize / 2, paint)

        // Reset paint properties
        paint.reset()
    }

    fun isAlive(): Boolean = currentSize < maxSize && alpha > 0
}