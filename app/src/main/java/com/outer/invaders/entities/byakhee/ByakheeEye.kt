package com.outer.invaders.entities.byakhee;

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class ByakheeEye(var x: Float, var y: Float, val radius: Float) {
    private var pupilRadius = radius / 3
    private var pupilDirection = 1 // 1 pour expansion, -1 pour contraction
    private var pupilSpeed = 0.5f
    private val numVeins = 8 // Nombre de veines
    private val veinWidth = 2f // Épaisseur des veines

    fun draw(canvas: Canvas, paint: Paint) {
        // Dessiner le blanc de l'œil (vert dans ce cas)
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, radius, paint)

        // Dessiner les veines rouges
        paint.color = Color.RED
        paint.strokeWidth = veinWidth
        paint.style = Paint.Style.STROKE

        // Calculer l'angle entre chaque veine
        val angleStep = (2 * Math.PI) / numVeins

        for (i in 0 until numVeins) {
            val angle = i * angleStep
            // Point de départ (près de la pupille)
            val startX = x + (pupilRadius * 1.2f * Math.cos(angle)).toFloat()
            val startY = y + (pupilRadius * 1.2f * Math.sin(angle)).toFloat()
            // Point d'arrivée (près du bord de l'iris)
            val endX = x + (radius * 0.9f * Math.cos(angle)).toFloat()
            val endY = y + (radius * 0.9f * Math.sin(angle)).toFloat()

            // Dessiner la veine principale
            canvas.drawLine(startX, startY, endX, endY, paint)

            // Ajouter de petites branches aux veines
            if (i % 2 == 0) { // Une branche sur deux pour ne pas surcharger
                val branchLength = radius * 0.2f
                val branchAngle = angle + Math.PI / 6 // 30 degrés
                val midX = (startX + endX) / 2
                val midY = (startY + endY) / 2
                val branchEndX = midX + (branchLength * Math.cos(branchAngle)).toFloat()
                val branchEndY = midY + (branchLength * Math.sin(branchAngle)).toFloat()
                canvas.drawLine(midX, midY, branchEndX, branchEndY, paint)
            }
        }

        // Remettre le style en FILL pour la pupille
        paint.style = Paint.Style.FILL

        // Dessiner la pupille  avec pulsation
        paint.color = Color.BLACK
        canvas.drawCircle(x, y, pupilRadius, paint)

        // Mettre à jour la taille de la pupille
        pupilRadius += pupilSpeed * pupilDirection
        if (pupilRadius > radius / 2) {
            pupilRadius = radius / 2
            pupilDirection = -1
        } else if (pupilRadius < radius / 4) {
            pupilRadius = radius / 4
            pupilDirection = 1
        }
    }
}
