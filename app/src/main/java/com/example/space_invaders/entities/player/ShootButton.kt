package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RadialGradient
import android.graphics.Shader
import kotlin.math.abs

class ShootButton(private val x: Float, private val y: Float, private val size: Float) {
    private val radius = size / 2
    private val arrowSize = size / 4

    fun draw(canvas: Canvas, paint: Paint) {
        // Créer un dégradé radial pour l'effet 3D
        val colors = intArrayOf(
            Color.rgb(100, 100, 100), // Couleur plus foncée sur les bords
            Color.LTGRAY,             // Gris clair au centre
            Color.rgb(50, 50, 50)      // Ombre au centre
        )
        val positions = floatArrayOf(0f, 0.8f, 1f)
        val gradient = RadialGradient(
            x + radius, y + radius, radius,
            colors, positions, Shader.TileMode.CLAMP
        )

        // Appliquer le dégradé au pinceau
        paint.shader = gradient

        // Dessiner le cercle principal
        paint.style = Paint.Style.FILL
        canvas.drawCircle(x + radius, y + radius, radius, paint)

        // Réinitialiser le shader pour les prochains dessins
        paint.shader = null

        // Dessiner les triangles
        paint.color = Color.YELLOW
        // Triangle du haut
        drawTriangle(canvas, paint, x + radius, y + radius / 2, 0f)
        // Triangle du bas
        drawTriangle(canvas, paint, x + radius, y + radius * 1.5f, 180f)
        // Triangle de gauche
        drawTriangle(canvas, paint, x + radius / 2, y + radius, 270f)
        // Triangle de droite
        drawTriangle(canvas, paint, x + radius * 1.5f, y + radius, 90f)
    }


    private fun drawTriangle(canvas: Canvas, paint: Paint, cx: Float, cy: Float, rotation: Float) {
        canvas.save()
        canvas.rotate(rotation, cx, cy)
        val path = Path()
        val halfBase = arrowSize / 2 // La moitié de la base du triangle
        val height = arrowSize * 0.866f // Hauteur du triangle équilatéral (√3/2 * base)

        path.moveTo(cx, cy - height) // Sommet
        path.lineTo(cx - halfBase, cy) // Coin inférieur gauche
        path.lineTo(cx + halfBase, cy) // Coin inférieur droit
        path.close()

        canvas.drawPath(path, paint)
        canvas.restore()
    }
    fun contains(touchX: Float, touchY: Float): Boolean {
        return (touchX >= x && touchX <= x + size &&
                touchY >= y && touchY <= y + size)
    }

    fun getShootDirection(touchX: Float, touchY: Float): ShootDirection {
        val centerX = x + radius
        val centerY = y + radius
        val dx = touchX - centerX
        val dy = touchY - centerY

        return when {
            abs(dx) > abs(dy) -> if (dx > 0) ShootDirection.RIGHT else ShootDirection.LEFT
            else -> if (dy > 0) ShootDirection.DOWN else ShootDirection.UP
        }
    }
}
