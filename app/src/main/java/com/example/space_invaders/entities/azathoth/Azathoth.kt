package com.example.space_invaders.entities.azathoth

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.entities.player.Player
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class Azathoth(
    val x: Float,
    var y: Float,
    val rayonMax: Float
) {
    private val paint = Paint().apply { isAntiAlias = true }
    private val particules = mutableListOf<ParticuleGalactique>()
    private var angleRotation: Float = 0f
    private val vitesseRotation: Float = 0.005f
    private var hueActuel: Float = 0f

    // AJOUT : Variables pour l'oscillation verticale
    private val yInitial: Float = y // On sauvegarde la position Y de départ
    private var tempsOscillation: Long = 0L // Un timer interne pour le mouvement
    private val vitesseOscillation: Float = 0.0004f // Vitesse du mouvement de haut en bas
    private val amplitudeOscillation: Float = 300f // Distance max du mouvement (60 pixels en haut, 60 en bas)


    init {
        repeat(3000) { creerParticule() }
    }

    private fun creerParticule(estNouvelle: Boolean = false) {
        val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
        val rayon = if (estNouvelle) Random.nextFloat() * rayonMax * 0.1f else Random.nextFloat() * rayonMax
        val angleBras = angle + (rayon / rayonMax) * 4f * Math.PI.toFloat()
        particules.add(ParticuleGalactique(angle = angleBras, rayon = rayon))
    }

    fun update(deltaTime : Long) {
        // --- LOGIQUE DE MOUVEMENT VERTICAL AJOUTÉE ---
        tempsOscillation += deltaTime
        // On calcule le décalage vertical avec une onde sinusoïdale
        val offsetY = sin(tempsOscillation * vitesseOscillation) * amplitudeOscillation
        // On met à jour la position Y d'Azathoth
        y = yInitial + offsetY
        // --- FIN DE L'AJOUT ---

        //val deltaTime = 1L
        angleRotation += vitesseRotation * deltaTime
        hueActuel = (hueActuel + 0.1f * deltaTime) % 360f

        val iter = particules.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.rayon += p.vitesseRadiale * (deltaTime / 16f)
            p.vie -= p.decroissanceVie * (deltaTime / 16f)
            if (p.rayon > rayonMax || p.vie <= 0f) {
                iter.remove()
            }
        }
        while (particules.size < 3000) {
            creerParticule(estNouvelle = true)
        }
    }

    fun draw(canvas: Canvas) {
        particules.forEach { p ->
            val angleTotal = p.angle + angleRotation
            val posX = x + cos(angleTotal) * p.rayon
            val posY = y + sin(angleTotal) * p.rayon
            val saturation = 0.6f + p.vie * 0.4f
            val luminosite = 0.5f + p.vie * 0.5f
            val alpha = (255 * p.vie).toInt()
            paint.color = Color.HSVToColor(alpha, floatArrayOf(hueActuel, saturation, luminosite))
            canvas.drawCircle(posX, posY, p.taille, paint)
        }
    }

    fun checkCollisionAvecJoueur(player: Player): Boolean {
        val distance = kotlin.math.hypot((player.x - x).toDouble(), (player.y - y).toDouble()).toFloat()
        return distance < (rayonMax * 0.8f + player.size / 2)
    }
}