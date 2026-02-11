package com.outer.invaders.game

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import com.outer.invaders.R
import com.outer.invaders.audio.BackgroundSoundManager
import com.outer.invaders.backgrounds.Background
import com.outer.invaders.entities.Bullet
import com.outer.invaders.entities.byakhee.Byakhee
import com.outer.invaders.entities.ExplosionParticle
import com.outer.invaders.entities.azathoth.Azathoth
import com.outer.invaders.entities.azathoth.ChaoticDancer
import com.outer.invaders.entities.coloroutofspace.ColorOutOfSpace
import com.outer.invaders.entities.cthulhu.Tentacle
import com.outer.invaders.entities.deepone.DeepOne
import com.outer.invaders.entities.dhole.Dhole
import com.outer.invaders.entities.elderthing.ElderProjectile
import com.outer.invaders.entities.elderthing.ElderThing
import com.outer.invaders.entities.nightgaunt.NightgauntSwarm
import com.outer.invaders.entities.player.HealthBar
import com.outer.invaders.entities.player.Player
import com.outer.invaders.entities.player.ShootButton
import com.outer.invaders.entities.player.ShootDirection
import com.outer.invaders.entities.serpentman.SerpentMan
import com.outer.invaders.entities.serpentman.SerpentManIllusion
import com.outer.invaders.entities.serpentman.SerpentManProjectile
import com.outer.invaders.entities.shoggoth.Shoggoth
import com.outer.invaders.game.activity.GameOverActivity
import com.outer.invaders.levels.rlyehLevel.CyclopeanStructure
import com.outer.invaders.levels.rlyehLevel.ScreenDistortionEffect
import com.outer.invaders.levels.shoggothLevel.MazeSystem
import com.outer.invaders.utils.Enums.BackgroundType

import kotlin.math.PI
import kotlin.math.hypot
import kotlin.random.Random

class SpaceInvadersView(context: Context, private val onGameOver: () -> Unit, private val onGameWin: () -> Unit) : View(context) {

    private lateinit var player: Player
    private val bullets = mutableListOf<Bullet>()
    private var lastDeltaX = 0f
    private var lastDeltaY = 0f

    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private var level = 1 // 8 TEST TEST the level you want TEST TEST
    private var isPlayerTouched = false
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    private var lastFrameTime: Long = 0L

    // Créer un objet Background
    private lateinit var background: Background

    //Créer un objet sound manager
    private val soundManager = BackgroundSoundManager(context)

    // Byakhee related variables
    private val byakhees = mutableListOf<Byakhee>()
    private var speedMultiplier = 1f
    private val explosions = mutableListOf<List<ExplosionParticle>>()
    private val maxByakheeLevels = 5// 1 REAL 5 Définissez le nombre de niveaux Byakhee souhaités
    private val numByakheeRows = 1//REAL 1  //  lignes //ATTENTION
    private val numByakheeCols = 5// 2 REAL 5
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
    // Nombre de DeepOnes à détruire pour gagner le niveau
    private val deepOneScoreToWin = 70//7 REAL 70
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
    //private val maxColorOutOfSpaceLevel = 5 // Définissez le niveau ColorOutOfSpace
    private var colorOutOfSpaceDestroyed = 0
    private var colorOutOfSpaceScoreToWin = 150// 3 REAL 100 //Nombre de ColorOutOgSapce à détruire pour gagner le niveau

    // Shoggoth related variables
    private val shoggoths = mutableListOf<Shoggoth>()
    private var shoggothSize = 0f
    private var shoggothScoreToWin = 10// 2 REAL 10  Nombre de Shoggoths à détruire
    private var shoggothsDestroyed = 0
    private lateinit var mazeSystem: MazeSystem

    // Ajouter ces variables pour le niveau Elder Thing
    private val elderThings = mutableListOf<ElderThing>()
    private var elderThingSize = 0f
    private var elderThingScoreToWin = 100 //10 REAL 100 Nombre d'Elder Things à détruire
    private var elderThingsDestroyed = 0

    // --- Variables spécifiques au niveau Nightgaunt ---
    private var nightgauntSwarm: NightgauntSwarm? = null
    private var nightgauntsDestroyed = 0
    private val nightgauntScoreToWin = 3000 //REAL 3000
    private var nightgauntSize = 0f // Sera initialisé dans onSizeChanged

