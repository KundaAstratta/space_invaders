package com.outer.invaders.entities.elderthing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ElderProjectile(
    var x: Float,
    var y: Float,
    private val velocityX: Float,
    private val velocityY: Float,
    val radius: Float
) {
    fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(100, 255, 200)
        canvas.drawCircle(x, y, radius, paint)
    }

    fun move() {
        x += velocityX
        y += velocityY
    }
}