package com.example.space_invaders
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random


class SpaceInvadersView(context: Context, private val onGameOver: () -> Unit) : View(context) {

    private lateinit var player: Player
    private val byakhees = mutableListOf<Byakhee>()
    private var dhole: Dhole? = null
    private val maxByakheeLevels = 3 // Définissez le nombre de niveaux Byakhee souhaités
    private val bullets = mutableListOf<Bullet>()
    private val paint = Paint()

    private var screenWidth = 0f
    private var screenHeight = 0f

    private val numByakheeRows = 1//4  lignes
    private val numByakheeCols = 5
    private val byakheePadding = 10f
    private var byakheeWidth = 0f
    private var byakheeHeight = 0f

    private var speedMultiplier = 1f
    private var byakheesDestroyed = 0

    private val explosions = mutableListOf<List<ExplosionParticle>>()

    private val duplicationInterval = 5000L // Intervalle de duplication en millisecondes (5 secondes)
    private var lastDuplicationTime = 0L // Temps de la dernière duplication

    private var level = 1


    private var isPlayerTouched = false
    private var touchOffsetX = 0f
    private var touchOffsetY = 0f

    // Créer un objet Background
    private lateinit var background: Background

    //Transition
    private var isLevelTransition = false
    private var transitionAlpha = 0
    private var levelNumberAlpha = 0
    private val transitionDuration = 180// Temps des transitions en millisecondes
    private var transitionCounter = 0

    init {
        // L'initialisation se fera dans onSizeChanged
    }

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

        // Initialiser le fond d'écran
        background = Background(context,screenWidth, screenHeight)

        // Initialiser les ennemis
        val startX = (screenWidth - (numByakheeCols * (byakheeWidth + byakheePadding) - byakheePadding)) / 2
        val startY = screenHeight / 4

        //TEST NIVEAU DHOLE

        for (row in 0 until numByakheeRows) {
            for (col in 0 until numByakheeCols) {
                val x = startX + col * (byakheeWidth + byakheePadding)
                val y = startY + row * (byakheeHeight + byakheePadding)
                byakhees.add(Byakhee(x, y, byakheeWidth, byakheeHeight, level))
            }
        }

        // TEST NIVEAU DHOLE
        //createDhole()

    }
