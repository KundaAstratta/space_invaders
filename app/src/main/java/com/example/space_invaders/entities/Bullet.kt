package com.example.space_invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlin.math.sin

class Bullet(var x: Float, var y: Float) {
    private val speed = 15f
    private val baseRadius = 15f
    private var time = 0f

    val maxRadius: Float
        get() = baseRadius + 5

    // Méthode pour dessiner les cercles animés
    fun draw(canvas: Canvas, paint: Paint) {
        // Couleur des cercles
        paint.color = Color.rgb(57, 255, 20)

        // Calculer la taille des cercles en fonction du temps
        val radius1 = baseRadius + sin(time) * 5
        val radius2 = baseRadius + sin(time + 1) * 5
        val radius3 = baseRadius + sin(time + 2) * 5
        val radius4 = baseRadius + sin(time + 3) * 5

        // Dessiner les quatre cercles autour de la position de la bullet
        canvas.drawCircle(x, y, radius1, paint)
        canvas.drawCircle(x - 25, y, radius2, paint)  // Décalé vers la gauche
        canvas.drawCircle(x + 25, y, radius3, paint)  // Décalé vers la droite
        canvas.drawCircle(x, y - 25, radius4, paint)  // Décalé vers le haut

        // Incrémenter le temps pour animer les cercles
        time += 0.1f
    }

    // Méthode pour déplacer la bullet

    fun move() {
        y -= speed
    }

    // Méthode pour vérifier les collisions avec un ennemi Byakhee
    fun intersects(byakhee: Byakhee): Boolean {
        val radius = maxRadius
        return (x + radius > byakhee.x && x - radius < byakhee.x + byakhee.width) &&
                (y + radius > byakhee.y && y - radius < byakhee.y + byakhee.height)
    }
}
