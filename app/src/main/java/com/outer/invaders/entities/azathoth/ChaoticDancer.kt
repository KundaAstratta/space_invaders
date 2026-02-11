package com.outer.invaders.entities.azathoth

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.outer.invaders.entities.player.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private enum class StateDancer { ORBITING, CHARGING }

class ChaoticDancer(
    private val centreX: Float,
    private val centreY: Float,
    private val player: Player
) {
    private val paint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.rgb(255, 100, 200) // Couleur distinctive
    }
    private val path = Path()

    var x: Float = 0f
    var y: Float = 0f
    val rayonHitbox = 30f
    var health: Int = 3 // Le danseur est détruit en 3 tirs

    private var state = StateDancer.ORBITING
    private var stateTimer = System.currentTimeMillis()
    private val chargeCooldown = Random.nextLong(3000, 6000) // Charge toutes les 3 à 6 secondes
    private val chargeDuration = 1000L // La charge dure 1 seconde

    private var targetX: Float = 0f
    private var targetY: Float = 0f

    // Propriétés orbitales
    private var angleOrbital: Float = Random.nextFloat() * 2f * Math.PI.toFloat()
    private val vitesseOrbitale: Float = Random.nextFloat() * 0.001f + 0.0005f
    private var rayonOrbital: Float = Random.nextFloat() * 200f + 450f

    // Propriétés de forme
    private val nombreSommets = 5
    private val anglesSommets = FloatArray(nombreSommets) { i -> (i.toFloat() / nombreSommets) * 2f * Math.PI.toFloat() }
    private var tempsMorphing = 0L

    init {
        //updatePosition(0L)
    }

    fun update(deltaTime: Long) {
        tempsMorphing += deltaTime
        val currentTime = System.currentTimeMillis()

        when (state) {
            StateDancer.ORBITING -> {
                // Tourne autour d'Azathoth
                angleOrbital += vitesseOrbitale * deltaTime
                x = centreX + cos(angleOrbital) * rayonOrbital
                y = centreY + sin(angleOrbital) * rayonOrbital

                // Vérifie s'il est temps de charger le joueur
                if (currentTime - stateTimer > chargeCooldown) {
                    state = StateDancer.CHARGING
                    stateTimer = currentTime
                    // Vise la position actuelle du joueur
                    targetX = player.x
                    targetY = player.y
                }
            }
            StateDancer.CHARGING -> {
                // Fonce en ligne droite vers la dernière position connue du joueur
                val angleToTarget = atan2((targetY - y).toDouble(), (targetX - x).toDouble()).toFloat()
                val moveSpeed = 10f
                x += cos(angleToTarget) * moveSpeed * (deltaTime / 16f)
                y += sin(angleToTarget) * moveSpeed * (deltaTime / 16f)

                // Après la charge, retourne en orbite
                if (currentTime - stateTimer > chargeDuration) {
                    state = StateDancer.ORBITING
                    stateTimer = currentTime
                    // Recalcule une nouvelle orbite pour varier
                    rayonOrbital = Random.nextFloat() * 200f + 450f
                }
            }
        }
    }

    fun draw(canvas: Canvas) {
        path.reset()
        for (i in 0 until nombreSommets) {
            val morphing = sin(tempsMorphing * 0.005f + anglesSommets[i]) * 15f
            val rayonSommet = rayonHitbox + morphing
            val posX = x + cos(anglesSommets[i]) * rayonSommet
            val posY = y + sin(anglesSommets[i]) * rayonSommet
            if (i == 0) path.moveTo(posX, posY) else path.lineTo(posX, posY)
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    fun takeDamage(): Boolean {
        health--
        return health <= 0
    }

    fun checkCollisionAvecJoueur(player: Player): Boolean {
        val distance = kotlin.math.hypot((player.x - x).toDouble(), (player.y - y).toDouble()).toFloat()
        return distance < (rayonHitbox + player.size / 2)
    }
}