/*
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        //canvas.drawColor(Color.BLACK)

        // Dessiner le fond d'écran en premier
        background.draw(canvas)

        if (player.isAlive) {
            player.draw(canvas, paint)
        }
        byakhees.forEach { it.draw(canvas, paint) }
        bullets.forEach { it.draw(canvas, paint) }
        dhole?.draw(canvas, paint)

        explosions.forEach { explosion ->
            explosion.forEach { it.draw(canvas, paint) }
        }

        update()
        invalidate()
    }
 */
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
            val levelText = "STAGE $level"
            canvas.drawText(levelText, screenWidth / 2, screenHeight / 2, paint)
        }
    }

    update()
    invalidate()
}
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

    private fun checkGameOver() {
        if (!player.isAlive) {
            // Attendez un peu pour que l'explosion soit visible
            createPlayerExplosion()
            postDelayed({
                onGameOver()
            }, 1000) // x secondes de délai
        }
    }

    private fun update() {

        if (isLevelTransition) {
            handleLevelTransition()
        } else {
            // Le reste du code d'update reste inchangé
            bullets.forEach { it.move() }

            // Vérifier les collisions des balles avec les structures
            val bulletsToDesrtoy= bullets.filter { bullet ->
                background.structures.any { structure ->
                    structure.intersectsBulletVersusStruct(bullet.x, bullet.y, bullet.width, bullet.height)
                }
            }
            bullets.removeAll(bulletsToDesrtoy)

            bullets.removeAll { it.y < 0 }

            byakhees.forEach { byakhee ->
                byakhee.move(screenWidth, screenHeight, speedMultiplier, background.structures)
                byakhee.changeDirection(speedMultiplier)
            }

            if (byakheesDestroyed > 0 && byakheesDestroyed % 5 == 0) {
                speedMultiplier *= 1.1f // Augmentation de 10%
                byakheesDestroyed++
            }

            val bulletsToRemove = mutableListOf<Bullet>()
            val byakheeToRemove = mutableListOf<Byakhee>()

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

            // Duplication des ennemis
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastDuplicationTime > duplicationInterval) {
                duplicateByakhee()
                lastDuplicationTime = currentTime
            }

            explosions.forEach { explosion ->
                explosion.forEach { it.update() }
            }
            explosions.removeAll { explosion -> explosion.all { !it.isAlive() } }

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
                            // Le jeu se termine après avoir vaincu le Dhole
                            postDelayed({ onGameOver() }, 1000) // Délai d'une seconde avant de terminer le jeu
                        }
                    }
                }
                bullets.removeAll(bulletsToRemove)

            }

            if (byakhees.isEmpty() && dhole == null) {
                startNextLevel()
            }


        }



    }

    private fun startNextLevel() {
        level++
        speedMultiplier = 1f
        byakheesDestroyed = 0

        isLevelTransition = true
        transitionCounter = 0
        transitionAlpha = 0
        levelNumberAlpha = 0

        when {
            level <= maxByakheeLevels -> {
                createByakhees()
                background.switchBackground(Background.BackgroundType.COSMIC_HORROR_RUINS)
            }
            //level == maxByakheeLevels + 1 -> {
            else -> {
                createDhole()
                background.switchBackground(Background.BackgroundType.DHOLE_REALM)
            }
        }
    }

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

    private fun createDhole() {
        dhole = Dhole(screenWidth / 2, screenHeight / 4, screenWidth / 10)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchRadius = dpToPx(context, 200f) // rayon de controle autour du jour
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
                if (isPlayerTouched) {
                    player.moveTo(
                        event.x + touchOffsetX,
                        event.y + touchOffsetY,
                        screenWidth,
                        screenHeight,
                        background.structures // Passez les structures ici
                    )
                }
            }
            MotionEvent.ACTION_UP -> {
                if (isPlayerTouched) {
                    // Tirer seulement si le toucher est relâché près du joueur
                    val distanceToPlayer = kotlin.math.hypot(event.x - player.x, event.y - player.y)
                    if (distanceToPlayer <= player.size / 2 + touchRadius) {
                        bullets.add(Bullet(player.x, player.y - player.size / 2))
                    }
                    isPlayerTouched = false
                }
            }
        }
        return true
    }

    fun dpToPx(context: Context, dp: Float): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        )
    }

}

class Player(var x: Float, var y: Float, val size: Float) {
    var lives = 3
    var isAlive = true
    private var rotation = 0f
    private val rotationSpeed = 0.1f
    private val textureSize = size / 4f
    private var lastX = x
    private val veins = mutableListOf<Vein>()
    private val numVeins = 15

    init {
        generateVeins()
    }

    private fun generateVeins() {
        for (i in 0 until numVeins) {
            veins.add(Vein(
                Random.nextFloat() * 2 * PI.toFloat(),
                Random.nextFloat() * (size / 2 - textureSize),
                Random.nextFloat() * (size / 8) + size / 16,
                Random.nextFloat() * 2 + 1
            ))
        }
    }

    data class Vein(
        val angle: Float,
        val distanceFromCenter: Float,
        val length: Float,
        val width: Float
    )

