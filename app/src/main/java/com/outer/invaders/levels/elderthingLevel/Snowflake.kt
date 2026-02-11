package com.outer.invaders.levels.elderthingLevel

import android.graphics.Color
import kotlin.random.Random

class Snowflake(
    var x: Float,
    var y: Float,
    val size: Float,
    private val speed: Float,
    private val swayFactor: Float,
    private val screenWidth: Float
) {
    private var time = Random.nextFloat() * 100f

    // Couleur verdâtre avec variation d'intensité pour chaque flocon
    val color = Color.rgb(
        200 + Random.nextInt(55),                  // Composante rouge
        220 + Random.nextInt(35),                  // Composante verte plus élevée
        200 + Random.nextInt(35)                   // Composante bleue
    )

    // Alpha variable pour différents niveaux de transparence
    val alpha = 150 + Random.nextInt(105)

    // Taille du halo (effet de lueur)
    val glowSize = size * (1.3f + Random.nextFloat() * 0.7f)

    fun update() {
        // Mettre à jour la position verticale
        y += speed

        // Ajouter un mouvement horizontal oscillant pour simuler le vent
        time += 0.05f
        x += Math.sin(time.toDouble()).toFloat() * swayFactor

        // S'assurer que le flocon reste dans les limites horizontales
        if (x < 0) x = 0f
        if (x > screenWidth) x = screenWidth
    }
}