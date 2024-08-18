package com.example.space_invaders.entities.dhole

import android.graphics.Canvas
import android.graphics.Paint
import com.example.space_invaders.entities.Player
import kotlin.math.sin
import kotlin.random.Random

class Dhole(var x: Float, var y: Float, val circleSize: Float) {
    private val numCircles = 15
    private val circles = mutableListOf<DholeCircle>()
    private var time = 0f
    private var dx = Random.nextFloat() * 4 - 2 // Vitesse horizontale aléatoire
    private var dy = Random.nextFloat() * 4 - 2 // Vitesse verticale aléatoire
    private val maxSpeed = 5f
    private val changeDirectionChance = 0.02f // 2% de chance de changer de direction à chaque frame
    private var invincibilityFrames = 0

    init {
        for (i in 0 until numCircles) {
            circles.add(DholeCircle(x, y + i * circleSize * 0.75f, circleSize))
        }
    }

    fun setInvincibilityFrame() {
        invincibilityFrames = 30 // Par exemple, 30 frames d'invincibilité (ajustez selon vos besoins)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        circles.forEach { it.draw(canvas, paint) }
    }

    fun move(screenWidth: Float, screenHeight: Float) {
        // Mise à jour de la position
        x += dx
        y += dy

        if (invincibilityFrames > 0) {
            invincibilityFrames--
        }

        // Changement de direction aléatoire
        if (Random.nextFloat() < changeDirectionChance) {
            dx = Random.nextFloat() * maxSpeed * 2 - maxSpeed
            dy = Random.nextFloat() * maxSpeed * 2 - maxSpeed
        }

        // Gestion des bords de l'écran
        if (x < 0 || x > screenWidth) dx = -dx
        if (y < 0 || y > screenHeight / 2) dy = -dy // Limite à la moitié supérieure de l'écran

        // Mise à jour du temps pour l'ondulation
        time += 0.1f

        // Mise à jour de la position des cercles avec ondulation
        updateCirclePositions()
    }

    private fun updateCirclePositions() {
        val waveAmplitude = circleSize * 2 // Amplitude de l'ondulation
        val waveFrequency = 0.3f // Fréquence de l'ondulation

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
