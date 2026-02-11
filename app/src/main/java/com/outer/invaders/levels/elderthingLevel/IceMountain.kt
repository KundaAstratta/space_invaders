package com.outer.invaders.levels.elderthingLevel

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import com.outer.invaders.backgrounds.Background
import kotlin.random.Random

class IceMountain(
    private val x: Float,
    private val y: Float,
    private val width: Float,
    private val height: Float
) : Background.Structure(x + width/2, y + height/2, (width + height)/4) {
    private val path = Path()
    private val basePath = Path()
    private val icePaint = Paint().apply {
        // Gradient pour donner un aspect plus réaliste à la glace
        shader = LinearGradient(
            x, y,
            x, y + height,
            Color.rgb(240, 250, 255), // Bleu très pâle en haut
            Color.rgb(180, 210, 240), // Bleu glacé plus foncé en bas
            Shader.TileMode.CLAMP
        )
        style = Paint.Style.FILL
    }
    private val outlinePaint = Paint().apply {
        color = Color.rgb(150, 200, 255) // Bleu glacé pour le contour
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val highlightPaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        alpha = 180 // Semi-transparent pour un effet subtil
    }
    private val shadowPaint = Paint().apply {
        color = Color.rgb(80, 100, 150) // Bleu foncé pour les ombres
        style = Paint.Style.STROKE
        strokeWidth = 1.5f
        alpha = 100 // Très transparent
    }

    // Listes pour stocker les détails de texture
    private val crevasses = mutableListOf<Pair<Float, Float>>()
    private val snowPatches = mutableListOf<Triple<Float, Float, Float>>()

    init {
        // Créer une forme de montagne plus détaillée
        path.moveTo(x, y + height)

        // Point central supérieur (sommet principal)
        val peakX = x + width/2 + Random.nextFloat() * width * 0.1f - width * 0.05f
        val peakY = y

        // Nombre de points plus élevé pour plus de détails
        val numPoints = 12
        val xStep = width / (numPoints - 1)

        // Côté gauche de la montagne avec plus d'irrégularités
        var lastX = x
        var lastY = y + height
        for (i in 1 until numPoints/2) {
            val px = x + i * xStep + Random.nextFloat() * xStep * 0.2f - xStep * 0.1f
            // Variation de hauteur plus prononcée et aléatoire
            val heightFactor = if (i < numPoints/4) i.toFloat()/(numPoints/2) else (numPoints/2 - i).toFloat()/(numPoints/2)
            val py = y + height * (0.1f + Random.nextFloat() * 0.3f) * (1 - heightFactor*0.8f)
            path.lineTo(px, py)

            // Ajouter des crevasses potentielles
            if (Random.nextFloat() > 0.7f) {
                crevasses.add(Pair(px, py))
            }

            lastX = px
            lastY = py
        }

        // Sommet avec une légère variation
        path.lineTo(peakX, peakY)

        // Ajouter des crevasses près du sommet
        crevasses.add(Pair(peakX - width * 0.05f, peakY + height * 0.1f))
        crevasses.add(Pair(peakX + width * 0.05f, peakY + height * 0.1f))

        lastX = peakX
        lastY = peakY

        // Côté droit de la montagne
        for (i in numPoints/2 + 1 until numPoints) {
            val px = x + i * xStep + Random.nextFloat() * xStep * 0.2f - xStep * 0.1f
            val heightFactor = if (i > 3*numPoints/4) (numPoints - i).toFloat()/(numPoints/2) else (i - numPoints/2).toFloat()/(numPoints/2)
            val py = y + height * (0.1f + Random.nextFloat() * 0.3f) * (1 - heightFactor*0.8f)
            path.lineTo(px, py)

            // Ajouter des crevasses potentielles
            if (Random.nextFloat() > 0.7f) {
                crevasses.add(Pair(px, py))
            }

            lastX = px
            lastY = py
        }

        // Base
        path.lineTo(x + width, y + height)
        path.close()

        // Créer un chemin pour la base de la montagne (sol enneigé)
        basePath.moveTo(x - width * 0.1f, y + height)
        basePath.lineTo(x + width * 1.1f, y + height)
        basePath.lineTo(x + width * 1.0f, y + height + height * 0.1f)
        basePath.lineTo(x, y + height + height * 0.1f)
        basePath.close()

        // Générer des taches de neige aléatoires
        for (i in 0..8) {
            val snowX = x + Random.nextFloat() * width
            val snowY = y + height * (0.3f + Random.nextFloat() * 0.6f)
            val snowSize = width * (0.02f + Random.nextFloat() * 0.04f)
            snowPatches.add(Triple(snowX, snowY, snowSize))
        }
    }

    override fun draw(canvas: Canvas) {
        // Dessiner la base enneigée
        val basePaint = Paint().apply {
            color = Color.rgb(240, 250, 255)
            style = Paint.Style.FILL
        }
        canvas.drawPath(basePath, basePaint)

        // Dessiner la montagne
        canvas.drawPath(path, icePaint)
        canvas.drawPath(path, outlinePaint)

        // Ajouter des détails de texture

        // Dessiner des crevasses (fissures dans la glace)
        for (crevasse in crevasses) {
            val startX = crevasse.first
            val startY = crevasse.second
            val length = height * (0.05f + Random.nextFloat() * 0.15f)
            val angle = -Math.PI/2 + (Random.nextFloat() * Math.PI/4 - Math.PI/8)
            val endX = startX + (length * Math.cos(angle)).toFloat()
            val endY = startY + (length * Math.sin(angle)).toFloat()

            // Tracer la crevasse avec un effet de profondeur
            shadowPaint.alpha = 150 + Random.nextInt(105)
            canvas.drawLine(startX, startY, endX, endY, shadowPaint)
        }

        // Dessiner des accumulations de neige
        val snowPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            alpha = 180 + Random.nextInt(75)
        }

        for (snowPatch in snowPatches) {
            canvas.drawCircle(snowPatch.first, snowPatch.second, snowPatch.third, snowPaint)
        }

        // Ajouter des reflets pour donner une impression de surface glacée
        for (i in 0..5) {
            val startX = x + Random.nextFloat() * width * 0.8f + width * 0.1f
            val startY = y + height * (0.2f + Random.nextFloat() * 0.6f)
            val length = width * (0.05f + Random.nextFloat() * 0.15f)
            val angle = -Math.PI/8 + (Random.nextFloat() * Math.PI/4)
            val endX = startX + (length * Math.cos(angle)).toFloat()
            val endY = startY + (length * Math.sin(angle)).toFloat()

            // Rendre certains reflets plus brillants que d'autres
            highlightPaint.alpha = 100 + Random.nextInt(155)
            canvas.drawLine(startX, startY, endX, endY, highlightPaint)
        }

        // Ajouter un effet d'éclat au sommet
        val glintPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            alpha = 180
        }

        val peakX = x + width/2
        val peakY = y
        val glintRadius = width * 0.02f
        canvas.drawCircle(peakX, peakY, glintRadius, glintPaint)
    }
}