    // --- Variables spécifiques au niveau Homme serpent ---
    private val serpentMen = mutableListOf<SerpentMan>()
    private val serpentManProjectiles = mutableListOf<SerpentManProjectile>()
    private val serpentManIllusions = mutableListOf<SerpentManIllusion>()
    private var serpentManSize = 0f
    private var serpentMenDestroyed = 0
    private var serpentMenAlive = 10 // 3 REAL 10
    private val serpentMenScoreToWin = 500 // 5 REAL 500   Nombre d'Hommes-Serpents à vaincre

    // --- Variables spécifiques au niveau Azathoth ---
    private var azathoth: Azathoth? = null
    private val dancers = mutableListOf<ChaoticDancer>()
    private val dancersMaxScreen = 25 //REAL 25
    private val dancersToWin = 1000//REAL 1000
    private var dancersKilled = 0

    // --- Variables pour la capture du joueur ---
    private var isPlayerCaptured = false
    private var captureStartTime = 0L
    private val captureDuration = 10000L // 10 secondes en millisecondes

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

    // initialiser et adapter les éléments du jeu (comme le joueur, les ennemis,
    // et le fond d'écran) en fonction des dimensions de l'écran
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        screenWidth = w.toFloat()
        screenHeight = h.toFloat()

        // Calculer la taille des ennemis
        byakheeWidth = (screenWidth - (numByakheeCols + 1) * byakheePadding) / numByakheeCols
        byakheeHeight = byakheeWidth / 2 // Ajustez le ratio selon vos préférences

        // Calculer la taille des Nightgaunts (basée sur la taille de base définie dans le Swarm)
        nightgauntSize = NightgauntSwarm.NIGHTGAUNT_BASE_SIZE * (screenWidth / 1080f) // Adapter à la largeur de l'écran


        // Initialiser le joueur
        val playerSize = screenWidth / 10
        player = Player(screenWidth / 2, screenHeight - playerSize, playerSize)
        resetPlayerPosition()

        // Initialiser le fond d'écran
        background = Background(context, screenWidth, screenHeight)

        mazeSystem = MazeSystem(screenWidth, screenHeight)

        // Initialiser les ennemis du premier niveau
        createByakhees()

