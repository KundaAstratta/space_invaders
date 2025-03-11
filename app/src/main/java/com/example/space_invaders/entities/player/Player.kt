package com.example.space_invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.example.space_invaders.backgrounds.Background
import com.example.space_invaders.entities.ExplosionParticle
import com.example.space_invaders.entities.byakhee.Byakhee
import com.example.space_invaders.entities.deepone.DeepOne
import com.example.space_invaders.entities.deepone.IchorousBlast
import com.example.space_invaders.levels.shoggothLevel.MazeSystem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 200//20
    var isAlive = true
    private var lifeLossAnimation: LifeLossAnimation? = null

    private var pulseRadius = 0f
    private var pulseDirection = 1 // 1 pour expansion, -1 pour contraction
    private var pulseSpeed = 2f

    fun draw(canvas: Canvas, paint: Paint) {
        // ... (code pour la couleur en fonction des vies inchangé)
        val baseColor = when (lives) {
            3 -> Color.rgb(0, 255, 255)  // Cyan
            2 -> Color.rgb(255, 255, 0)  // Jaune
            1 -> Color.rgb(255, 0, 255)  // Magenta
            else -> Color.GREEN
        }

        // Dessiner le corps principal

        val saucerRadius = size / 2



        // Dessiner le cercle vert pulsant
        paint.color = baseColor
        canvas.drawCircle(x, y, pulseRadius, paint)

        // Mettre à jour le rayon du cercle pulsant
        pulseRadius += pulseSpeed * pulseDirection
        if (pulseRadius > size / 2) {
            pulseRadius = size / 2
            pulseDirection = -1
        } else if (pulseRadius < 0) {
            pulseRadius = 0f
            pulseDirection = 1
        }

        // Ajouter des points le long du bord
        paint.color = Color.GREEN
        val numPoints = 20 // Nombre de points à dessiner
        val angleIncrement = 2 * PI / numPoints
        // Ajouter des points scintillants le long du bord
        for (i in 0 until numPoints) {
            val angle = i * angleIncrement
            val pointX = x + cos(angle) * saucerRadius
            val pointY = y + sin(angle) * saucerRadius

            // Variation de la taille des points
            val pointRadius = Random.nextFloat() * 7f + 5f

            // Variation de la couleur des points
            val greenValue = Random.nextInt(150, 256)
            paint.color = Color.rgb(0, greenValue, 0)

            canvas.drawCircle(pointX.toFloat(), pointY.toFloat(), pointRadius, paint)
        }

        // Dessiner l'animation de perte de vie si elle existe
        lifeLossAnimation?.let { animation ->
            animation.draw(canvas, paint)
            animation.update()
            if (animation.isFinished()) {
                lifeLossAnimation = null
            }
        }

    }

    fun moveTo(
        newX: Float,
        newY: Float,
        screenWidth: Float,
        screenHeight: Float,
        structures: List<Background.Structure>,
        mazeSystem: MazeSystem? = null
    ) {
        var finalX = newX.coerceIn(size / 2, screenWidth - size / 2)
        var finalY = newY.coerceIn(size / 2, screenHeight - size / 2)

        // If in Shoggoth level with maze system, use maze-specific movement
        if (mazeSystem != null) {
            val movement = mazeSystem.getValidMovement(x, y, finalX, finalY, size / 2)
            finalX = movement.first
            finalY = movement.second
        } else {
            // Original structure collision check
            var canMove = true
            for (structure in structures) {
                if (structure.intersectsPlayerVersusStruct(finalX, finalY, size)) {
                    canMove = false
                    break
                }
            }

            if (!canMove) {
                return
            }
        }

        x = finalX
        y = finalY
        //++++
    }

    fun hit(): Boolean {
        lives--
        if (lives <= 0) {
            isAlive = false
        } else {
            // Créer une nouvelle animation de perte de vie
            lifeLossAnimation = LifeLossAnimation(x + size / 2, y - size / 2)
        }
        return !isAlive
    }

    fun intersectsByakhee(byakhee: Byakhee): Boolean {
        val distance = kotlin.math.hypot(x - byakhee.x - byakhee.width / 2, y - byakhee.y - byakhee.height / 2)
        return distance < size / 2 + kotlin.math.min(byakhee.width, byakhee.height) / 2
    }

    fun intersectsDeepOne(deepOne: DeepOne): Boolean {
        val distance = kotlin.math.hypot(x - deepOne.x - deepOne.width / 2, y - deepOne.y - deepOne.height / 2)
        return distance < size / 2 + kotlin.math.min(deepOne.width, deepOne.height) / 2
    }

    fun intersectsIchorousBlast(blast: IchorousBlast): Boolean {
        val distance = hypot(x - blast.x, y - blast.y)
        return distance < size / 2 + blast.radius
    }

    // Créer l'explosion du joueur
    fun createPlayerExplosion(explosions: MutableList<List<ExplosionParticle>>) {
        val rippleCount = 5
        val baseColor = Color.YELLOW

        val particles = List(rippleCount) {
            ExplosionParticle(
                x,
                y,
                initialSize = 20f,
                maxSize = 200f,
                growthRate = Random.nextFloat() * 5 + 2,
                color = baseColor
            )
        }
        explosions.add(particles)
    }


}