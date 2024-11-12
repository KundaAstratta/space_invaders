package com.example.space_invaders.game

import android.content.Context
import android.content.Intent
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
import com.example.space_invaders.entities.byakhee.Byakhee
import com.example.space_invaders.entities.ExplosionParticle
import com.example.space_invaders.entities.coloroutofspace.ColorOutOfSpace
import com.example.space_invaders.entities.cthulhu.Tentacle
import com.example.space_invaders.entities.deepone.DeepOne
import com.example.space_invaders.entities.dhole.Dhole
import com.example.space_invaders.entities.player.HealthBar
import com.example.space_invaders.entities.player.Player
import com.example.space_invaders.entities.player.ShootButton
import com.example.space_invaders.entities.player.ShootDirection
import com.example.space_invaders.game.activity.GameOverActivity
import com.example.space_invaders.levels.rlyehLevel.ScreenDistortionEffect
import kotlin.math.PI
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

    // Byakhee related variables
    private val byakhees = mutableListOf<Byakhee>()
    private var speedMultiplier = 1f
    private val explosions = mutableListOf<List<ExplosionParticle>>()
    private val maxByakheeLevels = 2 // Définissez le nombre de niveaux Byakhee souhaités
    private val numByakheeRows = 3//1  //  lignes //ATTENTION
    private val numByakheeCols = 5//2
    private val byakheePadding = 10f
    private var byakheeWidth = 0f
    private var byakheeHeight = 0f
    private var byakheesDestroyed = 0
    private val duplicationInterval = 5000L // Intervalle de duplication en millisecondes (5 secondes)
    private var lastDuplicationTime = 0L // Temps de la dernière duplication

    //DeepOne related variables
    private val deepOnes = mutableListOf<DeepOne>()
    private var lastSpawnTime: Long = 0
    private val spawnInterval: Long = 5000 // 5000 millisecondes = 5s
    private var deepOneScore = 0
    private val deepOneScoreToWin = 70//7 // Nombre de DeepOnes à détruire pour gagner le niveau
    private var deepOnesDestroyed = 0
    //Tentacles Chtulhu
    private val tentacles = mutableListOf<Tentacle>()
    private var tentaclePaint = Paint()
    //Distorsion
    private lateinit var screenDistortionEffect: ScreenDistortionEffect
    private val distortionPaint = Paint()

    // Dhole related variable
    private var dhole: Dhole? = null

    // ColorOutOfSpace related variables
    private val colorOutOfSpaces = mutableListOf<ColorOutOfSpace>()
    private val maxColorOutOfSpaceLevel = 5 // Définissez le niveau ColorOutOfSpace
    private var colorOutOfSpaceDestroyed = 0
    private var colorOutOfSpaceScoreToWin = 100 //Nombre de ColorOutOgSapce à détruire pour gagner le niveau


    //Transition
    private var transitionAlpha = 255 // Nouvelle variable pour l'alpha de la transition
    private var isTransitioning = false // Nouvelle variable pour indiquer si une transition est en cours


    // Variable globale pour le score
    private var score = 0

    //Health bar
    private lateinit var healthBar: HealthBar


    // Classe pour représenter un score temporaire
    data class TemporaryScore(
        val x: Float,
        val y: Float,
        var alpha: Int = 255,
        var yOffset: Float = 0f
    )

    // Liste pour stocker les scores temporaires à afficher
    private val temporaryScores = mutableListOf<TemporaryScore>()

    private lateinit var shootButton: ShootButton
    private val shootButtonSize = 250f // Taille du bouton de tir

    init {
        // L'initialisation se fera dans onSizeChanged
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f
        paint.isAntiAlias = true // ???
        tentaclePaint = Paint()
        tentaclePaint.style = Paint.Style.STROKE
        tentaclePaint.strokeCap = Paint.Cap.ROUND
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

        // Initialize health bar
        val healthBarWidth = shootButtonSize
        val healthBarHeight = 30f
        val healthBarX = 30f
        val healthBarY = screenHeight - healthBarHeight - 30f
        healthBar = HealthBar(healthBarX, healthBarY, healthBarWidth, healthBarHeight)

        // Initialiser le bouton de tir
        shootButton = ShootButton(
            screenWidth - shootButtonSize - 30f,
            screenHeight - shootButtonSize - 30f,
            shootButtonSize
        )

        //Distorsion
        screenDistortionEffect = ScreenDistortionEffect(screenWidth, screenHeight)
        distortionPaint.style = Paint.Style.FILL
        distortionPaint.color = Color.argb(100, 0, 255, 255)

        // Initialiser les ennemis ColorOutOfSpace
        if (level == maxColorOutOfSpaceLevel) {
            createColorOutOfSpace()
        }
    }


    //Dessiner tous les éléments du jeu sur l'écran
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        background.draw(canvas)

        // Dessin normal du jeu
        if (player.isAlive) {
            player.draw(canvas, paint)
        }
        byakhees.forEach { it.draw(canvas, paint) }
        bullets.forEach { it.draw(canvas, paint) }
        dhole?.draw(canvas, paint)
        deepOnes.forEach { it.draw(canvas, paint) }
        colorOutOfSpaces.forEach { it.draw(canvas, paint) }

        explosions.forEach { explosion ->
            explosion.forEach { it.draw(canvas, paint) }
        }

        // Dessiner les scores temporaires
        temporaryScores.forEach { tempScore ->
            paint.color = Color.argb(tempScore.alpha, 255, 255, 255)
            paint.textSize = 150f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("+1", tempScore.x, tempScore.y - tempScore.yOffset, paint)
        }

        // Draw health bar
        healthBar.draw(canvas, player.lives)

        // Dessiner le bouton de tir
        shootButton.draw(canvas, paint)

        // Dessiner les tentacules
        tentacles.forEach { it.draw(canvas, tentaclePaint) }

        // Dessiner l'effet de distorsion s'il est actif
        if (screenDistortionEffect.isActive()) {
            screenDistortionEffect.draw(canvas, distortionPaint)
        }

        // Appliquer la transition
        if (isTransitioning) {
            // Générer des couleurs HSV aléatoires
            val hue = Random.nextFloat() * 360f
            val saturation = 1f
            val brightness = 1f
            paint.color = Color.HSVToColor(floatArrayOf(hue, saturation, brightness))

            canvas.drawRect(0f, 0f, screenWidth, screenHeight, paint)
        }

        update()
        invalidate()

    }

    // Add this method to handle gaining a life
    fun playerGainLife() {
        if (player.lives < 15) {
            player.lives++
            invalidate() // Force redraw to update health bar
        }
    }

    // Modify the existing player.hit() calls to update the health bar
    private fun updatePlayerHealth() {
        if (player.hit()) {
            player.createPlayerExplosion(explosions)
            checkGameOver()
        }
        invalidate() // Force redraw to update health bar
    }

    //Mettre à jour la position du jour entre chaque stage
    private fun resetPlayerPosition() {
        val playerSize = screenWidth / 10
        player.x = screenWidth / 2
        player.y = screenHeight - playerSize
    }

    //Check si le joueur est mort
    private fun checkGameOver() {
        // Ajouter un effet temporaire pour indiquer la perte d'une vie
        if (!player.isAlive) {
            player.createPlayerExplosion(explosions)
            postDelayed({
                val intent = Intent(context, GameOverActivity::class.java) // Ou utilisez un fragment manager si vous utilisez des fragments
                intent.putExtra("finalScore", score)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                onGameOver()
                onDestroy()  // Appeler onDestroy pour arrêter le son lorsque le jeu se termine
            }, 1000) // x secondes de délai
        }
    }

    // Mettre à jour l'état du jeu
    private fun update() {

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
        when {
            level <= maxByakheeLevels -> updateByakhee()
            level == 3 -> updateDhole()
            level == 4 -> { //R'lyeh level
                screenDistortionEffect.update()
                updateDeepOne()
                updateTentacles()
            }
            level == 5 -> updateColorOutOfSpace()
        }

        //explosion
        explosions.forEach { explosion ->
            explosion.forEach { it.update() }
        }
        explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

        // Vérifier si le niveau est terminé
        if (
            (level <= maxByakheeLevels && byakhees.isEmpty()) ||
            (level == 3 && dhole == null) ||
            (level == 4 && deepOneScore >= deepOneScoreToWin) ||
            (level == 5 && colorOutOfSpaceDestroyed > colorOutOfSpaceScoreToWin)
        ) {
            startNextLevel()
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
        deepOneScore = 0 // Réinitialiser le score des DeepOnes
        colorOutOfSpaceDestroyed = 0 // Compteur de ColorOutOfSpace détruits

        isTransitioning = true // Démarrer la transition
        transitionAlpha = 255 // Réinitialiser l'alpha


        // Mettre à jour la barre de vie +1 ?
        playerGainLife()

        // Réinitialiser la position du joueur
        resetPlayerPosition()

        // Réinitialiser les explosions
        explosions.clear()

        // Réinitialiser les balles
        bullets.clear()

        // Réinitialiser les listes d'ennemis
        byakhees.clear()
        dhole = null // Réinitialiser le Dhole
        deepOnes.clear()
        deepOnes.forEach { it.ichorousBlasts.clear() } // Effacer les tirs de chaque DeepOne
        colorOutOfSpaces.clear()

        // Réinitialiser les tentacules
        tentacles.clear()

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
            level == 3 -> {
                createDhole()
                background.switchBackground(Background.BackgroundType.DHOLE_REALM)
            }
            level == 4 -> {
                createDeepOnes()
                //deepOneScore = 0 // Réinitialiser le score DeepOne
                background.switchBackground(Background.BackgroundType.RLYEH)
            }
            else -> {
                createColorOutOfSpace()
                background.switchBackground(Background.BackgroundType.COLOUR_OUT_OF_SPACE)
            }
        }

        // Animer l'alpha avec un postDelayed
        postDelayed({
            // ... (code existant pour réinitialiser les éléments du niveau)

            // Terminer la transition
            isTransitioning = false
            transitionAlpha = 0
        }, 1000) // Durée de la transition en millisecondes (1 seconde

    }

    // Méthode à appeler pour libérer les ressources lorsque le jeu se termine ou est détruit
    fun onDestroy() {
        soundManager.release()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        val touchRadius = dpToPx(context, 200f) // Rayon de contrôle autour du joueur
        if (!player.isAlive) return true // Ignorer les touches si le joueur est mort

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (shootButton.contains(event.x, event.y)) {
                    // L'utilisateur a touché le bouton de tir
                    return true
                }
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
                if (isPlayerTouched) {
                    val newX = event.x + touchOffsetX
                    val newY = event.y + touchOffsetY
                    lastDeltaX = newX - player.x
                    lastDeltaY = newY - player.y
                    player.moveTo(newX, newY, screenWidth, screenHeight, background.structures)
                }
            }

            MotionEvent.ACTION_UP -> {
                if (shootButton.contains(event.x, event.y)) {
                    // Déterminer la direction du tir basée sur la position du toucher dans le bouton
                    val direction = shootButton.getShootDirection(event.x, event.y)
                    shoot(direction)
                    return true
                }
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

    private fun shoot(direction: ShootDirection) {
        val bulletSpeed = 1f
        val bulletVelocity = when (direction) {
            ShootDirection.UP -> Pair(0f, -bulletSpeed)
            ShootDirection.DOWN -> Pair(0f, bulletSpeed)
            ShootDirection.LEFT -> Pair(-bulletSpeed, 0f)
            ShootDirection.RIGHT -> Pair(bulletSpeed, 0f)
        }
        bullets.add(Bullet(player.x, player.y, bulletVelocity.first, bulletVelocity.second))
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

        // Vérifier les collisions des Byakhee avec le player
        for (byakhee in byakhees) {
            if (player.intersectsByakhee(byakhee)) {
                updatePlayerHealth()
                byakhees.remove(byakhee)
                break
            }
        }

        // Vérifier les collisions des Byakhee avec les balles
        for (byakhee in byakhees) {
            for (bullet in bullets) {
                if (bullet.intersectsByakhee(byakhee)) {
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
                updatePlayerHealth()
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
                        //postDelayed({ onGameOver() }, 1000) // Délai d'une seconde avant de terminer le jeu
                    }
                }
            }
            bullets.removeAll(bulletsToRemove)
        }
    }


    // Code spécifique au DeepOne
    // Code spécifique au DeepOne
    // Code spécifique au DeepOne
    fun createDeepOnes() {
        // Logique pour créer les ennemis "deepOnes" en haut de l'écran
        // Vous devrez adapter cela en fonction de la façon dont vous avez implémenté la classe deepOne
        val deepOneWidth = screenWidth / 10
        val deepOneHeight = deepOneWidth
        val startX = Random.nextFloat() * (screenWidth - deepOneWidth)
        val startY = 0f // En haut de l'écran

        deepOnes.add(DeepOne(startX, startY, deepOneWidth, deepOneHeight,screenWidth,screenHeight))
    }

   // fun getDeepOnes(): List<DeepOne> = deepOnes

    fun updateDeepOne() {
        // Gestion du temps pour faire apparaître de nouveaux ennemis
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastSpawnTime > spawnInterval) {
            createDeepOnes()
            lastSpawnTime = currentTime
        }

        // Mettre à jour les ennemis
        deepOnes.forEach { deepOne ->
            deepOne.move()
            deepOne.attackPlayer(player) // Attaque du joueur toutes les 15 secondes

            // Vérifier si le DeepOne touche une structure cyclopéenne
            if (background.backgroundType == Background.BackgroundType.RLYEH) {
                val cyclopeanStructures = background.structures.filterIsInstance<Background.CyclopeanStructure>()
                for (structure in cyclopeanStructures) {
                    if (structure.intersectsDeepOneVsStruct(deepOne)) {
                        // Faire disparaître le DeepOne et le faire réapparaître près d'une autre structure au hasard
                        val randomStructure = cyclopeanStructures.random()
                        deepOne.x = randomStructure.centerX + Random.nextFloat() * randomStructure.size - randomStructure.size / 2
                        deepOne.y = randomStructure.centerY + Random.nextFloat() * randomStructure.size - randomStructure.size / 2
                        break
                    }
                }
            }
        }


        // Gestion des collisions de DeepOne avec les balles
        val bulletsToRemove = mutableListOf<Bullet>()
        val deepOnesToRemove = mutableListOf<DeepOne>()

        for (deepOne in deepOnes) {
            for (bullet in bullets) {
                if (bullet.intersectsDeepOne(deepOne)) {
                    if (deepOne.hit()) {
                        deepOnesToRemove.add(deepOne)
                        createExplosion(deepOne.x, deepOne.y, deepOne.width, deepOne.height)
                        deepOneScore++
                        // Incrémenter le score et ajouter un score temporaire
                        score++
                        temporaryScores.add(SpaceInvadersView.TemporaryScore(deepOne.x + deepOne.width / 2, deepOne.y + deepOne.height / 2))

                        deepOnesDestroyed++
                        if (deepOnesDestroyed == 6) {
                            screenDistortionEffect.start() // Démarrer l'effet de distorsion
                            triggerChaosEvent()
                            spawnTentacle()
                            deepOnesDestroyed = 0
                        }

                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        // Vérifier les collisions des DeepOne avec le player
        for (deepOne in deepOnes) {
            if (player.intersectsDeepOne(deepOne)) {
                updatePlayerHealth()
                deepOnes.remove(deepOne)
                break
            }
        }

        // Vérifier les collisions des IchorousBlasts avec le joueur

        for (deepOne in deepOnes) {
            for (blast in deepOne.ichorousBlasts) {
                if (player.intersectsIchorousBlast(blast)) {
                    updatePlayerHealth()
                    deepOne.ichorousBlasts.remove(blast) // Supprimer le blast après collision
                    break
                }
            }
        }

        deepOnes.removeAll(deepOnesToRemove)
        bullets.removeAll(bulletsToRemove)
    }

    private fun triggerChaosEvent() {
        deepOnesDestroyed = 0
        background.redistributeStructures()
    }

    // Code spécifique aux Tentacules
    // Code spécifique aux Tentacules
    // Code spécifique aux Tentacules
    fun updateTentacles() {
        // Mise à jour et vérification des collisions pour les tentacules
        val tentaclesToRemove = mutableListOf<Tentacle>()
        for (tentacle in tentacles) {
            tentacle.update()

            // Vérifier les collisions avec les balles
            for (bullet in bullets) {
                if (tentacle.intersectsPlayer(bullet.x, bullet.y, bullet.maxRadius)) {
                    if (tentacle.hit()) {
                        tentaclesToRemove.add(tentacle)
                    }
                    bullets.remove(bullet)
                    break
                }
            }

            // Vérifier la collision avec le joueur
            if (tentacle.intersectsPlayer(player.x, player.y, player.size / 2)) {
                updatePlayerHealth()
                break
            }
        }

        tentacles.removeAll(tentaclesToRemove)
    }

    private fun spawnTentacle() {
        val startX = Random.nextFloat() * screenWidth
        val startY = Random.nextFloat() * screenHeight
        val direction = Random.nextFloat() * 2 * PI.toFloat()
        val tentacle = Tentacle(startX, startY, screenWidth / 2, direction)
        tentacles.add(tentacle)
    }

    // Code spécifique aux ColorOutOfSpace
    // Code spécifique aux ColorOutOfSpace
    // Code spécifique aux ColorOutOfSpace
    private fun createColorOutOfSpace() {
        colorOutOfSpaces.clear()
        for (i in 0 until 1) { // Commencez avec un ennemi
            val x = Random.nextFloat() * (screenWidth - player.size * 3) + player.size * 1.5f
            val y = Random.nextFloat() * (screenHeight - player.size * 3) + player.size * 1.5f
            colorOutOfSpaces.add(ColorOutOfSpace(x, y, player, screenWidth.toInt(), screenHeight.toInt()))
        }
    }

    private fun updateColorOutOfSpace() {
        colorOutOfSpaces.forEach { it.move() }

        val bulletsToRemove = mutableListOf<Bullet>()
        val colorOutOfSpaceToRemove = mutableListOf<ColorOutOfSpace>()

        // Vérifier les collisions des ColorOutOfSpace avec le joueur
        for (colorOutOfSpace in colorOutOfSpaces) {
            if (colorOutOfSpace.intersectsPlayer(player)) {
                updatePlayerHealth()
                colorOutOfSpaceToRemove.add(colorOutOfSpace)
                createColorOutOfSpace()
                break
            }
        }

        // Vérifier les collisions des ColorOutOfSpace avec les balles
        for (colorOutOfSpace in colorOutOfSpaces) {
            for (bullet in bullets) {
                if (colorOutOfSpace.intersectsBullet(bullet.x, bullet.y)) {
                    if (colorOutOfSpace.hit()) {
                        colorOutOfSpaceToRemove.add(colorOutOfSpace)
                        createExplosion(colorOutOfSpace.x, colorOutOfSpace.y, colorOutOfSpace.size, colorOutOfSpace.size)
                        colorOutOfSpaceDestroyed++
                        if (colorOutOfSpaceDestroyed < colorOutOfSpaceScoreToWin) {
                            createColorOutOfSpace() // Créer un nouveau ColorOutOfSpace
                        } else {
                            // Le joueur a gagné, passer au niveau suivant ou terminer le jeu
                            startNextLevel()
                        }
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }
        }

        colorOutOfSpaces.removeAll(colorOutOfSpaceToRemove)
        bullets.removeAll(bulletsToRemove)

        // Incrémenter le score et ajouter un score temporaire pour chaque ColorOutOfSpace détruit
        colorOutOfSpaceToRemove.forEach {
            score++
            temporaryScores.add(TemporaryScore(it.x, it.y))
        }
    }


}
