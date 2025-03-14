package com.example.space_invaders.backgrounds

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.space_invaders.R
import com.example.space_invaders.entities.deepone.DeepOne
import kotlin.random.Random

class Background(private val context: Context, private val screenWidth: Float, private val screenHeight: Float) {
    private val paint = Paint()
    val structures = mutableListOf<Structure>()
    var backgroundType = BackgroundType.COSMIC_HORROR_RUINS
    private var backgroundBitmap: Bitmap? = null

    enum class BackgroundType {
        COSMIC_HORROR_RUINS,
        DHOLE_REALM,
        RLYEH,
        COLOUR_OUT_OF_SPACE,
        LUNAR_SPACE
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
            BackgroundType.RLYEH -> R.drawable.rlyeh_background // Assurez-vous d'avoir cette image
            BackgroundType.COLOUR_OUT_OF_SPACE -> R.drawable.color_out_of_space_background
            BackgroundType.LUNAR_SPACE -> R.drawable.lunar_background
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
            BackgroundType.RLYEH -> generateRlyehStructures()
            BackgroundType.COLOUR_OUT_OF_SPACE -> generateColorOutOfSpace()
            BackgroundType.LUNAR_SPACE -> generateLunarSpaceStructures()
        }
    }

    private fun generateByakheeRuins() {
        val minStructureRadius = screenWidth.coerceAtMost(screenHeight) * 0.025f  // 2.5% de la dimension la plus petite de l'écran
        val maxStructureRadius = screenWidth.coerceAtMost(screenHeight) * 0.1f   // 10% de la dimension la plus petite de l'écran

        for (i in 0..15) {
            val structureRadius = Random.nextFloat() * (maxStructureRadius - minStructureRadius) + minStructureRadius

            val centerX = Random.nextFloat() * (screenWidth - 2 * structureRadius) + structureRadius
            val centerY = Random.nextFloat() * (screenHeight - 2 * structureRadius) + structureRadius

            structures.add(
                Structure(
                    centerX,
                    centerY,
                    structureRadius
                )
            )
        }
    }

    private fun generateDholeRealm() {
        // Implémentez la génération du décor pour le royaume des Dholes
    }

    private fun generateColorOutOfSpace() {
        // Implémentez la génération du décor pour le monde de Color Out Of Space
    }

    private fun generateLunarSpaceStructures() {
        // Implémentez la génération du décor pour le monde de Lunar Space
    }

    // Redistribue les structures après le changement de fond
    fun redistributeStructures() {
        structures.clear()
        when (backgroundType) {
            BackgroundType.RLYEH -> generateRlyehStructures()
            else -> {} // Ne rien faire pour les autres types de fond
        }
    }

    private fun generateRlyehStructures() {
        val minStructureSize = screenWidth.coerceAtMost(screenHeight) * 0.1f
        val maxStructureSize = screenWidth.coerceAtMost(screenHeight) * 0.3f

        for (i in 0..10) {
            val structureSize = Random.nextFloat() * (maxStructureSize - minStructureSize) + minStructureSize
            val centerX = Random.nextFloat() * (screenWidth - structureSize) + structureSize / 2
            val centerY = Random.nextFloat() * (screenHeight - structureSize) + structureSize / 2
            structures.add(CyclopeanStructure(centerX, centerY, structureSize))
        }
    }

    fun draw(canvas: Canvas) {
        // Dessiner l'image de fond
        backgroundBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> drawByakheeRuins(canvas)
            BackgroundType.DHOLE_REALM -> drawDholeRealm(canvas)
            BackgroundType.RLYEH -> drawRlyehStructures(canvas)
            BackgroundType.COLOUR_OUT_OF_SPACE -> drawColourOutOfSpace(canvas)
            BackgroundType.LUNAR_SPACE -> drawLunarSpaceStructures(canvas)
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

    private fun drawRlyehStructures(canvas: Canvas) {
        drawStars(canvas)
        for (structure in structures) {
            if (structure is CyclopeanStructure) {
                structure.draw(canvas)
            }
        }
    }

    private fun drawColourOutOfSpace(canvas: Canvas) {
        drawStars(canvas)
    }

    private fun drawLunarSpaceStructures(canvas: Canvas) {
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

    open inner class Structure(val centerX: Float, val centerY: Float, val radius: Float) {
        private val gradientPaint = Paint()
        private val gradient: RadialGradient
        private val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            strokeWidth = radius * 0.02f
        }

        init {
            // Créer un gradient radial de gris allant du gris clair au gris foncé
            gradient = RadialGradient(
                centerX, centerY, radius,
                intArrayOf(Color.LTGRAY, Color.DKGRAY),
                null,
                Shader.TileMode.CLAMP
            )
            gradientPaint.shader = gradient
        }

        open fun draw(canvas: Canvas) {
            // Dessiner le cercle avec le gradient
            canvas.drawCircle(centerX, centerY, radius, gradientPaint)

            // Dessiner les "pointes" blanches
            val numberOfPoints = 8
            val angleStep = (2 * Math.PI / numberOfPoints).toFloat()
            for (i in 0 until numberOfPoints) {
                val angle = i * angleStep
                val startX = centerX + (radius * 0.8f * Math.cos(angle.toDouble())).toFloat()
                val startY = centerY + (radius * 0.8f * Math.sin(angle.toDouble())).toFloat()
                val endX = centerX + (radius * Math.cos(angle.toDouble())).toFloat()
                val endY = centerY + (radius * Math.sin(angle.toDouble())).toFloat()
                canvas.drawLine(startX, startY, endX, endY, whitePaint)
            }

            // Dessiner des cercles concentriques pour donner l'impression de relief
            for (i in 1..3) {
                val innerRadius = radius * (1 - 0.2f * i)
                canvas.drawCircle(centerX, centerY, innerRadius, whitePaint)
            }
        }

        fun intersectsPlayerVersusStruct(playerX: Float, playerY: Float, playerSize: Float): Boolean {
            val distanceX = Math.abs(playerX - centerX)
            val distanceY = Math.abs(playerY - centerY)
            val distance = Math.sqrt((distanceX * distanceX + distanceY * distanceY).toDouble()).toFloat()
            return distance < radius + playerSize / 2
        }

        fun intersectsBulletVersusStruct(objX: Float, objY: Float, objDiameter: Float): Boolean {
            val distanceX = Math.abs(objX - centerX)
            val distanceY = Math.abs(objY - centerY)
            val distance = Math.sqrt((distanceX * distanceX + distanceY * distanceY).toDouble()).toFloat()
            return distance < radius + objDiameter / 2
        }
    }

    inner class CyclopeanStructure(centerX: Float, centerY: Float, val size: Float) : Structure(centerX, centerY, size / 2) {
        private val path = Path()
        private val color = Color.argb(150, Random.nextInt(100, 200), Random.nextInt(100, 200), Random.nextInt(100, 200))

        init {
            path.moveTo(centerX, centerY - size / 2)
            val numPoints = Random.nextInt(5, 8)
            for (i in 1..numPoints) {
                val angle = i * (2 * Math.PI / numPoints)
                val radius = size / 2 * (0.5f + Random.nextFloat() * 0.5f)
                val px = centerX + (radius * Math.cos(angle)).toFloat()
                val py = centerY + (radius * Math.sin(angle)).toFloat()
                path.lineTo(px, py)
            }
            path.close()
        }

        override fun draw(canvas: Canvas) {
            paint.color = color
            paint.style = Paint.Style.FILL
            canvas.drawPath(path, paint)

            paint.color = Color.BLACK
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawPath(path, paint)
        }

        fun intersectsDeepOneVsStruct(entity: DeepOne): Boolean {
            val entityRect = RectF(entity.x, entity.y, entity.x + entity.width, entity.y + entity.height)
            val structureBounds = RectF()
            path.computeBounds(structureBounds, true)
            return RectF.intersects(entityRect, structureBounds)
        }
    }
}