    fun draw(canvas: Canvas, paint: Paint) {
        // Couleur de la boule en fonction des vies restantes
        val baseColor = when (lives) {
            3 -> Color.rgb(0, 255, 255)  // Cyan
            2 -> Color.rgb(255, 255, 0)  // Jaune
            1 -> Color.rgb(255, 0, 255)  // Magenta
            else -> Color.TRANSPARENT
        }

        // Dessiner la boule principale (l'oeil)
        paint.color = Color.WHITE
        canvas.drawCircle(x, y, size / 2, paint)

        // Dessiner les veines
        paint.color = Color.RED
        for (vein in veins) {
            val startX = x + cos(vein.angle) * vein.distanceFromCenter
            val startY = y + sin(vein.angle) * vein.distanceFromCenter
            val endX = startX + cos(vein.angle) * vein.length
            val endY = startY + sin(vein.angle) * vein.length
            paint.strokeWidth = vein.width
            canvas.drawLine(startX, startY, endX, endY, paint)
        }

        // Dessiner la boule  (l'iris) AVANT la boule noire
        paint.color = baseColor
        val eyeRadius = textureSize / 2 + 10f // Légèrement plus grand que la boule noire
        val eyeX = x + cos(rotation) * (size / 2 - eyeRadius)
        val eyeY = y + sin(rotation) * (size / 2 - eyeRadius)
        canvas.drawCircle(eyeX, eyeY, eyeRadius, paint)

        // Dessiner la texture (petit cercle noir)
        paint.color = Color.BLACK
        val textureX = x + cos(rotation) * (size / 2 - textureSize / 2)
        val textureY = y + sin(rotation) * (size / 2 - textureSize / 2)
        canvas.drawCircle(textureX, textureY, textureSize / 2, paint)

        // Mise à jour de la rotation seulement si la boule a bougé
        val movement = x - lastX
        if (abs(movement) > 0.1f) {  // Seuil pour éviter les micro-mouvements
            rotation += movement * rotationSpeed
            if (rotation > 2 * Math.PI) {
                rotation -= 2 * Math.PI.toFloat()
            } else if (rotation < 0) {
                rotation += 2 * Math.PI.toFloat()
            }
        }
        lastX = x
    }

    fun moveTo(newX: Float, newY: Float, screenWidth: Float, screenHeight: Float, structures: List<Background.Structure>) {
        //x = newX.coerceIn(0f, screenWidth)
        //y = newY.coerceIn(size / 2, screenHeight - size / 2)

        val potentialX = newX.coerceIn(size / 2, screenWidth - size / 2)
        val potentialY = newY.coerceIn(size / 2, screenHeight - size / 2)

        var canMove = true
        for (structure in structures) {
            if (structure.intersects(potentialX, potentialY, size)) {
                canMove = false
                break
            }
        }

        if (canMove) {
            x = potentialX
            y = potentialY
        }
    }

    fun hit(): Boolean {
        lives--
        if (lives <= 0) {
            isAlive = false
        }
        return !isAlive
    }

    fun intersects(byakhee: Byakhee): Boolean {
        val distance = kotlin.math.hypot(x - byakhee.x - byakhee.width / 2, y - byakhee.y - byakhee.height / 2)
        return distance < size / 2 + kotlin.math.min(byakhee.width, byakhee.height) / 2
    }
}

class Byakhee(var x: Float, var y: Float, val width: Float, val height: Float, level: Int) {
    var hitsToDestroy = 3 + level
    private var dx = Random.nextFloat() * 8 - 4 // Vitesse horizontale aléatoire entre -4 et 4
    private var dy = Random.nextFloat() * 8 - 4 // Vitesse verticale aléatoire entre -4 et 4

    private var rotationAngle = 0f
    private var rotationSpeed = 2f // Ajustez la vitesse de rotation selon vos préférences

    private lateinit var gradient: LinearGradient

    private val baseColor = when (level) {
        1 -> Color.RED
        2 -> Color.BLUE
        else -> Color.rgb(128,0,128)
    }

    init {
        // ... autre code d'initialisation ...
        updateGradient()
    }

