package com.example.space_invaders.entities.deepone

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PathMeasure
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import com.example.space_invaders.entities.player.Player
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

class DeepOne(
    x: Float,
    y: Float,
    width: Float,
    height: Float,
    screenWidth: Float,
    screenHeight: Float,
    var hitsToDestroy: Int = 2
) {
    private var lastAttackTime = 0L
    private val attackInterval = 2000L // 2 secondes
    val ichorousBlasts = mutableListOf<IchorousBlast>()
    private var direction: Int = 1 // 1 pour droite, -1 pour gauche
    private var zigzagDistance: Float = 100f // Distance horizontale du zigzag
    private var zigzagProgress: Float = 0f
    private var verticalSpeed: Float = 5f // Vitesse verticale
    private var horizontalSpeed: Float = 5f // Vitesse horizontale
    // Nouvelles variables pour le mouvement aléatoire
    private  var randomJumpTimer: Int = 0
    private var isJumping: Boolean = false
    private var jumpHeight: Float = 0f
    private var rotationAngle: Float = 0f

    var x = x
    var y = y
    var width = width
        private set
    var height = height
        private set
    var screenHWidth = screenWidth
        private set
    var screenHHeight = screenHeight
        private set

    // Nouvelles variables pour les effets lovecraftiens
    private var glowPhase = 0f
    private var distortionPhase = 0f
    // Cclasse Rune pour stocker des positions relatives
    private data class Rune(
        val symbol: String,
        var offsetX: Float, // Position relative au DeepOne
        var offsetY: Float, // Position relative au DeepOne
        var alpha: Float
    )
    private val runeSymbols = listOf("𐀀", "𐀁", "𐀂", "𐀃", "𐀄")
    private var activeRunes = mutableListOf<Rune>()

    init {
        // Initialiser les runes avec des positions relatives
        repeat(5) {
            activeRunes.add(Rune(
                symbol = runeSymbols.random(),
                offsetX = Random.nextFloat() * width - width/2, // Position relative au centre
                offsetY = Random.nextFloat() * height - height/2, // Position relative au centre
                alpha = 0f
            ))
        }
    }


    // Mouvement
    fun move() {
        // Mouvement vertical de base
        y += verticalSpeed

        // Mouvement horizontal en zigzag avec variation aléatoire
        val randomHorizontalVariation = Random.nextFloat() * 2 - 1 // Entre -1 et 1
        x += direction * (horizontalSpeed + randomHorizontalVariation)
        zigzagProgress += horizontalSpeed

        // Changement de direction avec une chance de changement aléatoire
        if (zigzagProgress >= zigzagDistance || Random.nextFloat() < 0.05) {
            direction *= -1 // Inverse la direction
            zigzagProgress = 0f
            // Chance de changer la distance du zigzag
            if (Random.nextFloat() < 0.3) {
                zigzagDistance = Random.nextFloat() * 150f + 50f // Entre 50 et 200
            }
        }

        // Saut aléatoire
        if (!isJumping) {
            randomJumpTimer++
            if (randomJumpTimer > 60 && Random.nextFloat() < 0.1) { // Chance de sauter toutes les 60 frames
                isJumping = true
                jumpHeight = Random.nextFloat() * 50f + 20f // Hauteur de saut entre 20 et 70
                randomJumpTimer = 0
            }
        } else {
            y += jumpHeight * 0.1f
            jumpHeight -= 5f // Gravité
            if (jumpHeight < 0) {
                isJumping = false
                jumpHeight = 0f
            }
        }

        // Rotation aléatoire
        rotationAngle += Random.nextFloat() * 10f - 5f // Rotation entre -5 et 5 degrés par frame

        // Variations aléatoires des vitesses
        if (Random.nextFloat() < 0.02) { // 2% de chance de changer les vitesses
            verticalSpeed = Random.nextFloat() * 4f + 1f // Entre 1 et 5
            horizontalSpeed = Random.nextFloat() * 5f + 2f // Entre 2 et 7
        }

        // Animation de l'aura mystique
        glowPhase += 0.05f

        // Mise à jour des runes avec des mouvements relatifs
        activeRunes.forEach { rune ->
            // Faire tourner les runes autour du DeepOne
            val radius = hypot(rune.offsetX, rune.offsetY)
            val currentAngle = atan2(rune.offsetY, rune.offsetX)
            val newAngle = currentAngle + 0.02f // Vitesse de rotation

            rune.offsetX = cos(newAngle) * radius + sin(glowPhase) * 10
            rune.offsetY = sin(newAngle) * radius + cos(glowPhase) * 10
            rune.alpha = (sin(glowPhase) + 1) / 2 * 255
        }

        // Effet de distorsion
        distortionPhase += 0.02f

    }

    fun attackPlayer(player: Player) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastAttackTime > attackInterval) {
            // Créer un projectile dirigé vers le joueur
            val dx = player.x - (x + width / 2)
            val dy = player.y - (y + height / 2)
            val distance = hypot(dx, dy)
            val projectileSpeed = 5f // Ajustez la vitesse du projectile
            val ichorousBlast = IchorousBlast(
                x + width / 2,
                y + height,
                dx / distance * projectileSpeed,
                dy / distance * projectileSpeed,
                Color.GREEN // Ou une autre couleur pour le liquide
            )
            ichorousBlasts.add(ichorousBlast)
            lastAttackTime = currentTime
        }

        // Mettre à jour et dessiner les projectiles
        ichorousBlasts.forEach { it.move() }
        ichorousBlasts.removeAll {
            val screenWidth = screenHWidth
            val screenHeight = screenHHeight
            it.isOffScreen(screenWidth, screenHeight)
        }

    }
    fun hit(): Boolean {
        hitsToDestroy--
        return hitsToDestroy <= 0
    }

