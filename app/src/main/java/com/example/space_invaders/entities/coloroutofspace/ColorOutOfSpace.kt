package com.example.space_invaders.entities.coloroutofspace

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.entities.player.Player
import kotlin.math.hypot
import kotlin.random.Random

class ColorOutOfSpace(
    var x: Float,
    var y: Float,
    private val player: Player,
    private val screenWidth: Int,
    private val screenHeight: Int
) {
    var size = player.size * 3
    private val pixels = mutableListOf<Pixel>()
    private var hue = Random.nextFloat() * 360f
    private var saturation = 1f
    private var brightness = 1f
    private var targetSize = size
    private var hitsToDestroy = 1

    init {
        generatePixels()
    }

    private fun generatePixels() {
        pixels.clear()
        val numPixels = 100
        for (i in 0 until numPixels) {
            val angle = Random.nextFloat() * 360f
            val distance = Random.nextFloat() * size / 2
            val pixelX = x + distance * Math.cos(Math.toRadians(angle.toDouble())).toFloat()
            val pixelY = y + distance * Math.sin(Math.toRadians(angle.toDouble())).toFloat()
            val pixelSize = Random.nextFloat() * 10 + 5
            pixels.add(Pixel(pixelX, pixelY, pixelSize))
        }
    }

    fun draw(canvas: Canvas, paint: Paint) {
        pixels.forEach { pixel ->
            paint.color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))
            canvas.drawRect(
                pixel.x - pixel.size / 2,
                pixel.y - pixel.size / 2,
                pixel.x + pixel.size / 2,
                pixel.y + pixel.size / 2,
                paint
            )
        }
    }

    fun move() {
        // Pulsation de la taille
        val sizeDifference = targetSize - size
        size += sizeDifference * 0.1f
        if (kotlin.math.abs(sizeDifference) < 0.1f) {
            targetSize = Random.nextFloat() * player.size * 3 + player.size
        }

        // Changement de couleur
        hue = (hue + 1) % 360

        // Déplacement vers le joueur
        val speed = 3f
        val dx = player.x - x
        val dy = player.y - y
        val distance = hypot(dx, dy)
        x += (dx / distance * speed).coerceIn((-screenWidth / 2).toFloat(), (screenWidth / 2).toFloat())
        y += (dy / distance * speed).coerceIn((-screenHeight / 2).toFloat(), (screenHeight / 2).toFloat())

        // Générer de nouveaux pixels si nécessaire
        if (Random.nextFloat() < 0.1f) {
            generatePixels()
        }
    }

    fun hit(): Boolean {
        hitsToDestroy--
        return hitsToDestroy <= 0
    }

    fun intersectsPlayer(player: Player): Boolean {
        val distance = hypot(x - player.x, y - player.y)
        return distance < size / 2 + player.size / 2
    }

    fun intersectsBullet(bulletX: Float, bulletY: Float): Boolean {
        val distance = hypot(x - bulletX, y - bulletY)
        return distance < size / 2
    }

    inner class Pixel(val x: Float, val y: Float, val size: Float)
}