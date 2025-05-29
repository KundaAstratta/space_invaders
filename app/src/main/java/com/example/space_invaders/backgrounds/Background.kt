package com.example.space_invaders.backgrounds

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import com.example.space_invaders.R
import com.example.space_invaders.utils.Enums.BackgroundType
import com.example.space_invaders.levels.rlyehLevel.CyclopeanStructure
import com.example.space_invaders.levels.elderthingLevel.IceMountain
import com.example.space_invaders.levels.elderthingLevel.ResearchStation
import com.example.space_invaders.levels.elderthingLevel.Snowflake
import kotlin.random.Random

class Background(private val context: Context, private val screenWidth: Float, private val screenHeight: Float) {
    private val paint = Paint()
    val structures = mutableListOf<Structure>()
    var backgroundType = BackgroundType.COSMIC_HORROR_RUINS
    private var backgroundBitmap: Bitmap? = null
    private val snowflakes = mutableListOf<Snowflake>()

    // Niveau Nightgaunt - Variables pour l'effet visuel si nécessaire
    private var dreamlandsShift = 0f

    init {
        loadBackgroundImage()
        generateStructures()
        if (backgroundType == BackgroundType.ANTARTIC) {
            generateSnowflakes()
        }
    }

    private fun loadBackgroundImage() {
        val resourceId = when (backgroundType) {
            BackgroundType.DEFAULT -> R.drawable.byakhee_horror_background
            BackgroundType.COSMIC_HORROR_RUINS -> R.drawable.byakhee_horror_background
            BackgroundType.DHOLE_REALM -> R.drawable.dhole_realm_background
            BackgroundType.RLYEH -> R.drawable.rlyeh_background
            BackgroundType.COLOUR_OUT_OF_SPACE -> R.drawable.color_out_of_space_background
            BackgroundType.LUNAR_SPACE -> R.drawable.lunar_background
            BackgroundType.ANTARTIC -> R.drawable.antartic_background
            BackgroundType.DREAMLANDS -> R.drawable.dreamland_background
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

        try {
            backgroundBitmap?.recycle() // Libérer l'ancienne bitmap si elle existe
            backgroundBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)?.let {
                Bitmap.createScaledBitmap(it, screenWidth.toInt(), screenHeight.toInt(), true)
            }
        } catch (e: OutOfMemoryError) {
            println("Erreur de mémoire lors du chargement de l'image de fond: ${e.message}")
            // Gérer l'erreur, peut-être utiliser une couleur de fond simple
            backgroundBitmap = null // Assurez-vous que la bitmap est nulle si le chargement échoue
        } catch (e: Exception) {
            println("Erreur lors du chargement de l'image de fond: ${e.message}")
            backgroundBitmap = null
        }

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
            BackgroundType.DEFAULT -> generateDefault()
            BackgroundType.COSMIC_HORROR_RUINS -> generateByakheeRuins()
            BackgroundType.DHOLE_REALM -> generateDholeRealm()
            BackgroundType.RLYEH -> generateRlyehStructures()
            BackgroundType.COLOUR_OUT_OF_SPACE -> generateColorOutOfSpace()
            BackgroundType.LUNAR_SPACE -> generateLunarSpaceStructures()
            BackgroundType.ANTARTIC -> generateAntarticStructures()
            BackgroundType.DREAMLANDS -> generateDreamlandsStructures()
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

    private fun generateDefault() {
        // Implémentez la génération du décor pour le royaume des Dholes
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
            BackgroundType.ANTARTIC -> generateAntarticStructures()
            else -> {} // Ne rien faire pour les autres types de fond
        }

        // Générer des flocons si on est en Antarctique
        if (backgroundType == BackgroundType.ANTARTIC) {
            generateSnowflakes()
        } else {
            snowflakes.clear()
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

    private fun generateAntarticStructures() {
        // Générer des montagnes glacées
        for (i in 0..5) {
            val mountainWidth = screenWidth * (0.2f + Random.nextFloat() * 0.3f)
            val mountainHeight = screenHeight * (0.1f + Random.nextFloat() * 0.2f)
            val posX = Random.nextFloat() * screenWidth
            val posY = screenHeight - mountainHeight - (Random.nextFloat() * screenHeight * 0.1f)
            structures.add(IceMountain(posX, posY, mountainWidth, mountainHeight))
        }

        // Générer des stations de recherche abandonnées
        for (i in 0..3) {
            val stationSize = screenWidth.coerceAtMost(screenHeight) * (0.05f + Random.nextFloat() * 0.1f)
            val centerX = Random.nextFloat() * screenWidth
            val centerY = screenHeight * (0.5f + Random.nextFloat() * 0.4f)
            structures.add(ResearchStation(centerX, centerY, stationSize))
        }
    }

    private fun generateSnowflakes() {
        snowflakes.clear()
        // Créer 200 flocons de neige avec différentes tailles et vitesses
        for (i in 0..200) {
            val size = 1f + Random.nextFloat() * 4f
            val x = Random.nextFloat() * screenWidth
            val y = Random.nextFloat() * screenHeight
            val speed = 1f + Random.nextFloat() * 3f
            val swayFactor = 0.2f + Random.nextFloat() * 0.8f
            snowflakes.add(Snowflake(x, y, size, speed, swayFactor,screenWidth))
        }
    }

    private fun generateDreamlandsStructures() {
        // Pour l'instant, pas de structures spécifiques pour les Dreamlands
        // On pourrait ajouter des éléments flottants étranges plus tard
        structures.clear()
    }

    fun draw(canvas: Canvas) {
        // Dessiner l'image de fond si elle existe
        backgroundBitmap?.let { bitmap ->
            if (!bitmap.isRecycled) { // Vérifier si la bitmap est valide
                canvas.drawBitmap(bitmap, 0f, 0f, null)
            } else {
                // Fallback si la bitmap a été recyclée (par exemple, couleur de fond)
                canvas.drawColor(Color.BLACK) // Ou une autre couleur appropriée
            }
        } ?: run {
            // Si backgroundBitmap est null (erreur de chargement), dessiner un fond simple
            canvas.drawColor(Color.BLACK) // Ou une autre couleur par défaut
        }

        when (backgroundType) {
            BackgroundType.DEFAULT -> drawColourDefault(canvas)
            BackgroundType.COSMIC_HORROR_RUINS -> drawByakheeRuins(canvas)
            BackgroundType.DHOLE_REALM -> drawDholeRealm(canvas)
            BackgroundType.RLYEH -> drawRlyehStructures(canvas)
            BackgroundType.COLOUR_OUT_OF_SPACE -> drawColourOutOfSpace(canvas)
            BackgroundType.LUNAR_SPACE -> drawLunarSpaceStructures(canvas)
            BackgroundType.ANTARTIC -> drawAntarticStructures(canvas)
            BackgroundType.DREAMLANDS -> drawDreamlandsStructures(canvas)
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

    private fun drawColourDefault(canvas: Canvas) {
        drawStars(canvas)
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

    private fun drawAntarticStructures(canvas: Canvas) {
        // Dessiner les structures de l'Antarctique
        for (structure in structures) {
            structure.draw(canvas)
        }

        // Animer et dessiner les flocons de neige
        updateAndDrawSnowflakes(canvas)
    }

    private fun drawDreamlandsStructures(canvas: Canvas) {
        // Pour l'instant, juste un effet simple ou les étoiles
        drawStars(canvas) // Dessine des étoiles simples
        // On pourrait ajouter un effet visuel subtil, comme un décalage de couleur
        dreamlandsShift += 0.5f
        if (dreamlandsShift > screenWidth) dreamlandsShift = 0f
        // Exemple: dessiner une couche de couleur semi-transparente qui bouge
        // paint.color = Color.argb(30, 100, 0, 150) // Violet translucide
        // canvas.save()
        // canvas.translate(dreamlandsShift - screenWidth / 2, 0f)
        // canvas.drawRect(-screenWidth, 0f, screenWidth * 2, screenHeight, paint)
        // canvas.restore()
    }

    private fun drawStars(canvas: Canvas) {
        paint.color = Color.WHITE
        for (i in 0..200) {
            val x = Random.nextFloat() * screenWidth
            val y = Random.nextFloat() * screenHeight
            canvas.drawCircle(x, y, 5f, paint)
        }
    }

    private fun updateAndDrawSnowflakes(canvas: Canvas) {
        //paint.color = Color.WHITE
        for (snowflake in snowflakes) {
            // Mettre à jour la position
            snowflake.update()

            // Dessiner d'abord un halo diffus (effet de lueur verdâtre)
            paint.color = snowflake.color
            paint.alpha = snowflake.alpha / 3  // Halo plus transparent
            paint.style = Paint.Style.FILL
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.glowSize, paint)

            // Dessiner ensuite le flocon plus brillant
            paint.alpha = snowflake.alpha

            // Dessiner le flocon
            canvas.drawCircle(snowflake.x, snowflake.y, snowflake.size, paint)

            // Si le flocon sort de l'écran, le replacer en haut
            if (snowflake.y > screenHeight) {
                snowflake.y = 0f
                snowflake.x = Random.nextFloat() * screenWidth
            }
        }
    }

    fun switchBackground(newType: BackgroundType) {
        backgroundType = newType
        loadBackgroundImage()
        generateStructures()

        // Générer des flocons si on passe en mode Antarctique
        if (backgroundType == BackgroundType.ANTARTIC) {
            generateSnowflakes()
        } else {
            snowflakes.clear()
        }
    }

    open class Structure(
        val centerX: Float,
        val centerY: Float,
        val radius: Float
    ) {
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

}