package com.example.space_invaders.entities.cthulhu

import android.graphics.*
import kotlin.math.*


class Tentacle(
    var startX: Float,
    var startY: Float,
    val length: Float,
    val direction: Float
) {
    var health = 50
    private val segments = 30
    private val controlPoints = mutableListOf<PointF>()
    private val amplitude = 80f
    //private val frequency = 0.05f
    private var phase = 0f
    private val maxThickness = 40f
    private val minThickness = 10f

    init {
        generateControlPoints()
    }

    private fun generateControlPoints() {
        controlPoints.clear()
        for (i in 0..segments) {
            val t = i.toFloat() / segments
            val x = startX + t * length * cos(direction)
            val y = startY + t * length * sin(direction)
            val offset = sin((t * 2 * PI + phase).toFloat()) * amplitude * (1 - t)

            val perpX = sin(direction).toFloat()
            val perpY = -cos(direction).toFloat()

            controlPoints.add(PointF(x + offset * perpX, y + offset * perpY))
        }
    }

    fun update() {
        phase += 0.1f
        generateControlPoints()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        val path = Path()
        path.moveTo(controlPoints[0].x, controlPoints[0].y)

        for (i in 1 until controlPoints.size) {
            val startPoint = controlPoints[i - 1]
            val endPoint = controlPoints[i]
            val midPoint = PointF((startPoint.x + endPoint.x) / 2, (startPoint.y + endPoint.y) / 2)

            path.quadTo(startPoint.x, startPoint.y, midPoint.x, midPoint.y)
        }

        // Create gradient for more realistic coloring
        val gradient = LinearGradient(
            startX, startY, controlPoints.last().x, controlPoints.last().y,
            intArrayOf(Color.rgb(0, 100, 0), Color.rgb(0, 200, 0), Color.rgb(150, 255, 150)),
            null, Shader.TileMode.CLAMP
        )

        paint.shader = gradient
        paint.style = Paint.Style.STROKE
        paint.strokeCap = Paint.Cap.ROUND

        // Draw with varying thickness
        for (i in 0 until controlPoints.size - 1) {
            val t = i.toFloat() / (controlPoints.size - 1)
            val thickness = maxThickness * (1 - t) + minThickness * t
            paint.strokeWidth = thickness

            val start = controlPoints[i]
            val end = controlPoints[i + 1]
            canvas.drawLine(start.x, start.y, end.x, end.y, paint)
        }

        // Reset shader for other drawings
        paint.shader = null
    }

    fun intersectsPlayer(playerX: Float, playerY: Float, playerRadius: Float): Boolean {
        for (point in controlPoints) {
            val distance = sqrt((point.x - playerX).pow(2) + (point.y - playerY).pow(2))
            if (distance < playerRadius + maxThickness / 2) {
                return true
            }
        }
        return false
    }

    fun hit(): Boolean {
        health--
        return health <= 0
    }
}

