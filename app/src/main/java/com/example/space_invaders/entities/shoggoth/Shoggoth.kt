package com.example.space_invaders.entities.shoggoth

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// Classe représentant une bulle protoplasmique
data class ProtoplasmicBubble(
    var x: Float,
    var y: Float,
    var initialRadius: Float,
    var radius: Float,
    var pulseOffset: Float = Random.nextFloat() * 2 * PI.toFloat(),
    var pulseSpeed: Float = Random.nextFloat() * 0.1f + 0.05f,
    // Position relative au centre du Shoggoth
    var relativeX: Float = 0f,
    var relativeY: Float = 0f,
    // Paramètres de mouvement aléatoire
    var wanderAngle: Float = Random.nextFloat() * 2 * PI.toFloat(),
    var wanderSpeed: Float = Random.nextFloat() * 0.5f + 0.2f

) {
    fun pulse(time: Long) {
        val pulseFactor = sin((time * pulseSpeed + pulseOffset).toDouble()).toFloat() * 0.2f + 1f
        radius = initialRadius * pulseFactor
    }

    fun updatePosition(centerX: Float, centerY: Float, time: Long) {
        // Mise à jour de l'angle de déplacement avec un peu de chaos
        wanderAngle += (Random.nextFloat() - 0.5f) * 0.1f

        // Mouvement brownien autour de la position relative
        val wanderX = cos(wanderAngle) * wanderSpeed
        val wanderY = sin(wanderAngle) * wanderSpeed

        relativeX += wanderX
        relativeY += wanderY

        // Limite la distance maximale par rapport au centre
        val maxDistance = radius * 3f
        val currentDistance = kotlin.math.hypot(relativeX, relativeY)
        if (currentDistance > maxDistance) {
            val scale = maxDistance / currentDistance
            relativeX *= scale
            relativeY *= scale
        }

        // Position finale
        x = centerX + relativeX
        y = centerY + relativeY
    }
}

// Classe représentant un oeil
data class Eye(
    var x: Float,
    var y: Float,
    var size: Float,
    var lifespan: Int = Random.nextInt(50, 150),
    var alpha: Int = 255
) {
    fun update(): Boolean {
        lifespan--
        if (lifespan < 30) {
            alpha = (alpha * 0.9f).toInt()
        }
        return lifespan > 0
    }
}

// Classe représentant une tentacule
data class PseudoPode(
    var startX: Float,
    var startY: Float,
    var length: Float,
    var angle: Float = Random.nextFloat() * 2 * PI.toFloat(),
    var lifespan: Int = Random.nextInt(70, 170),
    var alpha: Int = 255,
    var segments: Int = 5  // Nombre de segments de la tentacule
) {
    val points = mutableListOf<Pair<Float, Float>>()

    fun update(): Boolean {
        lifespan--
        if (lifespan < 30) {
            alpha = (alpha * 0.9f).toInt()
        }
        return lifespan > 0
    }

    fun updatePoints() {
        points.clear()
        points.add(Pair(startX, startY))

        var currentX = startX
        var currentY = startY
        val segmentLength = length / segments

        for (i in 1..segments) {
            val segmentAngle = angle + sin(i * 0.4f) * 0.7f  // Ajoute une ondulation
            currentX += cos(segmentAngle) * segmentLength
            currentY += sin(segmentAngle) * segmentLength
            points.add(Pair(currentX, currentY))
        }
    }
}