    private fun updateGradient() {
        val startColor = when (hitsToDestroy) {
            5 -> Color.argb(255, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            4 -> Color.argb(230, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            3 -> Color.argb(200, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            2 -> Color.argb(170, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor))
            1 -> Color.WHITE
            else -> Color.TRANSPARENT
        }
        val endColor = Color.argb(
            Color.alpha(startColor),
            (Color.red(startColor) * 0.7f).toInt(),
            (Color.green(startColor) * 0.7f).toInt(),
            (Color.blue(startColor) * 0.7f).toInt()
        )
        gradient = LinearGradient(
            x, y, x + width, y + height,
            startColor, endColor, Shader.TileMode.CLAMP
        )
    }

    fun draw(canvas: Canvas, paint: Paint) {
        updateGradient()
        paint.shader = gradient
        paint.style = Paint.Style.FILL

        canvas.save()

        canvas.rotate(rotationAngle, x + width / 2, y + height / 2)

        val path = Path()
        path.moveTo(x + width / 2, y)
        path.lineTo(x, y + height)
        path.lineTo(x + width, y + height)
        path.close()

        canvas.drawPath(path, paint)

        canvas.restore()

        rotationAngle = (rotationAngle + rotationSpeed) % 360

        paint.shader = null  // Réinitialiser le shader pour ne pas affecter d'autres dessins
    }

    fun hit(): Boolean {
        hitsToDestroy--
        updateGradient()  // Mettre à jour le dégradé après chaque coup
        return hitsToDestroy <= 0
    }

fun move(screenWidth: Float, screenHeight: Float, speedMultiplier: Float, structures: List<Background.Structure>) {
    val newX = x + dx * speedMultiplier
    val newY = y + dy * speedMultiplier

    x = newX
    y = newY

    // Gestion de la sortie d'écran
    when {
        x < -width -> {
            x = screenWidth
            y = Random.nextFloat() * (screenHeight * 0.7f)
        }
        x > screenWidth -> {
            x = -width
            y = Random.nextFloat() * (screenHeight * 0.7f)
        }
        y < -height -> {
            y = screenHeight * 0.7f
            x = Random.nextFloat() * screenWidth
        }
        y > screenHeight -> {
            y = -height
            x = Random.nextFloat() * screenWidth
        }
    }
}
    fun changeDirection(speedMultiplier: Float) {
        if (Random.nextFloat() < 0.02) { // 2% de chance de changer de direction à chaque frame
            dx = (Random.nextFloat() * 8 - 4) * speedMultiplier
            dy = (Random.nextFloat() * 8 - 4) * speedMultiplier
        }
    }
}

class Dhole(var x: Float, var y: Float, val circleSize: Float) {
    private val numCircles = 15
    private val circles = mutableListOf<DholeCircle>()
    private var time = 0f
    private var dx = Random.nextFloat() * 4 - 2 // Vitesse horizontale aléatoire
    private var dy = Random.nextFloat() * 4 - 2 // Vitesse verticale aléatoire
    private val maxSpeed = 5f
    private val changeDirectionChance = 0.02f // 2% de chance de changer de direction à chaque frame
    private var invincibilityFrames = 0

    init {
        for (i in 0 until numCircles) {
            circles.add(DholeCircle(x, y + i * circleSize * 0.75f, circleSize))
        }
    }

    fun setInvincibilityFrame() {
        invincibilityFrames = 30 // Par exemple, 30 frames d'invincibilité (ajustez selon vos besoins)
    }

    fun draw(canvas: Canvas, paint: Paint) {
        circles.forEach { it.draw(canvas, paint) }
    }

    fun move(screenWidth: Float, screenHeight: Float) {
        // Mise à jour de la position
        x += dx
        y += dy

        if (invincibilityFrames > 0) {
            invincibilityFrames--
        }

        // Changement de direction aléatoire
        if (Random.nextFloat() < changeDirectionChance) {
            dx = Random.nextFloat() * maxSpeed * 2 - maxSpeed
            dy = Random.nextFloat() * maxSpeed * 2 - maxSpeed
        }

        // Gestion des bords de l'écran
        if (x < 0 || x > screenWidth) dx = -dx
        if (y < 0 || y > screenHeight / 2) dy = -dy // Limite à la moitié supérieure de l'écran

        // Mise à jour du temps pour l'ondulation
        time += 0.1f

        // Mise à jour de la position des cercles avec ondulation
        updateCirclePositions()
    }

    private fun updateCirclePositions() {
        val waveAmplitude = circleSize * 2 // Amplitude de l'ondulation
        val waveFrequency = 0.3f // Fréquence de l'ondulation

        for (i in 0 until numCircles) {
            val offsetX = waveAmplitude * sin((time + i * waveFrequency) * 2)
            val offsetY = waveAmplitude * sin((time + i * waveFrequency) * 3) / 2

            circles[i].x = x + offsetX
            circles[i].y = y + i * circleSize * 0.75f + offsetY
        }
    }

    fun hit(bulletX: Float, bulletY: Float): Boolean {
        for (circle in circles) {
            if (!circle.isDestroyed() && circle.hit(bulletX, bulletY)) {
                return true
            }
        }
        return false
    }

    fun isDestroyed(): Boolean = circles.all { it.isDestroyed() }

    fun intersectsPlayer(player: Player): Boolean {
        return invincibilityFrames <= 0 && circles.any { it.intersectsPlayer(player) }
    }

}


class DholeCircle(var x: Float, var y: Float, val size: Float) {
    private var hits = 0
    private val maxHits = 3

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = when (hits) {
            0 -> Color.rgb(128, 0, 128) // Violet foncé
            1 -> Color.rgb(160, 32, 240) // Violet moyen
            else -> Color.rgb(192, 64, 255) // Violet clair
        }
        canvas.drawCircle(x, y, size / 2, paint)
    }

    fun hit(bulletX: Float, bulletY: Float): Boolean {
        if (hits < maxHits && kotlin.math.hypot(x - bulletX, y - bulletY) <= size / 2) {
            hits++
            return true
        }
        return false
    }

    fun isDestroyed(): Boolean = hits >= maxHits

    fun intersectsPlayer(player: Player): Boolean {
        return kotlin.math.hypot(x - player.x, y - player.y) <= (size / 2 + player.size / 2)
    }
}

class Bullet(var x: Float, var y: Float) {
    val width = 5f  // Largeur de la balle
    val height = 15f  // Hauteur de la balle
    private val speed = 15f
    private val boltLength = 150f
    private val boltWidth = 5f
    private val flickerSpeed = 5 // Contrôle la vitesse de scintillement

