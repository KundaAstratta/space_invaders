package com.example.space_invaders.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.example.space_invaders.R
import com.example.space_invaders.audio.BackgroundSoundManager
import com.example.space_invaders.backgrounds.Background
import com.example.space_invaders.entities.Bullet
import com.example.space_invaders.entities.Byakhee
import com.example.space_invaders.entities.ExplosionParticle
import com.example.space_invaders.entities.dhole.Dhole
import com.example.space_invaders.entities.Player
import kotlin.random.Random

class SpaceInvadersView(context: Context, private val onGameOver: () -> Unit) : View(context) {


    private lateinit var player: Player
    private val bullets = mutableListOf<Bullet>()
    private var lastDeltaX = 0f
    private var lastDeltaY = 0f

    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private var level = 1
    private var isPlayerTouched = false
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    // Créer un objet Background
    private lateinit var background: Background

    //Créer un objet sound manager
    private val soundManager = BackgroundSoundManager(context)


    //Transition
    private var isLevelTransition = false
    private var transitionAlpha = 0
    private var levelNumberAlpha = 0
    private val transitionDuration = 180 // Temps des transitions en millisecondes
    private var transitionCounter = 0

    // Byakhee related variables
    private val byakhees = mutableListOf<Byakhee>()
    private var speedMultiplier = 1f
    private val explosions = mutableListOf<List<ExplosionParticle>>()
    private val maxByakheeLevels = 2 // Définissez le nombre de niveaux Byakhee souhaités
    private val numByakheeRows = 3  //4  lignes //ATTENTION
    private val numByakheeCols = 5
    private val byakheePadding = 10f
    private var byakheeWidth = 0f
    private var byakheeHeight = 0f
    private var byakheesDestroyed = 0
    private val duplicationInterval = 5000L // Intervalle de duplication en millisecondes (5 secondes)
    private var lastDuplicationTime = 0L // Temps de la dernière duplication

    // Dhole related variable
    private var dhole: Dhole? = null

    // Variable globale pour le score
    private var score = 0

    // Liste pour stocker les scores temporaires à afficher
    private val temporaryScores = mutableListOf<TemporaryScore>()

    // Classe pour représenter un score temporaire
    data class TemporaryScore(
        val x: Float,
        val y: Float,
        var alpha: Int = 255,
        var yOffset: Float = 0f
    )

    init {
        // L'initialisation se fera dans onSizeChanged
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.isAntiAlias = true // ???
    }

    //initialiser et adapter les éléments du jeu (comme le joueur, les ennemis,
    // et le fond d'écran) en fonction des dimensions de l'écran
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        screenWidth = w.toFloat()
        screenHeight = h.toFloat()

        // Calculer la taille des ennemis
        byakheeWidth = (screenWidth - (numByakheeCols + 1) * byakheePadding) / numByakheeCols
        byakheeHeight = byakheeWidth / 2 // Ajustez le ratio selon vos préférences

        // Initialiser le joueur
        val playerSize = screenWidth / 10
        player = Player(screenWidth / 2, screenHeight - playerSize, playerSize)
        resetPlayerPosition()

        // Initialiser le fond d'écran
        background = Background(context, screenWidth, screenHeight)

        // Initialiser les ennemis du niveau Byakhee
        createByakhees()

