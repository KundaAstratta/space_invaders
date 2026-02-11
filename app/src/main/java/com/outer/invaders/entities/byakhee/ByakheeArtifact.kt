package com.outer.invaders.entities.byakhee

import android.graphics.Canvas
import android.graphics.Paint
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class ByakheeArtifact(
    var x: Float,
    var y: Float,
    private val width: Float,
    private val length: Float,
    private val color: Int
) {
    private val segments = 5
    private val segmentPoints = Array(segments) { FloatArray(2) }
    private val angleVariations = FloatArray(segments) { 0f }
    private val angleChangeRates = FloatArray(segments) { Random.nextFloat() * 0.2f - 0.1f }

    init {
        // Initial setup of tentacle segments
        for (i in 0 until segments) {
            segmentPoints[i][0] = x
            segmentPoints[i][1] = y + i * (length / segments)
        }
    }

    fun update() {
        // Update angle variations for each segment
        for (i in 0 until segments) {
            angleVariations[i] += angleChangeRates[i]

            // Limit angle variations
            angleVariations[i] = angleVariations[i] % (2 * PI.toFloat())
        }

        // Recalculate segment positions based on angles
        segmentPoints[0][0] = x
        segmentPoints[0][1] = y

        for (i in 1 until segments) {
            val angle = angleVariations[i-1]
            val segmentLength = length / segments

            segmentPoints[i][0] = segmentPoints[i-1][0] +
                    segmentLength * sin(angle)
            segmentPoints[i][1] = segmentPoints[i-1][1] +
                    segmentLength * cos(angle)
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = width
        paint.color = color

        // Draw each segment
        for (i in 1 until segments) {
            canvas.drawLine(
                segmentPoints[i-1][0],
                segmentPoints[i-1][1],
                segmentPoints[i][0],
                segmentPoints[i][1],
                paint
            )
        }
    }
}