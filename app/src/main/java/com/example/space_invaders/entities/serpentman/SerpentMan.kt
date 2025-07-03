package com.example.space_invaders.entities.serpentman

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.space_invaders.entities.player.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class SerpentMan(
    var x: Float,
    var y: Float,
    val size: Float,
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val player: Player
) {

    // Caractéristiques de l'Homme-Serpent
    var lives = 3
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPath = Path()

    // Comportement et attaque
    private var attackCooldown = 0L
    private val attackInterval = 2000L // Attaque toutes les 2 secondes
    private var lastAttackTime = 0L

    // Mouvement (téléportation)
    private var teleportCooldown = 0L
    private val teleportInterval = 4000L // Se téléporte toutes les 4 secondes
    private var lastTeleportTime = 0L

    init {
        paint.style = Paint.Style.FILL
    }

    // Méthode pour dessiner l'Homme-Serpent
    fun draw(canvas: Canvas) {
        // Corps serpentin
        paint.color = Color.rgb(27, 82, 60) // Vert sombre reptilien
        bodyPath.reset()
        bodyPath.moveTo(x, y - size / 2)
        bodyPath.quadTo(x - size / 1.5f, y, x, y + size / 2)
        bodyPath.quadTo(x + size / 1.5f, y, x, y - size / 2)
        canvas.drawPath(bodyPath, paint)

        // Cape ou capuche
        paint.color = Color.rgb(40, 26, 13) // Marron sombre
        canvas.drawCircle(x, y - size / 4, size / 3, paint)

        // Yeux brillants
        paint.color = Color.YELLOW
        canvas.drawCircle(x - size / 8, y - size / 3, size / 15, paint)
        canvas.drawCircle(x + size / 8, y - size / 3, size / 15, paint)
    }

    // Mise à jour de l'état de l'Homme-Serpent
    fun update() {
        val currentTime = System.currentTimeMillis()

        // Gérer la téléportation
        if (currentTime > lastTeleportTime + teleportInterval + teleportCooldown) {
            teleport()
            lastTeleportTime = currentTime
            teleportCooldown = Random.nextLong(1000) // Ajoute un délai aléatoire
        }
    }

    // Mécanisme d'attaque
    fun maybeAttack(projectiles: MutableList<SerpentManProjectile>, illusions: MutableList<SerpentManIllusion>): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime > lastAttackTime + attackInterval + attackCooldown) {
            lastAttackTime = currentTime
            attackCooldown = Random.nextLong(500) // Délai aléatoire

            // 70% de chance de lancer un projectile, 30% de créer une illusion
            if (Random.nextFloat() < 0.7f) {
                projectiles.add(createProjectile())
            } else {
                illusions.add(createIllusion())
            }
            return true
        }
        return false
    }

    // Création d'un projectile magique
    private fun createProjectile(): SerpentManProjectile {
        val angle = atan2(player.y - y, player.x - x)
        val speed = 8f
        val velocityX = cos(angle) * speed
        val velocityY = sin(angle) * speed
        return SerpentManProjectile(x, y, velocityX, velocityY, size / 8)
    }

    // Création d'une illusion
    private fun createIllusion(): SerpentManIllusion {
        // L'illusion apparaît près de l'homme-serpent et se dirige vers le joueur
        return SerpentManIllusion(x + Random.nextFloat() * 100 - 50, y, size, player)
    }


    // Téléportation de l'entité
    private fun teleport() {
        x = Random.nextFloat() * (screenWidth - size) + size / 2
        y = Random.nextFloat() * (screenHeight / 2) // Apparaît dans la moitié supérieure
    }

    // Gestion des dégâts
    fun hit(): Boolean {
        lives--
        return lives <= 0
    }

    // Détection de collision avec une balle
    fun intersectsBullet(bulletX: Float, bulletY: Float, bulletRadius: Float): Boolean {
        val distance = kotlin.math.hypot((x - bulletX).toDouble(), (y - bulletY).toDouble()).toFloat()
        return distance < size / 2 + bulletRadius
    }
}