        elderThingSize = screenWidth / 7
        serpentManSize = screenWidth / 9 // AJOUT: Taille des Hommes-Serpents


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

    }


    //Dessiner tous les éléments du jeu sur l'écran
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val currentTime = System.currentTimeMillis()
        val deltaTime = currentTime - lastFrameTime
        lastFrameTime = currentTime

        background.draw(canvas)

        // Dessiner le labyrinthe
        if (level == maxByakheeLevels + 4) {
            paint.color = Color.DKGRAY
            mazeSystem.draw(canvas, paint)
        }

        // Dessin normal du jeu
        if (player.isAlive) {
            player.draw(canvas, paint)
        }

        when {
            level <= maxByakheeLevels -> {
                byakhees.forEach { it.draw(canvas, paint) }
            }
            level == maxByakheeLevels + 1 -> {
                dhole?.draw(canvas, paint)
            }
            level == maxByakheeLevels + 2 -> {
                deepOnes.forEach { it.draw(canvas, paint) }
                tentacles.forEach { it.draw(canvas, tentaclePaint) }
            }
            level == maxByakheeLevels + 3 -> {
                colorOutOfSpaces.forEach { it.draw(canvas, paint) }
            }
            level == maxByakheeLevels + 4 -> {
                // Nouveau rendu pour le niveau Arkham
                shoggoths.forEach { it.draw(canvas) }
            }
            level == (maxByakheeLevels + 5) -> {
                // Dessiner les Elder Things
                elderThings.forEach { it.draw(canvas, paint) }
            }
            level == (maxByakheeLevels + 6) -> {
                // Dessiner les Nightgaunts
                nightgauntSwarm?.draw(canvas, paint)
            }
            // AJOUT: Dessin pour le niveau Homme-Serpent
            level == (maxByakheeLevels + 7) -> {
                serpentMen.forEach { it.draw(canvas) }
                serpentManProjectiles.forEach { it.draw(canvas) }
                serpentManIllusions.forEach { it.draw(canvas) }
            }
            level ==  (maxByakheeLevels + 8) -> {
                azathoth?.draw(canvas)
                dancers.forEach { it.draw(canvas) }
            }
        }

        bullets.forEach { it.draw(canvas, paint) }

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

        update(deltaTime)
        invalidate()

    }

    // Add this method to handle gaining a life
    fun playerGainLife() {
        //if (player.lives < 150) {
        //    player.lives++
        //    invalidate() // Force redraw to update health bar
        //}
        player.lives = (player.lives + 20).coerceAtMost(150)
        invalidate() // Force redrawto update health bar
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
    private fun update(deltaTime: Long) {

        val currentTime = System.currentTimeMillis()

        // --- Mettre à jour le joueur ---
        // Gérer la fin de la capture
        if (isPlayerCaptured && currentTime > captureStartTime + captureDuration) {
            isPlayerCaptured = false
            // Ajouter un effet pour montrer que le joueur est libéré ?
        }

        // Si le joueur est capturé, son mouvement est dicté par l'essaim
        if (isPlayerCaptured && nightgauntSwarm != null) {
            val (dx, dy) = nightgauntSwarm!!.getSwarmDeltaMovement()
            // Appliquer le mouvement de l'essaim au joueur, en le maintenant dans l'écran
            val capturedPlayerX = (player.x + dx).coerceIn(player.size / 2, screenWidth - player.size / 2)
            val capturedPlayerY = (player.y + dy).coerceIn(player.size / 2, screenHeight - player.size / 2)
            player.x = capturedPlayerX
            player.y = capturedPlayerY
        }

        bullets.forEach { it.move() }

        // Pour le niveau Shoggoth, vérifier les collisions avec les murs du labyrinthe
        if (level == maxByakheeLevels + 4) {
            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { bullet ->
                if (mazeSystem.checkBulletCollision(bullet.x, bullet.y)) {
                    bulletsToRemove.add(bullet)
                }
            }
            bullets.removeAll(bulletsToRemove)
        } else {
            // Vérification des collisions avec les structures existantes
            val bulletsToRemove = mutableListOf<Bullet>()
            bullets.forEach { bullet ->
                if (background.structures.any { structure ->
                        structure.intersectsBulletVersusStruct(bullet.x, bullet.y, bullet.maxRadius * 2)
                    }) {
                    bulletsToRemove.add(bullet)
                }
            }
            bullets.removeAll(bulletsToRemove)
        }

        // Supprimer les balles hors de l'écran
        bullets.removeAll { it.y < 0 }

        // Mettre à jour les ennemis
        when {
            level <= maxByakheeLevels -> updateByakhee()
            level == (maxByakheeLevels + 1) -> updateDhole()  // avant 3
            level == (maxByakheeLevels + 2) -> { //R'lyeh level (avant 4)
                screenDistortionEffect.update()
                updateDeepOne()
                updateTentacles()
            }
            level == (maxByakheeLevels + 3) -> {// avant 5
                updateColorOutOfSpace()
                screenDistortionEffect.stop() //  Au cas ou l'effet de distortion n'est pas stoppé
            }
            level == (maxByakheeLevels + 4) -> updateShoggoth()
            level == (maxByakheeLevels + 5) -> updateElderThing()
            level == (maxByakheeLevels + 6) -> updateNightgaunt()
            level == (maxByakheeLevels + 7) -> updateSerpentMen()
            level == (maxByakheeLevels + 8) -> updateAzathothLevel(deltaTime) // AJOUT
        }

        //explosion
        explosions.forEach { explosion ->
            explosion.forEach { it.update() }
        }
        explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

        // Vérifier si le niveau est terminé
        if (
            (level <= maxByakheeLevels && byakhees.isEmpty()) ||
            (level == (maxByakheeLevels + 1) && dhole == null) ||
            (level == (maxByakheeLevels + 2) && deepOneScore >= deepOneScoreToWin) ||
            (level == (maxByakheeLevels + 3) && colorOutOfSpaceDestroyed >= colorOutOfSpaceScoreToWin) ||
            (level == (maxByakheeLevels + 4) && shoggothsDestroyed >= shoggothScoreToWin) ||
            (level == (maxByakheeLevels + 5) && elderThingsDestroyed >= elderThingScoreToWin) ||
            (level == (maxByakheeLevels + 6) && nightgauntsDestroyed >= nightgauntScoreToWin) ||
            (level == (maxByakheeLevels + 7) && serpentMenDestroyed >= serpentMenScoreToWin) ||
            (level == (maxByakheeLevels + 8) && dancersKilled >= dancersToWin) // AJOUT
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
        shoggothsDestroyed = 0 // Compteur de Shoggoths détruits
        elderThingsDestroyed = 0 // Réinitialiser le compteur d'Elder Things détruits
        serpentMenDestroyed = 0 // AJOUT: Réinitialiser compteur Hommes-Serpents
        dancers.clear()
        dancersKilled = 0
        azathoth = null

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
        shoggoths.clear()
        elderThings.clear()
        serpentMen.clear() // AJOUT
        serpentManIllusions.clear() // AJOUT
        serpentManProjectiles.clear() // AJOUT

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
                background.switchBackground(BackgroundType.COSMIC_HORROR_RUINS)
            }
            level == (maxByakheeLevels + 1) -> {
                createDhole()
                background.switchBackground(BackgroundType.DHOLE_REALM)
            }
            level == (maxByakheeLevels + 2) -> {
                createDeepOnes()
                //deepOneScore = 0 // Réinitialiser le score DeepOne
                background.switchBackground(BackgroundType.RLYEH)
            }
            level == (maxByakheeLevels + 3) -> {
                screenDistortionEffect.stop() //  Au cas ou l'effet de distortion n'est pas stoppé
                createColorOutOfSpace()
                background.switchBackground(BackgroundType.COLOUR_OUT_OF_SPACE)
            }
            level == (maxByakheeLevels + 4) -> {
                createShoggoth()
                background.switchBackground(BackgroundType.LUNAR_SPACE)

                // Placer le joueur dans un corridor aléatoire
                val (playerX, playerY) = mazeSystem.getRandomCorridorPosition()
                player.x = playerX
                player.y = playerY
            }
            level == (maxByakheeLevels + 5) -> {
                createElderThings()
                //background.switchBackground(Background.BackgroundType.COLOUR_OUT_OF_SPACE) // Nous ajouterons ce type plus tard
                background.switchBackground(BackgroundType.ANTARTIC) // Nous ajouterons ce type plus tard
            }
            level == (maxByakheeLevels + 6) -> {
                createNightgaunt()
                background.switchBackground(BackgroundType.DREAMLANDS)
            }
            // AJOUT: Lancement du niveau Homme-Serpent
            level == (maxByakheeLevels + 7) -> {
                createSerpentMen()
                background.switchBackground(BackgroundType.SERPENT_TEMPLE)
            }
            level == (maxByakheeLevels + 8) -> { // AJOUT
                createAzathothLevel()
                background.switchBackground(BackgroundType.AZATHOTH_REALM)

                // AJOUT : Le repositionnement se fait UNIQUEMENT ici
                val playerSize = screenWidth / 10
                player.x = screenWidth / 2
                player.y = screenHeight - playerSize
            }
            else -> { // Tous les niveaux sont terminés
                //onGameOver() // Ou un écran de victoire
                onGameWin() // On déclenche la victoire !
                // On s'assure que le jeu s'arrête en attendant la transition
                //level = 999
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

                    val mazeSystemForLevel = if (level == maxByakheeLevels + 4) mazeSystem else null

                    player.moveTo(newX, newY, screenWidth, screenHeight, background.structures,mazeSystemForLevel,
                        background.backgroundType
                    )
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
        //bullets.add(Bullet(player.x, player.y, bulletVelocity.first, bulletVelocity.second))
        val newBullet = Bullet(player.x, player.y, bulletVelocity.first, bulletVelocity.second)

        // Check bullet collision only in Shoggoth level
        if (level == maxByakheeLevels + 4) {
            if (!mazeSystem.checkBulletCollision(newBullet.x, newBullet.y)) {
                bullets.add(newBullet)
            }
        } else {
            bullets.add(newBullet)
        }
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
                //updatePlayerHealth()
                playerLosesLife() // Perd 1 vie par défaut
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
                //updatePlayerHealth()
                playerLosesLife()
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
            if (background.backgroundType == BackgroundType.RLYEH) {
                val cyclopeanStructures = background.structures.filterIsInstance<CyclopeanStructure>()
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
                //updatePlayerHealth()
                playerLosesLife()
                deepOnes.remove(deepOne)
                break
            }
        }

        // Vérifier les collisions des IchorousBlasts avec le joueur

        for (deepOne in deepOnes) {
            for (blast in deepOne.ichorousBlasts) {
                if (player.intersectsIchorousBlast(blast)) {
                    //updatePlayerHealth()
                    playerLosesLife()
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
                //updatePlayerHealth()
                playerLosesLife()
                break
            }
        }

        tentacles.removeAll(tentaclesToRemove)
    }

    private fun spawnTentacle() {
        val startX = Random.nextFloat() * (screenWidth - 30)
        val startY = Random.nextFloat() * (screenHeight - 30)
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
                //updatePlayerHealth()
                playerLosesLife()
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

    //Code spécifique aux Shoggoth
    //Code spécifique aux Shoggoth
    //Code spécifique aux Shoggoth

    private fun createShoggoth() {
        shoggoths.clear()
        shoggothSize = screenWidth / 8

        while (shoggoths.size < 1) { //3
            val (x, y) = mazeSystem.getRandomCorridorPosition()
            //val shoggothSize = screenWidth / 8
            val newShoggoth = Shoggoth(x, y, shoggothSize,screenWidth,screenHeight)

            // Vérifier si le nouveau Shoggoth chevauche le joueur ou les Shoggoths existants

            if (player.intersectsShoggoth(newShoggoth) || shoggoths.any { it.intersectsShoggoth(newShoggoth) }) {
                continue // Réessayer s'il y a chevauchement
            }

            shoggoths.add(newShoggoth)

        }

    }

    private fun updateShoggoth() {
        val shoggothsToRemove = mutableListOf<Shoggoth>()

        for (shoggoth in shoggoths) {
            // Déplacer le Shoggoth
            shoggoth.move() // Assurez-vous que Shoggoth.kt a la méthode move(mazeSystem: MazeSystem)

            // Vérifier les collisions avec les balles
            for (bullet in bullets) {
                if (shoggoth.intersectsBullet(bullet.x, bullet.y)) {
                    if (shoggoth.hit()) {
                        shoggothsToRemove.add(shoggoth)
                        createExplosion(shoggoth.x, shoggoth.y, shoggoth.initialSize, shoggoth.initialSize)

                        score++
                        shoggothsDestroyed++
                        // TEST
                        playerGainLife() //Pour chaque Shoggots détruit j'ajoute 20 vies ???
                        // TEST

                        temporaryScores.add(TemporaryScore(shoggoth.x + shoggoth.initialSize / 2, shoggoth.y + shoggoth.initialSize / 2))

                        // Faire réapparaître un nouveau Shoggoth dans une case vide
                        // spawnShoggoth()

                        // Vérifier si 3 Shoggoths ont été détruits
                        // Si oui, reinitialiser le labyrinthe et repositionner le joueur et les shoggoths
                        if (shoggothsDestroyed % 2 == 0) { //3
                            // Réinitialiser le labyrinthe
                            mazeSystem = MazeSystem(screenWidth, screenHeight)

                            // Repositionner le joueur dans un nouveau corridor
                            val (playerX, playerY) = mazeSystem.getRandomCorridorPosition()
                            player.x = playerX
                            player.y = playerY

                            // Supprimer les Shoggoths restants
                            shoggoths.clear()

                            // Recréer de nouveaux Shoggoths dans le nouveau labyrinthe
                            while (shoggoths.size < 1) { //3
                                spawnShoggoth()
                            }
                        }

                    }
                    bullets.remove(bullet)
                    break // Sortir de la boucle des balles après une collision
                }
            }

            // Vérifier la collision avec le joueur
            if (player.intersectsShoggoth(shoggoth)) {
                //updatePlayerHealth()
                playerLosesLife()
                // Vous pouvez également envisager de faire réapparaître le Shoggoth ici ou d'ajouter un effet de recul
                break // Sortir de la boucle des Shoggoths après une collision avec le joueur
            }

        }

        shoggoths.removeAll(shoggothsToRemove)

        // Maintenir toujours 3 Shoggoths à l'écran
        while (shoggoths.size < 1) { //3
            spawnShoggoth()
        }

    }

    private fun spawnShoggoth() {
        var maxAttempts = 10
        while (maxAttempts > 0) {
            val (x, y) = mazeSystem.getRandomCorridorPosition()
            val newShoggoth = Shoggoth(x, y, shoggothSize, screenWidth, screenHeight)

            // Vérifier si le nouveau Shoggoth ne chevauche pas le joueur ou les Shoggoths existants
            if (!player.intersectsShoggoth(newShoggoth) && shoggoths.none { it.intersectsShoggoth(newShoggoth) }) {
                shoggoths.add(newShoggoth)
                return
            }
            maxAttempts--
        }
    }

    // Ajouter la méthode d'extension pour la collision shoggoth-shoggoth
    private fun Shoggoth.intersectsShoggoth(other: Shoggoth): Boolean {
        val distance = hypot(x - other.x, y - other.y)
        return distance < (initialSize / 2 + other.initialSize / 2)
    }

    // Ajouter la méthode d'extension pour la collision joueur-shoggoth
    private fun Player.intersectsShoggoth(shoggoth: Shoggoth): Boolean {
        val distance = kotlin.math.hypot(x - shoggoth.x, y - shoggoth.y)
        return distance < (size / 2 + shoggoth.initialSize / 2)
    }

    //Code spécifique aux Elder Things
    //Code spécifique aux Elder Things
    //Code spécifique aux Elder Things

    // Ajouter les méthodes pour le niveau Elder Thing
    private fun createElderThings() {
        elderThings.clear()

        // Créer plusieurs Elder Things répartis sur l'écran
        for (i in 0 until 5) {
            val x = Random.nextFloat() * (screenWidth - elderThingSize) + elderThingSize / 2
            val y = Random.nextFloat() * (screenHeight / 2) + elderThingSize / 2
            elderThings.add(ElderThing(x, y, elderThingSize, screenWidth, screenHeight))
        }
    }

    private fun updateElderThing() {
        // Mettre à jour les Elder Things
        elderThings.forEach { it.move() }

        val bulletsToRemove = mutableListOf<Bullet>()
        val elderThingsToRemove = mutableListOf<ElderThing>()

        // Vérifier les collisions avec les balles
        for (elderThing in elderThings) {
            for (bullet in bullets) {
                if (elderThing.intersectsBullet(bullet.x, bullet.y)) {
                    if (elderThing.hit()) {
                        elderThingsToRemove.add(elderThing)
                        createExplosion(elderThing.x, elderThing.y, elderThing.size, elderThing.size)

                        // Incrémenter le score et ajouter un score temporaire
                        score++
                        temporaryScores.add(TemporaryScore(elderThing.x, elderThing.y))

                        elderThingsDestroyed++

                        // Si un Elder Thing est détruit, en créer un nouveau après un délai
                        if (elderThingsDestroyed < elderThingScoreToWin) {
                            postDelayed({
                                if (elderThings.size < 5 && player.isAlive) {
                                    val x = Random.nextFloat() * (screenWidth - elderThingSize) + elderThingSize / 2
                                    val y = Random.nextFloat() * (screenHeight / 2) + elderThingSize / 2
                                    elderThings.add(ElderThing(x, y, elderThingSize, screenWidth, screenHeight))
                                }
                            }, 2000)
                        }
                    }
                    bulletsToRemove.add(bullet)
                    break
                }
            }

            // Vérifier les collisions avec le joueur
            if (player.intersectsElderThing(elderThing)) {
                //updatePlayerHealth()
                playerLosesLife()
                break
            }

            // Vérifier les collisions des projectiles avec le joueur
            for (projectile in elderThing.projectiles) {
                if (player.intersectsElderProjectile(projectile)) {
                    //updatePlayerHealth()
                    playerLosesLife()
                    elderThing.projectiles.remove(projectile)
                    break
                }
            }
        }

        elderThings.removeAll(elderThingsToRemove)
        bullets.removeAll(bulletsToRemove)
    }

    // Ajouter les méthodes d'extension pour les collisions
    private fun Player.intersectsElderThing(elderThing: ElderThing): Boolean {
        val distance = kotlin.math.hypot(x - elderThing.x, y - elderThing.y)
        return distance < (size / 2 + elderThing.size / 2)
    }

    private fun Player.intersectsElderProjectile(projectile: ElderProjectile): Boolean {
        val distance = kotlin.math.hypot(x - projectile.x, y - projectile.y)
        return distance < (size / 2 + projectile.radius)
    }

    //Code spécifique aux Nightgaunt
    //Code spécifique aux Nightgaunt
    //Code spécifique aux Nightgaunt
    private fun createNightgaunt() {
        nightgauntSwarm = NightgauntSwarm(screenWidth, screenHeight, player)
        nightgauntsDestroyed = 0
        isPlayerCaptured = false // S'assurer que le joueur n'est pas capturé au début
    }

    // Dans la classe SpaceInvadersView (fichier SpaceInvadersView.kt)

    /**
     * Met à jour la logique spécifique au niveau des Nightgaunts.
     * Gère le mouvement de l'essaim, les collisions, la capture du joueur,
     * le score, et la réapparition des essaims.
     */
    private fun updateNightgaunt() {

        // Vérifie si un essaim existe
        if (nightgauntSwarm != null) {
            // Utilisation de 'val swarm = nightgauntSwarm!!' est sûr ici car on vient de vérifier la nullité
            val swarm = nightgauntSwarm!!

            // 1. Mettre à jour l'essaim (mouvement, collisions internes, détection collision joueur)
            val (destroyedPositions, playerCollisionDetected) = swarm.update(bullets, isPlayerCaptured)

            // 2. Traiter les Nightgaunts détruits (score et animation)
            if (destroyedPositions.isNotEmpty()) {
                destroyedPositions.forEach { position ->
                    score += 1
                    nightgauntsDestroyed += 1
                    temporaryScores.add(TemporaryScore(position.first, position.second))
                }
                println("${destroyedPositions.size} nightgaunts destroyed this frame. Total: $nightgauntsDestroyed / $nightgauntScoreToWin")
            }

            // 3. Gérer la capture du joueur
            if (playerCollisionDetected && !isPlayerCaptured && player.isAlive) {
                println("Player captured by Nightgaunts!")
                playerLosesLife(5)
                isPlayerCaptured = true
                captureStartTime = System.currentTimeMillis()
                // soundManager.playSound(R.raw.capture_sound)
            }

            // 4. Gérer la réapparition de l'essaim s'il est vide
            if (swarm.isEmpty() && nightgauntsDestroyed < nightgauntScoreToWin && !isTransitioning) {
                println("Swarm destroyed, creating a new one immediately.")
                // Remplace l'essaim actuel (qui est vide) par un nouveau
                nightgauntSwarm = NightgauntSwarm(screenWidth, screenHeight, player)
            }

        }
        // 5. Else: Si nightgauntSwarm était null au début de la fonction
        else {
            // Cette partie s'exécute si aucun essaim n'existe.
            // Principalement pour la création initiale au début du niveau,
            // ou si l'essaim a été mis à null pour une raison quelconque.
            // Vérifie si les conditions pour créer un essaim sont réunies.
            if (nightgauntsDestroyed < nightgauntScoreToWin && (level == maxByakheeLevels + 6) && !isTransitioning) {
                println("Swarm is null. Creating initial or replacement swarm.")
                nightgauntSwarm = NightgauntSwarm(screenWidth, screenHeight, player)
            }
        }
    } // Fin de la fonction updateNightgaunt


    // AJOUT: Logique pour le niveau Homme-Serpent
    // AJOUT: Logique pour le niveau Homme-Serpent
    // AJOUT: Logique pour le niveau Homme-Serpent

    private fun createSerpentMen() {
        serpentMen.clear()
        serpentManIllusions.clear()
        serpentManProjectiles.clear()
        // Créer 3 Hommes-Serpents
        for (i in 0 until 3) {
            val x = (screenWidth / 4) * (i + 1)
            val y = screenHeight / 4
            serpentMen.add(SerpentMan(x, y, serpentManSize, screenWidth, screenHeight, player))
        }
    }

    private fun updateSerpentMen() {
        // --- Mise à jour des Hommes-Serpents ---
        val serpentMenToRemove = mutableListOf<SerpentMan>()
        for (serpentMan in serpentMen) {
            serpentMan.update()
            serpentMan.maybeAttack(serpentManProjectiles, serpentManIllusions)

            // Collision avec le joueur
            if (player.intersectsSerpentMan(serpentMan)) {
                playerLosesLife(2) // Collision directe plus punitive
                serpentMenToRemove.add(serpentMan)
                createExplosion(serpentMan.x, serpentMan.y, serpentMan.size, serpentMan.size)
            }
        }
        serpentMen.removeAll(serpentMenToRemove)


        // --- Mise à jour des projectiles ---
        val projectilesToRemove = mutableListOf<SerpentManProjectile>()
        for (projectile in serpentManProjectiles) {
            projectile.move()
            if (projectile.isOffScreen(screenWidth, screenHeight)) {
                projectilesToRemove.add(projectile)
            } else if (projectile.intersectsPlayer(player)) {
                playerLosesLife()
                projectilesToRemove.add(projectile)
            }
        }
        serpentManProjectiles.removeAll(projectilesToRemove)


        // --- Mise à jour des illusions ---
        val illusionsToRemove = mutableListOf<SerpentManIllusion>()
        for (illusion in serpentManIllusions) {
            if (!illusion.update() || illusion.isOffScreen(screenWidth, screenHeight)) {
                illusionsToRemove.add(illusion)
            } else if (illusion.intersectsPlayer(player)) {
                playerLosesLife(5) // Perd 5 points de score
                //temporaryScores.add(TemporaryScore(illusion.x, illusion.y))
                illusionsToRemove.add(illusion)
            }
        }
        serpentManIllusions.removeAll(illusionsToRemove)


        // --- Collision des balles du joueur avec les Hommes-Serpents ---
        val bulletsToRemove = mutableListOf<Bullet>()
        for (bullet in bullets) {
            for (serpentMan in serpentMen) {
                if (serpentMan.intersectsBullet(bullet.x, bullet.y, bullet.maxRadius)) {
                    bulletsToRemove.add(bullet)
                    if (serpentMan.hit()) {
                        serpentMenToRemove.add(serpentMan)
                        createExplosion(serpentMan.x, serpentMan.y, serpentMan.size, serpentMan.size)
                        score += 10 // Gagner 10 points
                        serpentMenDestroyed++
                        temporaryScores.add(TemporaryScore(serpentMan.x, serpentMan.y))
                    }
                    break // La balle ne peut toucher qu'un ennemi
                }
            }
        }
        bullets.removeAll(bulletsToRemove)
        serpentMen.removeAll(serpentMenToRemove)

        // Faire réapparaître les Hommes-Serpents pour maintenir le défi
        if (serpentMen.size < serpentMenAlive && serpentMenDestroyed < serpentMenScoreToWin) {
            postDelayed({
                if (level == maxByakheeLevels + 7 && serpentMen.size < serpentMenAlive) {
                    val x = Random.nextFloat() * (screenWidth - serpentManSize) + serpentManSize / 2
                    val y = Random.nextFloat() * (screenHeight / 2)
                    serpentMen.add(SerpentMan(x, y, serpentManSize, screenWidth, screenHeight, player))
                }
            }, 1500) // Délai de réapparition
        }
    }

    // AJOUT: Méthode d'extension pour la collision joueur <-> Homme-Serpent
    private fun Player.intersectsSerpentMan(serpentMan: SerpentMan): Boolean {
        val distance = kotlin.math.hypot(x - serpentMan.x, y - serpentMan.y)
        return distance < (size / 2 + serpentMan.size / 2)
    }


    // AJOUT : AZATHOTH
    // AJOUT : AZATHOTH
    // AJOUT : AZATHOTH
    private fun createAzathothLevel() {
        dancers.clear()
        dancersKilled = 0
        azathoth = Azathoth(screenWidth / 2, screenHeight / 2, screenWidth / 3f)
        repeat(dancersMaxScreen) {
            dancers.add(ChaoticDancer(screenWidth / 2, screenHeight / 2, player))
        }

    }

    private fun updateAzathothLevel(deltaTime: Long) {
        // 1. Mettre à jour Azathoth et les danseurs
        azathoth?.update(deltaTime)
        dancers.forEach { it.update(deltaTime) }

        // 3. Gérer les collisions
        // 3.1. Collision Joueur <-> Azathoth
        azathoth?.let {
            if (it.checkCollisionAvecJoueur(player)) {
                playerLosesLife(5)
                // On pourrait ajouter un effet de recul pour le joueur ici
            }
        }

        val danseurIter = dancers.iterator()
        while(danseurIter.hasNext()) {
            val danseur = danseurIter.next()

            // Joueur <-> Danseur
            if (danseur.checkCollisionAvecJoueur(player)) {
                playerLosesLife(2)
                danseurIter.remove() // Le danseur est détruit au contact
                continue // Passe au danseur suivant pour éviter d'autres vérifications
            }

            // Tirs du joueur <-> Danseur
            val bulletIter = bullets.iterator()
            while(bulletIter.hasNext()) {
                val bullet = bulletIter.next()
                if (hypot((bullet.x - danseur.x).toDouble(), (bullet.y - danseur.y).toDouble()) < danseur.rayonHitbox) {
                    bulletIter.remove()
                    if (danseur.takeDamage()) { // takeDamage renvoie true si le danseur est mort
                        danseurIter.remove()
                        dancersKilled++
                        score += 50
                        // temporaryScores.add(...)
                        break // Sort de la boucle des tirs, car le danseur est mort
                    }
                }
            }
        }

        // 3. Faire réapparaître les danseurs pour en avoir toujours 10
        while (dancers.size < dancersMaxScreen && dancersKilled < dancersToWin) {
            dancers.add(ChaoticDancer(screenWidth / 2, screenHeight / 2, player))
        }

    }





    // Modifiée pour accepter le nombre de vies à perdre
    private fun playerLosesLife(livesToLose: Int = 1) {
        if (!player.isAlive || isPlayerCaptured) return // Ne pas perdre de vie si déjà mort ou pendant la capture initiale

        if (player.hit(livesToLose)) { // La méthode hit gère la décrémentation et renvoie true si vivant
            // Ajouter un effet visuel/sonore de dégât
            // soundManager.playSound(R.raw.player_hit_sound) // Ajouter un son
            invalidate() // Mettre à jour la barre de vie immédiatement
        } else {
            // Le joueur est mort
            checkGameOver()
        }
    }



}
