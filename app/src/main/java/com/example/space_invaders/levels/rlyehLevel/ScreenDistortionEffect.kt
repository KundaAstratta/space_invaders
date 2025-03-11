package com.example.space_invaders.levels.rlyehLevel

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.sin

class ScreenDistortionEffect(private val screenWidth: Float, private val screenHeight: Float) {
    private var isActive = false
    private var startTime = 0L
    private val duration = 3000 // Duration of the effect in milliseconds
    private val waveFrequency = 0.05f // Frequency of the wave
    private val waveAmplitude = 500f // Amplitude of the wave

    fun start() {
        isActive = true
        startTime = System.currentTimeMillis()
    }

    fun stop() {
        isActive = false
    }

    fun update() {
        if (isActive && System.currentTimeMillis() - startTime > duration) {
            isActive = false
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!isActive) return

        val currentTime = System.currentTimeMillis()
        val progress = (currentTime - startTime).toFloat() / duration

        val path = Path()
        var lastX = 0f
        var lastY = calculateWaveY(0f, progress)

        path.moveTo(lastX, lastY)

        for (x in 1..screenWidth.toInt()) {
            val xProgress = x / screenWidth
            val y = calculateWaveY(xProgress, progress)
            path.lineTo(x.toFloat(), y)
            lastX = x.toFloat()
            lastY = y
        }

        path.lineTo(screenWidth, screenHeight)
        path.lineTo(0f, screenHeight)
        path.close()

        canvas.drawPath(path, paint)
    }

    private fun calculateWaveY(xProgress: Float, timeProgress: Float): Float {
        val phase = timeProgress * 2 * Math.PI
        return (sin(xProgress * waveFrequency * 2 * Math.PI + phase) * waveAmplitude * (1 - timeProgress)).toFloat()
    }

    fun isActive(): Boolean = isActive
}
