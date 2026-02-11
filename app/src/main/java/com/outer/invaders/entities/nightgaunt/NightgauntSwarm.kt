// code/NightgauntSwarm.kt
package com.outer.invaders.entities.nightgaunt

import android.graphics.Canvas
import android.graphics.Paint
import com.outer.invaders.entities.Bullet
import com.outer.invaders.entities.player.Player
import kotlin.math.*
import kotlin.random.Random

class NightgauntSwarm(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val playerRef: Player // Référence au joueur pour la capture et la visée (si nécessaire)
) {
    val nightgaunts = mutableListOf<Nightgaunt>()
    private var swarmCenterX: Float = 0f
    private var swarmCenterY: Float = 0f
    private var targetX: Float = screenWidth / 2f
    private var targetY: Float = screenHeight / 2f
    private var baseSpeed = 2f + Random.nextFloat() * 2f // Vitesse de base de l'essaim
    private var swirlAngle = 0f
    private var swirlRadius = screenWidth / 6f + Random.nextFloat() * (screenWidth / 6f)
    private var timeSinceLastTargetChange = 0L
    private val targetChangeInterval = 3000L // Change de cible toutes les 3 secondes

    companion object {
        const val SWARM_SIZE = 50
        const val NIGHTGAUNT_BASE_SIZE = 40f // Taille de base d'un Nightgaunt
    }

    init {
        spawnSwarm()
    }

    // Fait apparaître un essaim sur un bord aléatoire de l'écran
    private fun spawnSwarm() {
        nightgaunts.clear()
        val edge = Random.nextInt(4) // 0: Haut, 1: Bas, 2: Gauche, 3: Droite
        val margin = NIGHTGAUNT_BASE_SIZE * 2 // Marge pour apparaître hors écran

        var startX = 0f
        var startY = 0f

        when (edge) {
            0 -> { // Haut
                startX = Random.nextFloat() * screenWidth
                startY = -margin
                targetX = Random.nextFloat() * screenWidth
                targetY = screenHeight * 0.75f // Cible vers le bas
            }
            1 -> { // Bas (moins probable ou direction différente si le joueur est en bas)
                startX = Random.nextFloat() * screenWidth
                startY = screenHeight + margin
                targetX = Random.nextFloat() * screenWidth
                targetY = screenHeight * 0.25f // Cible vers le haut
            }
            2 -> { // Gauche
                startX = -margin
                startY = Random.nextFloat() * screenHeight
                targetX = screenWidth * 0.75f // Cible vers la droite
                targetY = Random.nextFloat() * screenHeight
            }
            3 -> { // Droite
                startX = screenWidth + margin
                startY = Random.nextFloat() * screenHeight
                targetX = screenWidth * 0.25f // Cible vers la gauche
                targetY = Random.nextFloat() * screenHeight
            }
        }

        swarmCenterX = startX
        swarmCenterY = startY

        for (i in 0 until SWARM_SIZE) {
            // Position initiale légèrement dispersée autour du point de départ
            val ngX = startX + (Random.nextFloat() - 0.5f) * NIGHTGAUNT_BASE_SIZE * 4
            val ngY = startY + (Random.nextFloat() - 0.5f) * NIGHTGAUNT_BASE_SIZE * 4
            // Taille légèrement variable pour l'aspect organique
            val size = NIGHTGAUNT_BASE_SIZE * (0.8f + Random.nextFloat() * 0.4f)
            nightgaunts.add(Nightgaunt(ngX, ngY, size))
        }
        timeSinceLastTargetChange = System.currentTimeMillis()
    }

    // Calcule le mouvement de l'essaim et de chaque Nightgaunt
    /*
    fun update(bullets: MutableList<Bullet>, isPlayerCaptured: Boolean): Pair<Int, Boolean> {
    */
    fun update(bullets: MutableList<Bullet>, isPlayerCaptured: Boolean): Pair<List<Pair<Float, Float>>, Boolean> {

        var destroyedCount = 0
        val destroyedPositions = mutableListOf<Pair<Float, Float>>()
        var playerCollisionDetected = false

        // --- Mouvement de l'Essaim ---
        val currentTime = System.currentTimeMillis()
        if (currentTime - timeSinceLastTargetChange > targetChangeInterval) {
            // Changer de cible périodiquement pour un mouvement plus erratique
            targetX = Random.nextFloat() * screenWidth
            targetY = Random.nextFloat() * (screenHeight * 0.8f) // Éviter le bas de l'écran comme cible constante
            swirlRadius = screenWidth / 6f + Random.nextFloat() * (screenWidth / 6f) // Changer le rayon du tourbillon
            baseSpeed = 2f + Random.nextFloat() * 2f // Changer la vitesse
            timeSinceLastTargetChange = currentTime
        }

        // Déplacer le centre de l'essaim vers la cible
        val dx = targetX - swarmCenterX
        val dy = targetY - swarmCenterY
        val dist = sqrt(dx * dx + dy * dy)
        val moveSpeed = baseSpeed * (if(isPlayerCaptured) 1.5f else 1.0f) // Accélérer si le joueur est capturé

        if (dist > moveSpeed) {
            swarmCenterX += (dx / dist) * moveSpeed
            swarmCenterY += (dy / dist) * moveSpeed
        } else {
            swarmCenterX = targetX
            swarmCenterY = targetY
        }

        // --- Mouvement Individuel (Tourbillon / Zigzag) ---
        swirlAngle += 0.03f // Vitesse de rotation du tourbillon

        val iterator = nightgaunts.iterator()
        while (iterator.hasNext()) {
            val ng = iterator.next()

            // 1. Mouvement vers le centre de l'essaim (cohésion)
            val targetNgX = swarmCenterX + cos(swirlAngle + ng.hashCode()) * swirlRadius // Position dans le tourbillon
            val targetNgY = swarmCenterY + sin(swirlAngle + ng.hashCode()) * swirlRadius

            // 2. Ajout d'un mouvement "zigzag" aléatoire individuel
            val randomOffsetX = (Random.nextFloat() - 0.5f) * baseSpeed * 2
            val randomOffsetY = (Random.nextFloat() - 0.5f) * baseSpeed * 2

            val finalTargetX = targetNgX + randomOffsetX
            val finalTargetY = targetNgY + randomOffsetY

            // Déplacement doux vers la position cible individuelle
            val ngDx = finalTargetX - ng.x
            val ngDy = finalTargetY - ng.y
            val ngDist = sqrt(ngDx * ngDx + ngDy * ngDy)
            val individualSpeed = baseSpeed * 1.5f // Les individus bougent un peu plus vite

            if (ngDist > individualSpeed) {
                ng.x += (ngDx / ngDist) * individualSpeed
                ng.y += (ngDy / ngDist) * individualSpeed
            } else {
                ng.x = finalTargetX
                ng.y = finalTargetY
            }

            // Garder les Nightgaunts dans les limites (avec rebond ou replacement) - Optionnel
            // ng.x = ng.x.coerceIn(0f, screenWidth)
            // ng.y = ng.y.coerceIn(0f, screenHeight)

            // --- Collisions ---
            // Collision avec les balles
            val bulletIterator = bullets.iterator()
            while (bulletIterator.hasNext()) {
                val bullet = bulletIterator.next()
                if (ng.intersectsBullet(bullet.x, bullet.y)) {
                    destroyedPositions.add(Pair(ng.x, ng.y))
                    iterator.remove() // Supprimer le Nightgaunt
                    bulletIterator.remove() // Supprimer la balle
                    destroyedCount++
                    // Sortir de la boucle des balles car ce Nightgaunt est détruit
                    break
                }
            }

            // Collision avec le joueur (seulement si le Nightgaunt existe encore)
            if (!isPlayerCaptured && nightgaunts.contains(ng) && ng.intersectsPlayer(playerRef)) {
                playerCollisionDetected = true
                // Pas besoin de supprimer le Nightgaunt ici, la capture est gérée par SpaceInvadersView
            }
        }

        // Si l'essaim est vide, en faire réapparaître un nouveau ? (Géré par SpaceInvadersView)
        // if (nightgaunts.isEmpty()) {
        //     spawnSwarm()
        // }

        //return Pair(destroyedCount, playerCollisionDetected)
        return Pair(destroyedPositions, playerCollisionDetected)
    }


    // Dessine tous les Nightgaunts de l'essaim
    fun draw(canvas: Canvas, paint: Paint) {
        nightgaunts.forEach { it.draw(canvas, paint) }
    }

    // Vérifie si l'essaim est vide
    fun isEmpty(): Boolean = nightgaunts.isEmpty()

    // Renvoie le déplacement moyen de l'essaim pour entraîner le joueur
    fun getSwarmDeltaMovement(): Pair<Float, Float> {
        // On peut utiliser le déplacement calculé du centre de l'essaim
        val dx = targetX - swarmCenterX
        val dy = targetY - swarmCenterY
        val dist = sqrt(dx * dx + dy * dy)
        val moveSpeed = baseSpeed * 1.5f // Utiliser la vitesse accélérée pendant la capture

        if (dist > moveSpeed) {
            return Pair((dx / dist) * moveSpeed, (dy / dist) * moveSpeed)
        }
        return Pair(0f, 0f) // Pas de mouvement si la cible est atteinte
    }
}