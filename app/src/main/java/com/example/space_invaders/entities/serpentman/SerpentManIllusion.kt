package com.example.space_invaders.entities.serpentman

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import com.example.space_invaders.entities.player.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SerpentManIllusion(
    var x: Float,
    var y: Float,
    private val size: Float,
    private val player: Player
) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val bodyPath = Path()

    // Durée de vie et mouvement
    private var creationTime = System.currentTimeMillis()
    private val lifespan = 5000L // 5 secondes
    private val speed = 3f

    // Cible fixe
    private val targetX = player.x
    private val targetY = player.y
    private val angle = atan2(targetY - y, targetX - x)
    private val velocityX = cos(angle) * speed
    private val velocityY = sin(angle) * speed

    init {
        paint.style = Paint.Style.FILL
        // Effet de "distorsion" ou semi-transparent
        paint.alpha = 150
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.ADD)
    }

    fun update(): Boolean {
        move()
        // L'illusion disparaît après sa durée de vie
        return System.currentTimeMillis() - creationTime < lifespan
    }

    private fun move() {
        x += velocityX
        y += velocityY
    }

    fun draw(canvas: Canvas) {
        // Apparence similaire à l'original mais avec des couleurs "magiques"
        paint.color = Color.argb(paint.alpha, 150, 50, 255) // Violet/magenta
        bodyPath.reset()
        bodyPath.moveTo(x, y - size / 2)
        bodyPath.quadTo(x - size / 1.5f, y, x, y + size / 2)
        bodyPath.quadTo(x + size / 1.5f, y, x, y - size / 2)
        canvas.drawPath(bodyPath, paint)

        paint.color = Color.argb(paint.alpha, 0, 150, 150) // Cyan
        canvas.drawCircle(x, y - size / 4, size / 3, paint)

        paint.color = Color.WHITE
        canvas.drawCircle(x - size / 8, y - size / 3, size / 15, paint)
        canvas.drawCircle(x + size / 8, y - size / 3, size / 15, paint)
    }

    fun intersectsPlayer(player: Player): Boolean {
        val distance = kotlin.math.hypot((x - player.x).toDouble(), (y - player.y).toDouble()).toFloat()
        return distance < size / 2 + player.size / 2
    }

    fun isOffScreen(screenWidth: Float, screenHeight: Float): Boolean {
        return x < -size || x > screenWidth + size || y < -size || y > screenHeight + size
    }
}
