package com.example.space_invaders.backgrounds

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import com.example.space_invaders.R
import kotlin.random.Random

class Background(private val context: Context, private val screenWidth: Float, private val screenHeight: Float) {
    private val paint = Paint()
    val structures = mutableListOf<Structure>()
    private var backgroundType = BackgroundType.COSMIC_HORROR_RUINS
    private var backgroundBitmap: Bitmap? = null

    enum class BackgroundType {
        COSMIC_HORROR_RUINS,
        DHOLE_REALM
        // Ajoutez d'autres types de fond ici au besoin
    }

    init {
        loadBackgroundImage()
        generateStructures()
    }

    private fun loadBackgroundImage() {
        val resourceId = when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> R.drawable.byakhee_horror_background
            BackgroundType.DHOLE_REALM -> R.drawable.dhole_realm_background
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resourceId, options)

        val sampleSize = calculateInSampleSize(options, screenWidth.toInt(), screenHeight.toInt())
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }

        backgroundBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap!!, screenWidth.toInt(), screenHeight.toInt(), true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun generateStructures() {
        structures.clear()
        when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> generateByakheeRuins()
            BackgroundType.DHOLE_REALM -> generateDholeRealm()
        }
    }

    private fun generateByakheeRuins() {
        val minStructureWidth = screenWidth * 0.05f  // 5% de la largeur de l'écran
        val maxStructureWidth = screenWidth * 0.15f  // 15% de la largeur de l'écran
        val minStructureHeight = screenHeight * 0.05f  // 5% de la hauteur de l'écran
        val maxStructureHeight = screenHeight * 0.2f  // 20% de la hauteur de l'écran

        for (i in 0..10) {
            val structureWidth = Random.nextFloat() * (maxStructureWidth - minStructureWidth) + minStructureWidth
            val structureHeight = Random.nextFloat() * (maxStructureHeight - minStructureHeight) + minStructureHeight

            structures.add(
                Structure(
                    Random.nextFloat() * (screenWidth - structureWidth),
                    Random.nextFloat() * (screenHeight - structureHeight),
                    structureWidth,
                    structureHeight
                )
            )
        }
    }

    private fun generateDholeRealm() {
        // Implémentez la génération du décor pour le royaume des Dholes
    }

    fun draw(canvas: Canvas) {

        // Dessiner l'image de fond
        backgroundBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> drawByakheeRuins(canvas)
            BackgroundType.DHOLE_REALM -> drawDholeRealm(canvas)
        }
    }

    private fun drawByakheeRuins(canvas: Canvas) {
        //canvas.drawColor(Color.BLACK)
        drawStars(canvas)
        paint.color = Color.DKGRAY
        for (structure in structures) {
            structure.draw(canvas)
        }
    }

    private fun drawDholeRealm(canvas: Canvas) {
        // Implémentez le dessin du royaume des Dholes
        drawStars(canvas)
    }

    private fun drawStars(canvas: Canvas) {
        paint.color = Color.WHITE
        for (i in 0..200) {
            val x = Random.nextFloat() * screenWidth
            val y = Random.nextFloat() * screenHeight
            canvas.drawCircle(x, y, 5f, paint)
        }
    }

    fun switchBackground(newType: BackgroundType) {
        backgroundType = newType
        loadBackgroundImage()
        generateStructures()
    }

    inner class Structure(val x: Float, val y: Float, val width: Float, val height: Float) {
        private val gradientPaint = Paint()
        private val gradient: LinearGradient

        init {
            // Créer un gradient de gris allant du gris clair au gris foncé
            gradient = LinearGradient(
                x, y, x, y + height,
                intArrayOf(Color.LTGRAY, Color.DKGRAY),
                null,
                Shader.TileMode.CLAMP
            )
            gradientPaint.shader = gradient
        }

        fun draw(canvas: Canvas) {
            // Dessiner la structure avec le gradient
            canvas.drawRect(x, y, x + width, y + height, gradientPaint)

            // Dessiner les traits blancs pour simuler les briques
            val brickPaint = Paint().apply {
                color = Color.WHITE
                strokeWidth = width * 0.01f  // Ajuster l'épaisseur des lignes en fonction de la largeur
            }

            // Calculer la taille des briques et l'espacement
            val brickWidth = width / 5 // 5 briques en largeur
            val brickHeight = height / 3 // 3 briques en hauteur
            val spacing = width * 0.01f // Espacement entre les briques

            // Dessiner les lignes horizontales
            for (i in 1..2) { // 2 lignes horizontales
                val brickY = y + i * brickHeight + (i - 1) * spacing
                canvas.drawLine(x, brickY, x + width, brickY, brickPaint)
            }

            // Dessiner les lignes verticales
            for (i in 1..4) { // 4 lignes verticales
                val brickX = x + i * brickWidth + (i - 1) * spacing
                canvas.drawLine(brickX, y, brickX, y + height, brickPaint)
            }
        }

        fun intersectsPlayerVersusStruct(playerX: Float, playerY: Float, playerSize: Float): Boolean {
            return playerX + playerSize / 2 > x && playerX - playerSize / 2 < x + width &&
                    playerY + playerSize / 2 > y && playerY - playerSize / 2 < y + height
        }

        fun intersectsBulletVersusStruct(objX: Float, objY: Float, objDiameter: Float): Boolean {
            val radius = objDiameter / 2

            return (objX + radius > x && objX - radius < x + width) &&
                    (objY + radius > y && objY - radius < y + height)
        }

    }
}