    //private val baseColor = Color.rgb(128, 0, 255) // Violet de base pour l'énergie occulte
    private  var baseColor = Color.YELLOW
    private var currentColor = baseColor
    private val random = Random


    fun draw(canvas: Canvas, paint: Paint) {
        // Faire scintiller la couleur
        if (random.nextInt(flickerSpeed) == 0) {
            currentColor = getFlickeringColor()
        }
        paint.color = currentColor
        paint.strokeWidth = boltWidth

        // Dessiner le corps principal de l'éclair avec un zigzag
        val zigzagWidth = 10f
        var lastX = x
        var lastY = y
        for (i in 0..5) {
            val newX = x + (if (i % 2 == 0) zigzagWidth else -zigzagWidth)
            val newY = y - (i + 1) * boltLength / 6
            canvas.drawLine(lastX, lastY, newX, newY, paint)
            lastX = newX
            lastY = newY
        }

    }

    private fun getFlickeringColor(): Int {
        val flickerIntensity = 50
        return Color.rgb(
            (Color.red(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255),
            (Color.green(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255),
            (Color.blue(baseColor) + random.nextInt(-flickerIntensity, flickerIntensity + 1)).coerceIn(0, 255)
        )
    }

    fun move() {
        y -= speed
    }

    fun intersects(byakhee: Byakhee): Boolean {
        return x < byakhee.x + byakhee.width &&
                x + width > byakhee.x &&
                y < byakhee.y + byakhee.height &&
                y + height > byakhee.y
    }
}

class ExplosionParticle(
    private var x: Float,
    private var y: Float,
    private var initialSize: Float,
    private var maxSize: Float,
    private val growthRate: Float,
    private val color: Int
) {
    private var currentSize = initialSize
    private var alpha = 255

    fun update(): Boolean {
        currentSize += growthRate
        alpha -= 5

        return currentSize < maxSize && alpha > 0
    }

    fun draw(canvas: Canvas, paint: Paint) {
        paint.color = Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5f

        canvas.drawCircle(x, y, currentSize / 2, paint)

        // Reset paint properties
        paint.reset()
    }

    fun isAlive(): Boolean = currentSize < maxSize && alpha > 0
}

class Background(private val context: Context, private val screenWidth: Float, private val screenHeight: Float) {
    private val paint = Paint()
    val structures = mutableListOf<Structure>()
    private var backgroundType = BackgroundType.COSMIC_HORROR_RUINS
    private var backgroundBitmap: Bitmap? = null

    enum class BackgroundType {
        COSMIC_HORROR_RUINS,
        DHOLE_REALM
        // Ajoutez d'autres types de fond ici au besoin
    }

    init {
        loadBackgroundImage()
        generateStructures()
    }

    private fun loadBackgroundImage() {
        val resourceId = when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> R.drawable.byakhee_horror_background
            BackgroundType.DHOLE_REALM -> R.drawable.dhole_realm_background
        }

        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeResource(context.resources, resourceId, options)

        val sampleSize = calculateInSampleSize(options, screenWidth.toInt(), screenHeight.toInt())
        options.apply {
            inJustDecodeBounds = false
            inSampleSize = sampleSize
        }

        backgroundBitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)
        backgroundBitmap = Bitmap.createScaledBitmap(backgroundBitmap!!, screenWidth.toInt(), screenHeight.toInt(), true)
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }

