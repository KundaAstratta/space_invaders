package com.example.space_invaders.entities.dhole

import android.graphics.Canvas
import android.graphics.Paint
import com.example.space_invaders.entities.player.Player
import kotlin.math.sin
import kotlin.random.Random

class Dhole(var x: Float, var y: Float, val circleSize: Float) {
    private val numCircles = 5//REAL 15
    private val circles = mutableListOf<DholeCircle>()
    private var time = 0f
    private var invincibilityFrames = 0

    // Paramètres de mouvement omnidirectionnel
    private var horizontalAmplitude = 500f
    private var verticalAmplitude = 500f
    private var horizontalSpeed = 0.02f
    private var verticalSpeed = 0.02f
    private var horizontalTime = 0f
    private var verticalTime = 0f
    private var initialX = x
    private var initialY = y

    // Paramètres de dérive
    private var driftX = 0f
    private var driftY = 0f
    private val driftSpeed = 0.5f
    private var driftAngle = Random.nextFloat() * 2 * Math.PI.toFloat()
    private val driftChangeChance = 0.01f

    init {
        for (i in 0 until numCircles) {
            circles.add(DholeCircle(x, y + i * circleSize * 0.75f, circleSize))
        }
    }

    fun setInvincibilityFrame() {
        invincibilityFrames = 30
    }

    fun draw(canvas: Canvas, paint: Paint) {
        circles.forEach { it.draw(canvas, paint) }
    }

    fun move(screenWidth: Float, screenHeight: Float) {
        // Mouvement oscillant horizontal et vertical
        horizontalTime += horizontalSpeed
        verticalTime += verticalSpeed

        // Mise à jour de la dérive aléatoire
        if (Random.nextFloat() < driftChangeChance) {
            driftAngle = Random.nextFloat() * 2 * Math.PI.toFloat()
        }

        // Calcul de la dérive
        driftX += kotlin.math.cos(driftAngle) * driftSpeed
        driftY += kotlin.math.sin(driftAngle) * driftSpeed

        // Position finale combinant oscillation et dérive
        x = initialX + sin(horizontalTime) * horizontalAmplitude + driftX
        y = initialY + sin(verticalTime) * verticalAmplitude + driftY

        // Gestion des bords de l'écran
        when {
            x < 0 -> {
                x = 0f
                initialX = x
                driftX = 0f
                driftAngle = Random.nextFloat() * Math.PI.toFloat()
            }
            x > screenWidth -> {
                x = screenWidth
                initialX = x
                driftX = 0f
                driftAngle = Math.PI.toFloat() + Random.nextFloat() * Math.PI.toFloat()
            }
        }

        when {
            y < 0 -> {
                y = 0f
                initialY = y
                driftY = 0f
                driftAngle = Math.PI.toFloat() / 2 + Random.nextFloat() * Math.PI.toFloat()
            }
            y > screenHeight / 2 -> {
                y = screenHeight / 2
                initialY = y
                driftY = 0f
                driftAngle = -Math.PI.toFloat() / 2 + Random.nextFloat() * Math.PI.toFloat()
            }
        }

        if (invincibilityFrames > 0) {
            invincibilityFrames--
        }

        // Mise à jour du temps pour l'ondulation
        time += 0.05f

        // Mise à jour de la position des cercles avec ondulation
        updateCirclePositions()
    }

    private fun updateCirclePositions() {
        val waveAmplitude = circleSize * 2
        val waveFrequency = 0.3f

        for (i in 0 until numCircles) {
            val offsetX = waveAmplitude * sin((time + i * waveFrequency) * 2)
            val offsetY = waveAmplitude * sin((time + i * waveFrequency) * 3) / 2

            circles[i].x = x + offsetX
            circles[i].y = y + i * circleSize * 0.75f + offsetY
        }
    }

    fun hit(bulletX: Float, bulletY: Float): Boolean {
        for (circle in circles) {
            if (!circle.isDestroyed() && circle.hit(bulletX, bulletY)) {
                return true
            }
        }
        return false
    }

    fun isDestroyed(): Boolean = circles.all { it.isDestroyed() }

    fun intersectsPlayer(player: Player): Boolean {
        return invincibilityFrames <= 0 && circles.any { it.intersectsPlayer(player) }
    }
}