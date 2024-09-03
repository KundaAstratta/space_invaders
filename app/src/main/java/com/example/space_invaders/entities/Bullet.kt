package com.example.space_invaders.entities

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.entities.byakhee.Byakhee
import kotlin.math.sin

class Bullet(var x: Float, var y: Float, private val directionX: Float, private val directionY: Float) {
    private val speed = 15f
    private val baseRadius = 15f
    private var time = 0f
    private var angle = 0f // Angle de rotation


    val maxRadius: Float
        get() = baseRadius + 5

    // Méthode pour dessiner les cercles animés
    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.rgb(57, 255, 20)

        // Calculer la taille des cercles en fonction du temps
        val radius1 = baseRadius + sin(time) * 5
        val radius2 = baseRadius + sin(time + 1) * 5
        val radius3 = baseRadius + sin(time + 2) * 5
        val radius4 = baseRadius + sin(time + 3) * 5

        // Dessiner les quatre cercles autour de la position de la balle
        canvas.save()
        canvas.translate(x, y)
        canvas.rotate(angle) // Rotation de la balle
        canvas.drawCircle(0f, 0f, radius1, paint)
        canvas.drawCircle(-25f, 0f, radius2, paint)
        canvas.drawCircle(25f, 0f, radius3, paint)
        canvas.drawCircle(0f, -25f, radius4, paint)
        canvas.restore()

        // Incrémenter le temps et l'angle pour animer les cercles et la rotation
        time += 0.1f
        angle += 10f // Angle de rotation de la balle
    }

    // Méthode pour déplacer la bullet
    fun move() {
        x += directionX * speed
        y += directionY * speed
    }

    // Méthode pour vérifier les collisions avec un ennemi Byakhee
    fun intersects(byakhee: Byakhee): Boolean {
        val radius = maxRadius
        return (x + radius > byakhee.x && x - radius < byakhee.x + byakhee.width) &&
                (y + radius > byakhee.y && y - radius < byakhee.y + byakhee.height)
    }
}