        // Jouer un son de grésillement différent selon le niveau
        soundManager.playSound(R.raw.tv_static)
    }



    //Dessiner tous les éléments du jeu sur l'écran
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        background.draw(canvas)

        if (!isLevelTransition) {
            // Dessin normal du jeu
            if (player.isAlive) {
                player.draw(canvas, paint)
            }
            byakhees.forEach { it.draw(canvas, paint) }
            bullets.forEach { it.draw(canvas, paint) }
            dhole?.draw(canvas, paint)

            explosions.forEach { explosion ->
                explosion.forEach { it.draw(canvas, paint) }
            }
        }

        // Dessin de l'effet de transition
        if (isLevelTransition) {
            // Assombrissement de l'écran
            paint.color = Color.argb(transitionAlpha, 0, 0, 0)
            canvas.drawRect(0f, 0f, screenWidth, screenHeight, paint)

            // Affichage du numéro de niveau
            if (levelNumberAlpha > 0) {
                paint.color = Color.argb(levelNumberAlpha, 255, 255, 255)
                paint.textSize = screenHeight / 10 // Taille du texte en fonction de la hauteur de l'écran
                paint.textAlign = Paint.Align.CENTER
                var levelText = "STAGE $level"
                canvas.drawText(levelText, screenWidth / 2, screenHeight / 2, paint)
                when (level) {
                    2 -> levelText = "Byakhee"
                    3 -> levelText = "Dhole"
                }
                canvas.drawText(levelText, screenWidth / 2,  screenHeight / 3, paint)
            }
        }

        // Dessiner les scores temporaires
        temporaryScores.forEach { tempScore ->
            paint.color = Color.argb(tempScore.alpha, 255, 255, 255)
            paint.textSize = 150f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("+1", tempScore.x, tempScore.y - tempScore.yOffset, paint)
        }

        update()
        invalidate()

    }

    //Mettre à jour la position du jour entre chaque stage
    private fun resetPlayerPosition() {
        val playerSize = screenWidth / 10
        player.x = screenWidth / 2
        player.y = screenHeight - playerSize
    }

    //Créer l'explosion du byakhee
    private fun createExplosion(x: Float, y: Float, width: Float, height: Float) {
        val baseColor = Color.rgb(57, 255, 20)  // Green color for the ripple effect
        val maxRipples = 10  // Number of ripple effects

        val particles = List(maxRipples) {
            ExplosionParticle(
                x + Random.nextFloat() * width,
                y + Random.nextFloat() * height,
                initialSize = 10f,  // Start small
                maxSize = 150f,  // Max size for the ripple
                growthRate = Random.nextFloat() * 5 + 2,  // Random growth rate
                color = baseColor
            )
        }
        explosions.add(particles)
    }

    //Créer l'explosion du joueur
    private fun createPlayerExplosion() {
        val rippleCount = 5  // Number of ripples
        val baseColor = Color.YELLOW  // Color for the player's explosion ripples

        val particles = List(rippleCount) {
            ExplosionParticle(
                player.x,
                player.y,
                initialSize = 20f,  // Start small
                maxSize = 200f,  // Max size for the ripple
                growthRate = Random.nextFloat() * 5 + 2,  // Random growth rate
                color = baseColor
            )
        }
        explosions.add(particles)
    }

    //Check si le joueur est mort
    private fun checkGameOver() {
        if (!player.isAlive) {
            createPlayerExplosion()
            postDelayed({
                onGameOver()
                onDestroy()  // Appeler onDestroy pour arrêter le son lorsque le jeu se termine
            }, 1000) // x secondes de délai
        }
    }

    // Mettre à jour l'état du jeu
    private fun update() {
        if (isLevelTransition) {
            // Mettre à jour l'effet de transition
            handleLevelTransition()
        } else {
            bullets.forEach { it.move() }

            // Vérification des collisions des bullets avec les structures

            val bulletsToRemove = mutableListOf<Bullet>()  // Créer une liste temporaire pour les bullets à supprimer

            bullets.forEach { bullet ->
                // Vérifier les collisions avec les structures
                if (background.structures.any { structure ->
                        structure.intersectsBulletVersusStruct(bullet.x, bullet.y, bullet.maxRadius * 2)
                    }) {
                    bulletsToRemove.add(bullet)  // Ajouter la bullet à la liste des bullets à supprimer
                }
            }

            // Supprimer les bullets qui ont touché une structure
            bullets.removeAll(bulletsToRemove)
            // Supprimer les balles hors de l'écran
            bullets.removeAll { it.y < 0 }

            // Mettre à jour les ennemis
            updateByakhee()
            updateDhole()

            //explosion
            explosions.forEach { explosion ->
                explosion.forEach { it.update() }
            }
            explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

            // Vérifier si le niveau est terminé
            if (byakhees.isEmpty() && dhole == null) {
                startNextLevel()
            }
        }

        // Mettre à jour les scores temporaires
        temporaryScores.forEach {
            it.alpha -= 5 // Diminuer l'alpha pour l'effet de disparition
            it.yOffset += 2f // Déplacer le score vers le haut
        }
        temporaryScores.removeAll { it.alpha <= 0 } // Supprimer les scores qui ont disparu

    }

    // Mettre à jour le niveau
    private fun startNextLevel() {
        level++
        speedMultiplier = 1f
        byakheesDestroyed = 0

        // Réinitialiser la position du joueur
        resetPlayerPosition()

        // Réinitialiser les explosions
        explosions.clear()

        // Réinitialiser les balles
        bullets.clear()

        isLevelTransition = true
        transitionCounter = 0
        transitionAlpha = 0
        levelNumberAlpha = 0

        // Jouer un son de grésillement différent selon le niveau
        when  {
            level <= maxByakheeLevels  -> soundManager.playSound(R.raw.tv_static)
            else -> soundManager.playSound(R.raw.tv_static)
        }

        // Stage suivant
        when {
            level <= maxByakheeLevels -> {
                createByakhees()
                background.switchBackground(Background.BackgroundType.COSMIC_HORROR_RUINS)
            }
            else -> {
                createDhole()
                background.switchBackground(Background.BackgroundType.DHOLE_REALM)
            }
        }
    }

    // Méthode à appeler pour libérer les ressources lorsque le jeu se termine ou est détruit
    fun onDestroy() {
        soundManager.release()
    }

    // Mettre à jour l'effet de transition
    private fun handleLevelTransition() {
        transitionCounter++

        when {
            transitionCounter < transitionDuration / 3 -> {
                // Assombrissement de l'écran
                transitionAlpha = (255 * transitionCounter / (transitionDuration / 3)).coerceAtMost(255)
            }
            transitionCounter < 2 * transitionDuration / 3 -> {
                // Affichage du numéro de niveau
                levelNumberAlpha = 255
            }
            transitionCounter < transitionDuration -> {
                // Disparition du numéro de niveau et éclaircissement de l'écran
                levelNumberAlpha = (255 * (1 - (transitionCounter - 2 * transitionDuration / 3) / (transitionDuration / 3))).coerceAtLeast(0)
                transitionAlpha = levelNumberAlpha
            }
            else -> {
                // Fin de la transition
                isLevelTransition = false
                resetPlayerPosition()
                when {
                    level <= maxByakheeLevels -> {
                        createByakhees()
                        background.switchBackground(Background.BackgroundType.COSMIC_HORROR_RUINS)
                    }
                    else -> {
                        createDhole()
                        background.switchBackground(Background.BackgroundType.DHOLE_REALM)
                    }
                }
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        val touchRadius = dpToPx(context, 200f) // Rayon de contrôle autour du joueur
        if (!player.isAlive) return true // Ignorer les touches si le joueur est mort

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // Vérifier si le toucher commence près du joueur
                val distanceToPlayer = kotlin.math.hypot(event.x - player.x, event.y - player.y)
                if (distanceToPlayer <= player.size / 2 + touchRadius) {
                    isPlayerTouched = true
                    touchOffsetX = player.x - event.x
                    touchOffsetY = player.y - event.y
                }
            }
            MotionEvent.ACTION_MOVE -> {
                // Déplacer le joueur seulement si le toucher a commencé près de lui
                if (isPlayerTouched) {                val newX = event.x + touchOffsetX
                    val newY = event.y + touchOffsetY
                    lastDeltaX = newX - player.x
                    lastDeltaY = newY - player.y
                    player.moveTo(newX, newY, screenWidth, screenHeight, background.structures)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (isPlayerTouched) {
                    // Tirer seulement si le toucher est relâché près du joueur
                    val distanceToPlayer = kotlin.math.hypot(event.x - player.x, event.y - player.y)
                    if (distanceToPlayer <= player.size / 2 + touchRadius) {
                        bullets.add(Bullet(player.x, player.y - player.size / 2,0f,-1f))
                    }
                    isPlayerTouched = false
                }
            }


        }

        //Autres fonctionnalités de mouvement et tir

        return true
    }

    private fun isTouchInsideCircle(touchX: Float, touchY: Float, circleX: Float, circleY: Float, radius: Float): Boolean {
        val dx = touchX - circleX
        val dy = touchY - circleY
        return dx * dx + dy * dy <= radius * radius
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

    // Code spécifique aux Byakhee
    // Code spécifique aux Byakhee
    // Code spécifique aux Byakhee
    private fun createByakhees() {
        byakhees.clear()
        val startX = (screenWidth - (numByakheeCols * (byakheeWidth + byakheePadding) - byakheePadding)) / 2
        val startY = screenHeight / 4

        for (row in 0 until numByakheeRows) {
            for (col in 0 until numByakheeCols) {
                val x = startX + col * (byakheeWidth + byakheePadding)
                val y = startY + row * (byakheeHeight + byakheePadding)
                byakhees.add(Byakhee(x, y, byakheeWidth, byakheeHeight, level))
            }
        }
    }

    private fun duplicateByakhee() {
        val byakheesToDuplicate = byakhees.filter { Random.nextFloat() < 0.2f } // 20% de chance de duplication pour chaque ennemi

        for (byakhee in byakheesToDuplicate) {
            val newByakhee = Byakhee(byakhee.x, byakhee.y, byakhee.width, byakhee.height, level)
            newByakhee.hitsToDestroy = byakhee.hitsToDestroy  // Copie les points de vie
            byakhees.add(newByakhee)
        }
    }

    private fun updateByakhee() {
        // Mettre à jour les ennemis
        byakhees.forEach { byakhee ->
            byakhee.move(screenWidth, screenHeight, speedMultiplier, background.structures)
            byakhee.changeDirection(speedMultiplier)
        }

        val bulletsToRemove = mutableListOf<Bullet>()
        val byakheeToRemove = mutableListOf<Byakhee>()

        // Vérifier les collisions des ennemis avec le player
        for (byakhee in byakhees) {
            if (player.intersects(byakhee)) {
                if (player.hit()) {
                    createPlayerExplosion()
                    checkGameOver()
                }
                byakhees.remove(byakhee)
                break
            }
        }

        // Vérifier les collisions des ennemis avec les balles
        for (byakhee in byakhees) {
            for (bullet in bullets) {
                if (bullet.intersects(byakhee)) {
                    if (byakhee.hit()) {
                        createExplosion(byakhee.x, byakhee.y, byakhee.width, byakhee.height)
                        byakheeToRemove.add(byakhee)
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        byakhees.removeAll { it in byakheeToRemove }
        bullets.removeAll { it in bulletsToRemove }

        // Incrémenter le score et ajouter un score temporaire pour chaque Byakhee détruit
        byakheeToRemove.forEach {
            score++
            temporaryScores.add(TemporaryScore(it.x + it.width / 2, it.y + it.height / 2))
        }

        // Augmentation de la vitesse des ennemis
        if (byakheesDestroyed > 0 && byakheesDestroyed % 5 == 0) {
            speedMultiplier *= 1.1f // Augmentation de 10%
            byakheesDestroyed++
        }

        // Duplication des ennemis
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastDuplicationTime > duplicationInterval) {
            duplicateByakhee()
            lastDuplicationTime = currentTime
        }
    }

    // Code spécifique au Dhole
    // Code spécifique au Dhole
    // Code spécifique au Dhole
    private fun createDhole() {
        dhole = Dhole(screenWidth / 2, screenHeight / 4, screenWidth / 10)
    }

    private fun updateDhole() {
        dhole?.let { currentDhole ->
            currentDhole.move(screenWidth, screenHeight)
            if (currentDhole.intersectsPlayer(player)) {
                if (player.hit()) {
                    createPlayerExplosion()
                    checkGameOver()
                }
                // Ajouter un petit délai d'invincibilité pour éviter des hits multiples trop rapides
                currentDhole.setInvincibilityFrame()
            }

            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { bullet ->
                if (currentDhole.hit(bullet.x, bullet.y)) {
                    bulletsToRemove.add(bullet)
                    if (currentDhole.isDestroyed()) {
                        dhole = null
                        // Incrémenter le score et ajouter un score temporaire lorsque le Dhole est touché
                        score++
                        temporaryScores.add(TemporaryScore(bullet.x, bullet.y))
                        // Le jeu se termine après avoir vaincu le Dhole
                        postDelayed({ onGameOver() }, 1000) // Délai d'une seconde avant de terminer le jeu
                    }
                }
            }
            bullets.removeAll(bulletsToRemove)
        }
    }

}
