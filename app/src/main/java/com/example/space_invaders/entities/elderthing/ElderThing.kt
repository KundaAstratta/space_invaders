package com.example.space_invaders.entities.elderthing

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.random.Random

class ElderThing(
    var x: Float,
    var y: Float,
    val size: Float,
    val screenWidth: Float,
    val screenHeight: Float
) {
    private var speedX = Random.nextFloat() * 4f - 2f
    private var speedY = Random.nextFloat() * 4f - 2f
    private var health = 3
    private var lastDirectionChangeTime = System.currentTimeMillis()
    private val directionChangeInterval = 2000L // Change direction every 2 seconds
    private val path = Path()
    private var rotation = 0f
    private val rotationSpeed = Random.nextFloat() * 5f - 2.5f

    // Variables pour le halo
    private var haloSize = size * 1.8f
    private var haloPulsation = 0f
    private val haloPulsationSpeed = 0.05f
    private val haloMaxMultiplier = 0.2f // Maximum variation de taille pour la pulsation

    // Elder Things can fire small projectiles
    val projectiles = mutableListOf<ElderProjectile>()
    private var lastFireTime = System.currentTimeMillis()
    private val fireInterval = 3000L // Fire every 3 seconds

    fun draw(canvas: Canvas, paint: Paint) {
        // Dessiner d'abord le halo pulsant
        drawHalo(canvas, paint)

        // Sauvegarde de l'état actuel du canvas
        canvas.save()

        // Translation et rotation autour du point central
        canvas.translate(x, y)
        canvas.rotate(rotation)

        // Dessin du corps en forme d'étoile
        paint.style = Paint.Style.FILL
        paint.color = Color.rgb(180, 180, 220) // Couleur bleu-grisâtre

        // Création d'une forme d'étoile à 5 pointes
        path.reset()
        val outerRadius = size / 2
        val innerRadius = size / 4

        for (i in 0 until 5) {
            val outerAngle = Math.toRadians((i * 72).toDouble()).toFloat()
            val innerAngle = Math.toRadians((i * 72 + 36).toDouble()).toFloat()

            val outerX = Math.cos(outerAngle.toDouble()).toFloat() * outerRadius
            val outerY = Math.sin(outerAngle.toDouble()).toFloat() * outerRadius

            val innerX = Math.cos(innerAngle.toDouble()).toFloat() * innerRadius
            val innerY = Math.sin(innerAngle.toDouble()).toFloat() * innerRadius

            if (i == 0) {
                path.moveTo(outerX, outerY)
            } else {
                path.lineTo(outerX, outerY)
            }

            path.lineTo(innerX, innerY)
        }

        path.close()
        canvas.drawPath(path, paint)

        // Dessin du cercle central
        paint.color = Color.rgb(100, 50, 150) // Couleur pourpre
        canvas.drawCircle(0f, 0f, size / 6, paint)

        // Dessin des tentacules
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size / 20
        paint.color = Color.rgb(220, 180, 180) // Couleur rosâtre

        for (i in 0 until 5) {
            val angle = Math.toRadians((i * 72).toDouble()).toFloat()
            val startX = Math.cos(angle.toDouble()).toFloat() * (size / 2)
            val startY = Math.sin(angle.toDouble()).toFloat() * (size / 2)
            val endX = Math.cos(angle.toDouble()).toFloat() * (size / 1.5f)
            val endY = Math.sin(angle.toDouble()).toFloat() * (size / 1.5f)

            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Restauration du canvas
        canvas.restore()

        // Dessin des projectiles
        projectiles.forEach { it.draw(canvas, paint) }
    }

    private fun drawHalo(canvas: Canvas, paint: Paint) {
        // Mise à jour de la pulsation
        haloPulsation += haloPulsationSpeed
        if (haloPulsation > Math.PI * 2) {
            haloPulsation = 0f
        }

        // Calcul de la taille actuelle du halo avec pulsation
        val pulseFactor = 1f + Math.sin(haloPulsation.toDouble()).toFloat() * haloMaxMultiplier
        val currentHaloSize = haloSize * pulseFactor

        // Création d'un gradient radial pour le halo
        val haloGradient = RadialGradient(
            x, y,
            currentHaloSize,
            intArrayOf(
                Color.argb(120, 50, 220, 50),  // Vert vif semi-transparent au centre
                Color.argb(70, 30, 180, 30),   // Vert moyen
                Color.argb(30, 0, 130, 50),    // Vert foncé
                Color.argb(0, 0, 100, 0)       // Transparent à l'extérieur
            ),
            floatArrayOf(0f, 0.4f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        // Application du gradient
        paint.style = Paint.Style.FILL
        paint.shader = haloGradient

        // Dessin du halo
        canvas.drawCircle(x, y, currentHaloSize, paint)

        // Réinitialisation du shader pour ne pas affecter d'autres dessins
        paint.shader = null
    }

    fun move() {
        rotation += rotationSpeed

        // Changement de direction périodique
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDirectionChangeTime > directionChangeInterval) {
            speedX = Random.nextFloat() * 8f - 4f
            speedY = Random.nextFloat() * 8f - 4f
            lastDirectionChangeTime = currentTime
        }

        // Déplacement de l'Elder Thing
        x += speedX
        y += speedY

        // Rebond sur les bords
        if (x < size / 2) {
            x = size / 2
            speedX = -speedX
        } else if (x > screenWidth - size / 2) {
            x = screenWidth - size / 2
            speedX = -speedX
        }

        if (y < size / 2) {
            y = size / 2
            speedY = -speedY
        } else if (y > screenHeight - size / 2) {
            y = screenHeight - size / 2
            speedY = -speedY
        }

        // Tir de projectiles périodique
        if (currentTime - lastFireTime > fireInterval) {
            fireProjectile()
            lastFireTime = currentTime
        }

        // Mise à jour des projectiles
        val projectilesToRemove = mutableListOf<ElderProjectile>()
        projectiles.forEach {
            it.move()
            // Suppression des projectiles hors écran
            if (it.x < 0 || it.x > screenWidth || it.y < 0 || it.y > screenHeight) {
                projectilesToRemove.add(it)
            }
        }
        projectiles.removeAll(projectilesToRemove)
    }

    private fun fireProjectile() {
        val angle = Math.toRadians(Random.nextDouble(0.0, 360.0)).toFloat()
        val projectileSpeed = 6f
        val velocityX = Math.cos(angle.toDouble()).toFloat() * projectileSpeed
        val velocityY = Math.sin(angle.toDouble()).toFloat() * projectileSpeed

        projectiles.add(ElderProjectile(x, y, velocityX, velocityY, size / 8))
    }

    fun hit(): Boolean {
        health--
        return health <= 0
    }

    fun intersectsBullet(bulletX: Float, bulletY: Float): Boolean {
        val distance = kotlin.math.hypot(x - bulletX, y - bulletY)
        return distance < size / 2
    }
}