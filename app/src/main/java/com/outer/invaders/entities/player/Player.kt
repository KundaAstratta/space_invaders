/*
package com.outer.invaders.entities.player


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.outer.invaders.backgrounds.Background
import com.outer.invaders.entities.ExplosionParticle
import com.outer.invaders.entities.byakhee.Byakhee
import com.outer.invaders.entities.deepone.DeepOne
import com.outer.invaders.entities.deepone.IchorousBlast
import com.outer.invaders.levels.shoggothLevel.MazeSystem
import com.outer.invaders.utils.Enums.BackgroundType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 200//20
    var isAlive = true
    var lifeLossAnimation: LifeLossAnimation? = null

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
        mazeSystem: MazeSystem? = null,
        currentLevel: BackgroundType = BackgroundType.DEFAULT//String =  "" // Add a parameter to identify the level
    ) {
        var finalX = newX.coerceIn(size / 2, screenWidth - size / 2)
        var finalY = newY.coerceIn(size / 2, screenHeight - size / 2)

        // If in ANTARTIC level, allow passing over structures
        if (currentLevel == BackgroundType.ANTARTIC) { //"ANTARTIC") {
            // Just use the constrained coordinates without structure collision check
            x = finalX
            y = finalY
            return
        }

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
/*
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
*/

    fun hit(livesToLose: Int = 1): Boolean {
        // Ne rien faire si le joueur est déjà mort
        if (!isAlive) {
            return false
        }

        lives -= livesToLose // Retire le nombre de vies spécifié

        if (lives <= 0) {
            lives = 0 // Empêche les vies négatives
            isAlive = false
            // On pourrait vouloir arrêter une animation en cours ou en créer une de mort ici
            lifeLossAnimation = null // Ou une animation de mort ?
            println("Player has died!") // Log de débogage
        } else {
            // Le joueur est toujours en vie
            isAlive = true // Assure que le flag est correct (normalement déjà le cas)
            // Créer ou mettre à jour l'animation de perte de vie
            // Attention: si le joueur perd 5 vies d'un coup, cela ne crée qu'une animation.
            // Si vous voulez 5 animations, la logique serait différente.
            lifeLossAnimation = LifeLossAnimation(x + size / 2, y - size / 2, livesToLose)
            println("Player hit, $lives lives remaining.") // Log de débogage
        }

        // Retourne l'état de vie actuel du joueur APRES avoir subi le coup
        return isAlive
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
*/
package com.outer.invaders.entities.player

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.outer.invaders.backgrounds.Background
import com.outer.invaders.entities.ExplosionParticle
import com.outer.invaders.entities.byakhee.Byakhee
import com.outer.invaders.entities.deepone.DeepOne
import com.outer.invaders.entities.deepone.IchorousBlast
import com.outer.invaders.levels.shoggothLevel.MazeSystem
import com.outer.invaders.utils.Enums.BackgroundType
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 300
    val maxLives = 300 // Ajout pour un calcul de pourcentage simple
    var isAlive = true
    var lifeLossAnimation: LifeLossAnimation? = null

    // NOUVEAU : Variables pour le nouveau design
    private var angleRotation: Float = 0f
    private val sceauPath = Path()
    private var tempsPulsation: Long = 0

    init {
        // Pré-calculer le chemin du sceau (ici, une étoile à 6 branches)
        val rayonSceau = size / 2
        sceauPath.moveTo(x, y - rayonSceau)
        for (i in 1..5) {
            val angle = i * 2 * PI / 6
            sceauPath.lineTo(
                x + (rayonSceau * sin(angle)).toFloat(),
                y - (rayonSceau * cos(angle)).toFloat()
            )
        }
        sceauPath.close()
    }

    fun draw(canvas: Canvas, paint: Paint) {
        if (!isAlive) return

        tempsPulsation += 16 // Simule le passage du temps pour l'animation de pulsation

        val healthPercentage = lives.toFloat() / maxLives

        // Vitesse de rotation et couleur du sceau dépendent de la vie
        val rotationSpeed = 0.5f + (1f - healthPercentage) * 2f // Tourne plus vite quand la vie est basse
        val sceauColor = when {
            healthPercentage > 0.5f -> Color.CYAN
            healthPercentage > 0.25f -> Color.YELLOW
            else -> Color.MAGENTA
        }

        // 1. Dessiner l'aura extérieure
        paint.style = Paint.Style.FILL
        val auraColor = Color.argb(
            (50 + (1f - healthPercentage) * 100).toInt(), // Plus opaque quand la vie est basse
            Color.red(sceauColor),
            Color.green(sceauColor),
            Color.blue(sceauColor)
        )
        paint.color = auraColor
        canvas.drawCircle(x, y, size / 2 * (1.2f + (1 - healthPercentage) * 0.5f), paint)


        // 2. Dessiner le sceau arcanique rotatif
        canvas.save()
        angleRotation = (angleRotation + rotationSpeed) % 360
        canvas.rotate(angleRotation, x, y)

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f + (1f - healthPercentage) * 4f // Plus épais quand la vie est basse
        paint.color = sceauColor

        // Dessine une étoile simple
        val radius = size / 2
        for(i in 0..5){
            val angleStart = i * 2 * PI / 3
            val angleEnd = (i + 1) * 2 * PI / 3
            canvas.drawLine(
                x + (radius * sin(angleStart)).toFloat(),
                y - (radius * cos(angleStart)).toFloat(),
                x + (radius * sin(angleEnd)).toFloat(),
                y - (radius * cos(angleEnd)).toFloat(),
                paint
            )
        }

        canvas.restore()


        // 3. Dessiner le noyau d'énergie central
        paint.style = Paint.Style.FILL
        val pulsation = sin(tempsPulsation * 0.05) * (size / 10f) // Le noyau pulse
        val coreRadius = (size / 4f) + pulsation.toFloat()

        // La couleur du noyau passe du blanc au rouge sang
        val coreColor = Color.rgb(
            255,
            (255 * healthPercentage).toInt(),
            (255 * healthPercentage).toInt()
        )
        paint.color = coreColor
        canvas.drawCircle(x, y, coreRadius, paint)


        // Dessiner l'animation de perte de vie si elle existe
        lifeLossAnimation?.let { animation ->
            animation.draw(canvas, paint)
            animation.update()
            if (animation.isFinished()) {
                lifeLossAnimation = null
            }
        }
    }

    // --- AUCUN CHANGEMENT DANS LA MÉCANIQUE CI-DESSOUS ---
    // Toutes les fonctions de mouvement, de collision et de vie restent identiques.

    fun moveTo(
        newX: Float,
        newY: Float,
        screenWidth: Float,
        screenHeight: Float,
        structures: List<Background.Structure>,
        mazeSystem: MazeSystem? = null,
        currentLevel: BackgroundType = BackgroundType.DEFAULT
    ) {
        var finalX = newX.coerceIn(size / 2, screenWidth - size / 2)
        var finalY = newY.coerceIn(size / 2, screenHeight - size / 2)

        if (currentLevel == BackgroundType.ANTARTIC) {
            x = finalX
            y = finalY
            return
        }

        if (mazeSystem != null) {
            val movement = mazeSystem.getValidMovement(x, y, finalX, finalY, size / 2)
            finalX = movement.first
            finalY = movement.second
        } else {
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
    }

    fun hit(livesToLose: Int = 1): Boolean {
        if (!isAlive) return false
        lives -= livesToLose
        if (lives <= 0) {
            lives = 0
            isAlive = false
            lifeLossAnimation = null
        } else {
            isAlive = true
            lifeLossAnimation = LifeLossAnimation(x, y - size / 2, livesToLose)
        }
        return isAlive
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

    fun createPlayerExplosion(explosions: MutableList<List<ExplosionParticle>>) {
        val rippleCount = 5
        val baseColor = Color.YELLOW
        val particles = List(rippleCount) {
            ExplosionParticle(x, y, initialSize = 20f, maxSize = 200f, growthRate = Random.nextFloat() * 5 + 2, color = baseColor)
        }
        explosions.add(particles)
    }
}
