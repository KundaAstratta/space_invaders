package com.example.space_invaders.entities.azathoth

import kotlin.random.Random

data class ParticuleGalactique(
    var angle: Float,
    var rayon: Float,
    var vitesseRadiale: Float = Random.nextFloat() * 0.5f + 0.2f,
    var taille: Float = Random.nextFloat() * 3f + 1f,
    var vie: Float = 1.0f,
    var decroissanceVie: Float = Random.nextFloat() * 0.003f + 0.001f
)