        return inSampleSize
    }

    private fun generateStructures() {
        structures.clear()
        when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> generateByakheeRuins()
            BackgroundType.DHOLE_REALM -> generateDholeRealm()
        }
    }
/*
    private fun generateByakheeRuins() {
        for (i in 0..10) {
            structures.add(
                Structure(
                    Random.nextFloat() * screenWidth,
                    Random.nextFloat() * screenHeight,
                    Random.nextFloat() * 100 + 50,
                    Random.nextFloat() * 200 + 100
                )
            )
        }
    }
*/
private fun generateByakheeRuins() {
    val minStructureWidth = screenWidth * 0.05f  // 5% de la largeur de l'écran
    val maxStructureWidth = screenWidth * 0.15f  // 15% de la largeur de l'écran
    val minStructureHeight = screenHeight * 0.05f  // 5% de la hauteur de l'écran
    val maxStructureHeight = screenHeight * 0.2f  // 20% de la hauteur de l'écran

    for (i in 0..10) {
        val structureWidth = Random.nextFloat() * (maxStructureWidth - minStructureWidth) + minStructureWidth
        val structureHeight = Random.nextFloat() * (maxStructureHeight - minStructureHeight) + minStructureHeight

        structures.add(
            Structure(
                Random.nextFloat() * (screenWidth - structureWidth),
                Random.nextFloat() * (screenHeight - structureHeight),
                structureWidth,
                structureHeight
            )
        )
    }
}

    private fun generateDholeRealm() {
        // Implémentez la génération du décor pour le royaume des Dholes
    }

    private fun generateElderGodsDomain() {
        // Implémentez la génération du décor pour le domaine des Dieux Anciens
    }

    private fun generateDreamlands() {
        // Implémentez la génération du décor pour les Contrées du Rêve
    }

    fun draw(canvas: Canvas) {

        // Dessiner l'image de fond
        backgroundBitmap?.let { bitmap ->
            canvas.drawBitmap(bitmap, 0f, 0f, null)
        }

        when (backgroundType) {
            BackgroundType.COSMIC_HORROR_RUINS -> drawByakheeRuins(canvas)
            BackgroundType.DHOLE_REALM -> drawDholeRealm(canvas)
        }
    }

    private fun drawByakheeRuins(canvas: Canvas) {
        //canvas.drawColor(Color.BLACK)
        drawStars(canvas)
        paint.color = Color.DKGRAY
        for (structure in structures) {
            structure.draw(canvas)
        }
    }

    private fun drawDholeRealm(canvas: Canvas) {
        // Implémentez le dessin du royaume des Dholes
        drawStars(canvas)
    }

    private fun drawStars(canvas: Canvas) {
        paint.color = Color.WHITE
        for (i in 0..200) {
            val x = Random.nextFloat() * screenWidth
            val y = Random.nextFloat() * screenHeight
            canvas.drawCircle(x, y, 5f, paint)
        }
    }

    fun switchBackground(newType: BackgroundType) {
        backgroundType = newType
        loadBackgroundImage()
        generateStructures()
    }

    inner class Structure(val x: Float, val y: Float, val width: Float, val height: Float) {
        private val gradientPaint = Paint()
        private val gradient: LinearGradient

        init {
            // Créer un gradient de gris allant du gris clair au gris foncé
            gradient = LinearGradient(
                x, y, x, y + height,
                intArrayOf(Color.LTGRAY, Color.DKGRAY),
                null,
                Shader.TileMode.CLAMP
            )
            gradientPaint.shader = gradient
        }
/*
        fun draw(canvas: Canvas) {
            // Dessiner la structure avec le gradient
            canvas.drawRect(x, y, x + width, y + height, gradientPaint)

            // Dessiner les traits blancs pour simuler les briques
            val brickPaint = Paint().apply {
                color = Color.WHITE
                strokeWidth = 2f
            }

            // Calculer la taille des briques et l'espacement
            val brickWidth = width / 5 // 5 briques en largeur
            val brickHeight = height / 3 // 3 briques en hauteur
            val spacing = 2f // Espacement entre les briques

            // Dessiner les lignes horizontales
            for (i in 1..2) { // 2 lignes horizontales
                val brickY = y + i * brickHeight + (i - 1) * spacing
                canvas.drawLine(x, brickY, x + width, brickY, brickPaint)
            }

            // Dessiner les lignes verticales
            for (i in 1..4) { // 4 lignes verticales
                val brickX = x + i * brickWidth + (i - 1) * spacing
                canvas.drawLine(brickX, y, brickX, y + height, brickPaint)
            }
        }
*/
fun draw(canvas: Canvas) {
    // Dessiner la structure avec le gradient
    canvas.drawRect(x, y, x + width, y + height, gradientPaint)

    // Dessiner les traits blancs pour simuler les briques
    val brickPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = width * 0.01f  // Ajuster l'épaisseur des lignes en fonction de la largeur
    }

    // Calculer la taille des briques et l'espacement
    val brickWidth = width / 5 // 5 briques en largeur
    val brickHeight = height / 3 // 3 briques en hauteur
    val spacing = width * 0.01f // Espacement entre les briques

    // Dessiner les lignes horizontales
    for (i in 1..2) { // 2 lignes horizontales
        val brickY = y + i * brickHeight + (i - 1) * spacing
        canvas.drawLine(x, brickY, x + width, brickY, brickPaint)
    }

    // Dessiner les lignes verticales
    for (i in 1..4) { // 4 lignes verticales
        val brickX = x + i * brickWidth + (i - 1) * spacing
        canvas.drawLine(brickX, y, brickX, y + height, brickPaint)
    }
}

        fun intersects(playerX: Float, playerY: Float, playerSize: Float): Boolean {
            return playerX + playerSize / 2 > x && playerX - playerSize / 2 < x + width &&
                    playerY + playerSize / 2 > y && playerY - playerSize / 2 < y + height
        }

        fun intersectsBulletVersusStruct(objX: Float, objY: Float, objWidth: Float, objHeight: Float): Boolean {
            return objX < x + width && objX + objWidth > x &&
                    objY < y + height && objY + objHeight > y
        }
    }
}