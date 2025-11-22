/*
package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class LifeLossAnimation(
    private val startX: Float,
    private val startY: Float,
    private val livesLost: Int
) {
    private var currentY = startY
    private var alpha = 255
    private val duration = 100 // Durée de l'animation (nombre d'itérations)
    private var age = 0

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.argb(alpha, 255, 0, 0) // Couleur rouge avec transparence
        paint.textSize = 150f // Taille du texte
        paint.textAlign = Paint.Align.CENTER
        //canvas.drawText("-1", startX, currentY, paint)
        canvas.drawText("-${livesLost}", startX, currentY, paint)
    }

    fun update() {
        age++
        currentY -= 2 // Se déplace vers le haut
        alpha = (255 * (1 - age.toFloat() / duration)).toInt() // Diminue progressivement l'opacité
    }

    fun isFinished(): Boolean {
        return age >= duration
    }
}
*/
package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class LifeLossAnimation(
    private val startX: Float,
    private val startY: Float,
    private val livesLost: Int
) {
    private var currentY = startY
    private var alpha = 255
    private val duration = 120 // Animation un peu plus longue
    private var age = 0

    fun draw(canvas: Canvas, paint: Paint) {
        // AJOUT : Ajoute une ombre pour une meilleure lisibilité
        paint.setShadowLayer(10f, 0f, 0f, Color.BLACK)

        paint.color = Color.argb(alpha, 255, 50, 50) // Rouge sang
        paint.textSize = 150f // Taille ajustée
        paint.textAlign = Paint.Align.CENTER

        canvas.drawText("-${livesLost}", startX, currentY, paint)

        // Réinitialise l'ombre pour ne pas affecter les autres dessins
        paint.clearShadowLayer()
    }

    fun update() {
        age++
        currentY -= 1f // Se déplace plus lentement
        alpha = (255 * (1 - age.toFloat() / duration)).toInt()
    }

    fun isFinished(): Boolean {
        return age >= duration
    }
}