fun draw(canvas: Canvas, paint: Paint) {
    // Dessin de l'aura mystique renforcée
    paint.style = Paint.Style.FILL

    // Première couche d'aura (plus large et subtile)
    val outerAuraShader = RadialGradient(
        x + width / 2, y + height / 2,
        width * 2f,
        intArrayOf(
            Color.argb(0, 0, 255, 255),
            Color.argb(40, 0, 200, 200),
            Color.argb(0, 0, 150, 150)
        ),
        floatArrayOf(0.3f, 0.6f, 1f),
        Shader.TileMode.CLAMP
    )
    paint.shader = outerAuraShader
    canvas.drawCircle(x + width / 2, y + height / 2, width * 2f, paint)

    // Deuxième couche d'aura (plus intense et concentrée)
    val innerAuraShader = RadialGradient(
        x + width / 2, y + height / 2,
        width * 1.5f,
        intArrayOf(
            Color.argb(100, 0, 255, 255),
            Color.argb(80, 0, 200, 255),
            Color.argb(0, 0, 150, 255)
        ),
        floatArrayOf(0.2f, 0.5f, 1f),
        Shader.TileMode.CLAMP
    )
    paint.shader = innerAuraShader
    canvas.drawCircle(x + width / 2, y + height / 2, width * 1.5f, paint)

    // Pulsation de l'aura
    val pulseIntensity = (sin(glowPhase) + 1) / 2
    val pulseAuraShader = RadialGradient(
        x + width / 2, y + height / 2,
        width * 1.2f,
        intArrayOf(
            Color.argb((100 * pulseIntensity).toInt(), 0, 255, 255),
            Color.argb((60 * pulseIntensity).toInt(), 0, 200, 255),
            Color.argb(0, 0, 150, 255)
        ),
        floatArrayOf(0.1f, 0.4f, 1f),
        Shader.TileMode.CLAMP
    )
    paint.shader = pulseAuraShader
    canvas.drawCircle(x + width / 2, y + height / 2, width * 1.2f * (0.8f + 0.2f * pulseIntensity), paint)
    paint.shader = null

    // Effet de distorsion de l'espace renforcé
    val distortionPath = Path()
    val distortionRadius = width * 2
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f // Ligne plus épaisse

    for (angle in 0..360 step 15) {
        val angleRad = Math.toRadians(angle.toDouble())
        val distortion = sin(distortionPhase + angle / 30f) * 25 // Amplitude augmentée
        val xPos = x + width / 2 + cos(angleRad) * (distortionRadius + distortion)
        val yPos = y + height / 2 + sin(angleRad) * (distortionRadius + distortion)

        if (angle == 0) distortionPath.moveTo(xPos.toFloat(), yPos.toFloat())
        else distortionPath.lineTo(xPos.toFloat(), yPos.toFloat())
    }
    distortionPath.close()

    // Double tracé pour l'effet de distorsion
    paint.color = Color.argb(70, 0, 255, 255)
    canvas.drawPath(distortionPath, paint)
    paint.color = Color.argb(40, 255, 255, 255)
    paint.strokeWidth = 1.5f
    canvas.drawPath(distortionPath, paint)

    // Le reste du code de dessin du DeepOne...
    val bodyColor = Color.rgb(0, 128, 0)
    val bellyColor = Color.WHITE
    val scaleColor = Color.rgb(0, 100, 0)
    val eyeColor = Color.YELLOW
    val pupilColor = Color.BLACK
    val gillColor = Color.RED

    // Corps verdâtre
    drawBody(canvas, paint, bodyColor)

    // Ventre blanc
    drawBelly(canvas, paint, bellyColor)

    // Écailles sur le dos
    drawScales(canvas, paint, scaleColor)

    // Tête de poisson
    drawHead(canvas, paint, bodyColor)

    // Yeux et pupilles
    drawEyes(canvas, paint, eyeColor, pupilColor)

    // Ouïes
    drawGills(canvas, paint, gillColor)

    // Pattes palmées
    drawLegs(canvas, paint, bodyColor)

    // Dessiner les runes mystiques avec plus d'intensité
    paint.reset()
    paint.textSize = 30f
    activeRunes.forEach { rune ->
        // Effet de lueur autour des runes
        paint.style = Paint.Style.FILL
        paint.color = Color.argb((rune.alpha * 0.3).toInt(), 0, 255, 255)
        paint.strokeWidth = 8f
        val runeX = x + width/2 + rune.offsetX
        val runeY = y + height/2 + rune.offsetY

        // Halo autour des runes
        canvas.drawCircle(runeX, runeY, 20f, paint)

        // Rune elle-même avec plus d'intensité
        paint.alpha = rune.alpha.toInt()
        paint.color = Color.rgb(100, 255, 255)
        canvas.drawText(
            rune.symbol,
            runeX,
            runeY,
            paint
        )
    }

    // Dessiner les projectiles
    ichorousBlasts.forEach { it.draw(canvas, paint) }
}
    private fun drawBody(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = Color.LTGRAY
        //canvas.drawCircle(x + width / 2 , y + height / 2, 80f, paint)
        drawTextured3DCircle(canvas, x + width / 2, y + height / 2, 80f)
        paint.color = color
        val bodyPath = Path().apply {
            moveTo(x, y + height / 2)
            quadTo(x + width / 4, y, x + width / 2, y + height / 2)
            quadTo(x + 3 * width / 4, y + height, x + width, y + height / 2)
            close()
        }
        canvas.drawPath(bodyPath, paint)
    }

    private fun drawBelly(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = color
        val bellyPath = Path().apply {
            moveTo(x + width / 4, y + height / 2)
            quadTo(x + width / 2, y + 3 * height / 4, x + 3 * width / 4, y + height / 2)
            close()
        }
        canvas.drawPath(bellyPath, paint)
    }

    private fun drawScales(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = color
        val scaleCount = 10
        val scaleRadius = width / 40f
        val bodyPath = Path()
        bodyPath.moveTo(x, y + height / 2)
        bodyPath.quadTo(x + width / 4, y, x + width / 2, y + height / 2)

        for (i in 0 until scaleCount) {
            val t = i.toFloat() / (scaleCount - 1)
            val pathMeasure = PathMeasure(bodyPath, false)
            val pos = FloatArray(2)
            pathMeasure.getPosTan(t * pathMeasure.length, pos, null)

            val scaleX = pos[0]
            val scaleY = pos[1] - Random.nextFloat() * height / 8
            canvas.drawCircle(scaleX, scaleY, scaleRadius, paint)
        }
    }

    private fun drawHead(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = color
        val headRadius = width / 4
        canvas.drawCircle(x + width, y + height / 2, headRadius, paint)
    }

    private fun drawEyes(canvas: Canvas, paint: Paint, eyeColor: Int, pupilColor: Int) {
        val headRadius = width / 4
        val eyeRadius = headRadius / 4
        val eyeOffsetX = headRadius / 2
        val eyeOffsetY = headRadius / 3

        paint.color = eyeColor
        canvas.drawCircle(x + width - eyeOffsetX, y + height / 2 - eyeOffsetY, eyeRadius, paint)
        canvas.drawCircle(x + width + eyeOffsetX, y + height / 2 - eyeOffsetY, eyeRadius, paint)

        paint.color = pupilColor
        val pupilRadius = eyeRadius / 2
        canvas.drawCircle(x + width - eyeOffsetX, y + height / 2 - eyeOffsetY, pupilRadius, paint)
        canvas.drawCircle(x + width + eyeOffsetX, y + height / 2 - eyeOffsetY, pupilRadius, paint)
    }

    private fun drawGills(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = color
        val headRadius = width / 4
        val gillSize = headRadius / 3
        val gillOffsetX = headRadius * 0.8f
        val gillOffsetY = headRadius / 2

        canvas.drawOval(x + width - gillOffsetX, y + height / 2 + gillOffsetY, x + width - gillOffsetX + gillSize, y + height / 2 + gillOffsetY + gillSize / 2, paint)
        canvas.drawOval(x + width + gillOffsetX - gillSize, y + height / 2 + gillOffsetY, x + width + gillOffsetX, y + height / 2 + gillOffsetY + gillSize / 2, paint)
    }

    private fun drawLegs(canvas: Canvas, paint: Paint, color: Int) {
        paint.color = color
        val legWidth = width / 8
        val legHeight = height / 3
        val legOffsetX = width / 3
        val legOffsetY = height / 2

        drawLeg(canvas, paint, x + legOffsetX, y + height - legOffsetY, legWidth, legHeight, 30f)
        drawLeg(canvas, paint, x + width - legOffsetX, y + height - legOffsetY, legWidth, legHeight, -30f)
    }

    private fun drawLeg(canvas: Canvas, paint: Paint, startX: Float, startY: Float, legWidth: Float, legHeight: Float, angle: Float) {
        canvas.save()
        canvas.rotate(angle, startX, startY)
        val legPath = Path().apply {
            moveTo(startX, startY)
            lineTo(startX + legWidth, startY)
            lineTo(startX + legWidth / 2, startY + legHeight)
            close()
        }
        canvas.drawPath(legPath, paint)
        canvas.restore()
    }

    fun drawTextured3DCircle(canvas: Canvas, x: Float, y: Float, radius: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Fond de base
        paint.color = Color.LTGRAY
        canvas.drawCircle(x, y, radius, paint)

        // Dégradé pour l'effet 3D
        val shader = RadialGradient(
            x - radius * 0.3f, y - radius * 0.3f, radius * 1.3f,
            intArrayOf(Color.WHITE, Color.LTGRAY, Color.DKGRAY),
            floatArrayOf(0f, 0.7f, 1f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader
        canvas.drawCircle(x, y, radius, paint)

        // Réinitialiser le shader
        paint.shader = null

        // Texture
        val textureSize = 4f
        val textureRadius = radius * 0.9f // Légèrement plus petit pour laisser un bord
        for (i in 0 until 360 step 15) {
            for (j in 0 until radius.toInt() step textureSize.toInt()) {
                val angle = Math.toRadians(i.toDouble())
                val xPos = x + j * kotlin.math.cos(angle).toFloat()
                val yPos = y + j * kotlin.math.sin(angle).toFloat()

                if (Math.sqrt(Math.pow((xPos - x).toDouble(), 2.0) + Math.pow((yPos - y).toDouble(), 2.0)) <= textureRadius) {
                    paint.color = if (Math.random() > 0.5) Color.argb(50, 200, 200, 200) else Color.argb(50, 100, 100, 100)
                    canvas.drawCircle(xPos, yPos, textureSize / 2, paint)
                }
            }
        }

        // Reflet
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = radius * 0.1f
        val highlightShader = LinearGradient(
            x - radius, y - radius,
            x + radius, y + radius,
            intArrayOf(Color.WHITE, Color.TRANSPARENT),
            null,
            Shader.TileMode.CLAMP
        )
        paint.shader = highlightShader
        val highlightRect = RectF(x - radius * 0.8f, y - radius * 0.8f, x + radius * 0.8f, y - radius * 0.2f)
        canvas.drawArc(highlightRect, 180f, 180f, false, paint)
    }
}