class Shoggoth(
    var x: Float,
    var y: Float,
    val initialSize: Float,
    val screenWidth: Float, // Ajout de screenWidth
    val screenHeight: Float // Ajout de screenHeight
) {

    // Ajouter aux propriétés existantes
    private val pseudopodes = mutableListOf<PseudoPode>()
    private var currentSize = initialSize
    private var baseColor = Color.HSVToColor(floatArrayOf(
        Random.nextFloat() * 360f,  // Hue
        0.8f,  // Saturation - plus élevée pour des couleurs plus vives
        0.9f   // Value - plus élevé pour des couleurs plus brillantes
    ))
    private val bubbles = mutableListOf<ProtoplasmicBubble>()
    private val eyes = mutableListOf<Eye>()
   // private val pseudopodes = mutableListOf<Path>()
    private var time: Long = 0

    private var colorTransitionProgress = 0f
    private var targetColor = generateRandomColor()

    // Propriété pour stocker la direction actuelle
    private var currentDirection: Pair<Float, Float> = Pair(1f, 0f) // Par défaut, vers la droite
    // Add these properties at the beginning of the class
    private var dx: Float = 2f  // Vitesse horizontale
    private var dy: Float = 2f  // Vitesse verticale
    private var moveTimer: Int = 0
    private val CHANGE_DIRECTION_INTERVAL = 60  // Changement de direction toutes les 60 frames


    private val paint = Paint().apply {
        style = Paint.Style.FILL
        isAntiAlias = true
        alpha = 255  // Opacité maximale
    }

    init {
        generateBubbles()
    }

    private fun generateRandomColor(): Int {
        return Color.HSVToColor(floatArrayOf(
            Random.nextFloat() * 360f,  // Hue
            0.8f,  // Saturation
            0.9f   // Value
        ))
    }

    private fun generateBubbles() {
        val numBubbles = (currentSize / 8).toInt().coerceIn(20, 40)  // Plus de bulles
        bubbles.clear()

        for (i in 0 until numBubbles) {
            // Distribution plus aléatoire des bulles
            val angle = Random.nextFloat() * 2 * PI.toFloat()
            val distance = currentSize * Random.nextFloat() * 0.4f
            val bubbleRadius = currentSize * (0.15f + Random.nextFloat() * 0.1f)

            // Calcul des positions relatives initiales
            val relativeX = cos(angle) * distance
            val relativeY = sin(angle) * distance

            bubbles.add(ProtoplasmicBubble(
                x = x + relativeX,
                y = y + relativeY,
                initialRadius = bubbleRadius,
                radius = bubbleRadius,
                relativeX = relativeX,
                relativeY = relativeY,
                // Variation plus importante des vitesses de pulsation
                pulseSpeed = Random.nextFloat() * 0.15f + 0.05f
            ))
        }
    }

    private fun updateEyes() {
        if (Random.nextFloat() < 0.05f) {
            // Sélectionner une bulle au hasard pour y placer l'œil
            val randomBubble = bubbles.randomOrNull()

            if (randomBubble != null) {
                // Créer l'œil à la position de la bulle
                eyes.add(Eye(
                    x = randomBubble.x,
                    y = randomBubble.y,
                    // Ajuster la taille de l'œil en fonction de la taille de la bulle
                    size = randomBubble.radius * 0.8f,
                    // Augmenter légèrement la durée de vie pour une meilleure visibilité
                    lifespan = Random.nextInt(70, 170)
                ))
            }
        }

        // Mettre à jour la position des yeux existants pour qu'ils suivent leurs bulles
        eyes.forEach { eye ->
            // Trouver la bulle la plus proche de l'œil
            val nearestBubble = bubbles.minByOrNull { bubble ->
                val dx = eye.x - bubble.x
                val dy = eye.y - bubble.y
                dx * dx + dy * dy
            }

            // Si une bulle est trouvée, faire suivre l'œil doucement
            if (nearestBubble != null) {
                // Interpolation linéaire pour un mouvement plus fluide
                val lerpFactor = 0.1f
                eye.x += (nearestBubble.x - eye.x) * lerpFactor
                eye.y += (nearestBubble.y - eye.y) * lerpFactor
            }
        }

        // Supprimer les yeux expirés
        eyes.removeAll { !it.update() }
    }

    private fun updateColor() {
        colorTransitionProgress += 0.01f
        if (colorTransitionProgress >= 1f) {
            baseColor = targetColor
            targetColor = generateRandomColor()
            colorTransitionProgress = 0f
        }
    }

    fun move() {
        // Mise à jour du timer
        moveTimer++

        // Changement aléatoire de direction périodiquement
        if (moveTimer >= CHANGE_DIRECTION_INTERVAL) {
            // Choisir une nouvelle direction aléatoire
            val directions = listOf(
                Pair(6f, 0f),    // Droite
                Pair(-6f, 0f),   // Gauche
                Pair(0f, 6f),    // Bas
                Pair(0f, -6f)    // Haut
            )
            val newDirection = directions.random()
            dx = newDirection.first
            dy = newDirection.second
            moveTimer = 0
        }

        // Mise à jour de la position
        x += dx
        y += dy

        // Gestion des collisions avec les bords de l'écran
        val radius = currentSize / 2

        // Collision avec les bords horizontaux
        if (x - radius < 0) {
            x = radius
            dx = abs(dx)  // Force le mouvement vers la droite
        } else if (x + radius > screenWidth) {
            x = screenWidth - radius
            dx = -abs(dx)  // Force le mouvement vers la gauche
        }

        // Collision avec les bords verticaux
        if (y - radius < 0) {
            y = radius
            dy = abs(dy)  // Force le mouvement vers le bas
        } else if (y + radius > screenHeight) {
            y = screenHeight - radius
            dy = -abs(dy)  // Force le mouvement vers le haut
        }

        // Mettre à jour la position des bulles avec orbite
        bubbles.forEach { bubble ->
            bubble.updatePosition(x, y, time)
            bubble.pulse(time)

            // Ajouter un mouvement brownien supplémentaire
            bubble.x += (Random.nextFloat() - 0.5f) * 2f
            bubble.y += (Random.nextFloat() - 0.5f) * 2f

            // Créer une tentacule pour chaque bulle
            val pseudopode = pseudopodes.find { it.startX == bubble.x && it.startY == bubble.y }
            if (pseudopode == null && Random.nextFloat() < 0.05f) {
                pseudopodes.add(PseudoPode(
                    startX = bubble.x,
                    startY = bubble.y,
                    length = bubble.radius * 3.0f
                ))
            }
        }

        // Mise à jour des tentacules
        pseudopodes.forEach { pseudopode ->
            // Trouver la bulle la plus proche
            val nearestBubble = bubbles.minByOrNull { bubble ->
                val dx = pseudopode.startX - bubble.x
                val dy = pseudopode.startY - bubble.y
                dx * dx + dy * dy
            }

            if (nearestBubble != null) {
                // Mouvement fluide vers la bulle
                pseudopode.startX += (nearestBubble.x - pseudopode.startX) * 0.08f
                pseudopode.startY += (nearestBubble.y - pseudopode.startY) * 0.08f
                // Mise à jour de l'angle en fonction du mouvement
                pseudopode.angle += sin(time * 0.03f) * 0.15f
                pseudopode.updatePoints()
            }
        }

        // Supprimer les tentacules expirées
        pseudopodes.removeAll { !it.update() }

        updateEyes()
        updateColor()
        time++
    }

    fun draw(canvas: Canvas) {
        // Corps principal avec une opacité légèrement réduite
        paint.color = Color.argb(
            200,  // Opacité réduite pour plus d'effet organique
            Color.red(baseColor),
            Color.green(baseColor),
            Color.blue(baseColor)
        )
        canvas.drawCircle(x, y, currentSize / 2, paint)

        // Dessiner les bulles protoplasmiques avec effet de profondeur
        bubbles.forEach { bubble ->
            // Variation de l'opacité pour donner un effet de profondeur
            val opacity = (150 + Random.nextInt(105)).coerceIn(150, 255)
            paint.color = Color.argb(
                opacity,
                Color.red(baseColor),
                Color.green(baseColor),
                Color.blue(baseColor)
            )
            canvas.drawCircle(bubble.x, bubble.y, bubble.radius, paint)
        }

        // Dessiner les pseudopodes
        val pseudopodePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = initialSize * 0.04f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        pseudopodes.forEach { pseudopode ->
            // Dessiner la tentacule avec un dégradé de couleur et d'épaisseur
            val path = Path()
            path.moveTo(pseudopode.points[0].first, pseudopode.points[0].second)

            for (i in 1 until pseudopode.points.size) {
                path.lineTo(pseudopode.points[i].first, pseudopode.points[i].second)

                // Variation de l'épaisseur et de la couleur le long de la tentacule
                val progress = i.toFloat() / pseudopode.points.size
                pseudopodePaint.strokeWidth = initialSize * 0.02f * (1f - progress * 0.8f)
                pseudopodePaint.color = Color.argb(
                    (pseudopode.alpha * (1f - progress * 0.5f)).toInt(),
                    Color.red(baseColor),
                    Color.green(baseColor),
                    Color.blue(baseColor)
                )
                canvas.drawPath(path, pseudopodePaint)
            }
        }

        // Dessiner les yeux
        eyes.forEach { eye ->
            // Blanc de l'œil
            paint.color = Color.argb(eye.alpha, 255, 255, 255)
            canvas.drawCircle(eye.x, eye.y, eye.size, paint)

            // Iris
            paint.color = Color.argb(eye.alpha, 0, 0, 0)
            canvas.drawCircle(eye.x, eye.y, eye.size * 0.6f, paint)

            // Pupille
            paint.color = Color.argb(eye.alpha, 255, 0, 0)  // Rouge vif pour la pupille
            canvas.drawCircle(eye.x, eye.y, eye.size * 0.3f, paint)

            // Reflet dans l'œil
            paint.color = Color.argb(eye.alpha, 255, 255, 255)
            canvas.drawCircle(eye.x - eye.size * 0.2f, eye.y - eye.size * 0.2f, eye.size * 0.1f, paint)
        }
    }

    fun intersectsBullet(bulletX: Float, bulletY: Float): Boolean {
        val distance = kotlin.math.hypot(bulletX - x, bulletY - y)
        return distance < currentSize / 2
    }

    fun hit(): Boolean {
        currentSize *= 0.9f
        generateBubbles()
        return currentSize < initialSize * 0.3f
    }

}