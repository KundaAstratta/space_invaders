// code/Nightgaunt.kt
package com.example.space_invaders.entities.nightgaunt

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import com.example.space_invaders.entities.player.Player // Assurez-vous que le chemin est correct
import kotlin.math.hypot
import kotlin.math.sin

class Nightgaunt(
    var x: Float,
    var y: Float,
    val size: Float,
    // Référence à l'essaim n'est plus strictement nécessaire ici si le mouvement est centralisé
    // val swarm: NightgauntSwarm
    private var animationOffset: Float = Math.random().toFloat() * 1000f // Pour décaler les animations
) {

    // Le dessin fourni est complexe et très stylisé, on le garde.
    fun draw(canvas: Canvas, paint: Paint) {
        // Sauvegarde de l'état du canvas pour permettre des transformations
        canvas.save()

        // Création d'un effet de pulsation légère suggérant une instabilité dimensionnelle
        val time = System.currentTimeMillis() + animationOffset // Utiliser l'offset
        val pulseScale = 1f + (sin(time * 0.005f) * 0.05f)
        canvas.scale(pulseScale, pulseScale, x, y)

        // Palette de couleurs de base reflétant l'horreur cosmique
        val shadowColor = Color.argb(220, 20, 20, 30) // Sombre, presque noir
        val boneColor = Color.argb(200, 80, 80, 90)   // Gris os
        val eyeColor = Color.argb(255, 50, 200, 220) // Lueur cyan maladive

        // Corps principal - une forme asymétrique et inquiétante
        paint.style = Paint.Style.FILL
        paint.color = shadowColor

        // Création d'un chemin corporel complexe et non-euclidien
        // (Utilisation de courbes de Bézier pour des formes organiques)
        val bodyPath = Path().apply {
            // Point de départ (haut du "torse")
            moveTo(x, y - size * 0.6f) // Légèrement plus haut

            // Courbe pour le côté gauche
            cubicTo(
                x - size * 0.6f, y - size * 0.3f,  // Point de contrôle 1 (vers l'extérieur et le bas)
                x - size * 0.4f, y + size * 0.5f,  // Point de contrôle 2 (vers l'intérieur et le bas)
                x, y + size * 0.7f                  // Point final (bas du "corps")
            )

            // Courbe pour le côté droit
            cubicTo(
                x + size * 0.4f, y + size * 0.5f,  // Point de contrôle 1 (symétrique au précédent)
                x + size * 0.6f, y - size * 0.3f,  // Point de contrôle 2 (symétrique au précédent)
                x, y - size * 0.6f                  // Retour au point de départ
            )
            close() // Ferme le chemin
        }
        canvas.drawPath(bodyPath, paint)


        // Ailes éthérées et translucides avec une forme plus membranaire
        paint.color = Color.argb(90, 60, 60, 80) // Plus sombre et translucide
        val wingAmplitude = sin(time * 0.007f) * size * 0.2f // Mouvement de battement subtil

        val leftWingPath = Path().apply {
            moveTo(x - size * 0.1f, y - size * 0.2f) // Point d'attache près du corps
            // Courbe vers l'extérieur et le haut, puis revient
            quadTo(x - size * 1.2f, y - size * 0.8f + wingAmplitude, x - size * 0.8f, y - size * 1.3f)
            // Retour vers le corps, créant une forme de membrane tendue
            quadTo(x - size * 0.3f, y - size * 0.4f, x - size * 0.1f, y - size * 0.2f)
            close()
        }
        val rightWingPath = Path().apply {
            moveTo(x + size * 0.1f, y - size * 0.2f)
            quadTo(x + size * 1.2f, y - size * 0.8f + wingAmplitude, x + size * 0.8f, y - size * 1.3f)
            quadTo(x + size * 0.3f, y - size * 0.4f, x + size * 0.1f, y - size * 0.2f)
            close()
        }
        canvas.drawPath(leftWingPath, paint)
        canvas.drawPath(rightWingPath, paint)


        // Lignes structurelles semblables à des os suggérant une physiologie impossible
        paint.style = Paint.Style.STROKE
        paint.color = boneColor
        paint.strokeWidth = size * 0.04f // Un peu plus fin
        paint.strokeCap = Paint.Cap.ROUND // Extrémités arrondies

        // Lignes structurelles squelettiques plus dynamiques
        canvas.drawLine(x - size * 0.3f, y, x + size * 0.3f, y + size * 0.1f, paint) // Ligne horizontale légèrement inclinée
        canvas.drawLine(x, y - size * 0.5f, x - size * 0.1f, y + size * 0.6f, paint) // Ligne verticale légèrement décentrée
        // Ajouter quelques lignes pour les membres/griffes suggérées
        canvas.drawLine(x - size * 0.3f, y + size * 0.6f, x - size * 0.5f, y + size * 0.8f, paint)
        canvas.drawLine(x + size * 0.3f, y + size * 0.6f, x + size * 0.5f, y + size * 0.8f, paint)

        // "Yeux" ou fentes cosmiques et pulsantes
        paint.style = Paint.Style.FILL
        paint.color = eyeColor

        // Utiliser des rectangles très fins (fentes) au lieu de cercles
        val eyeWidth = size * 0.05f
        val eyeHeight = size * 0.15f
        val eyePulse = (sin(time * 0.008f) + 1f) / 2f // Pulsation de 0 à 1

        // Fente gauche
        val leftEyeY = y - size * 0.3f
        canvas.drawRect(
            x - size * 0.2f - eyeWidth / 2f, leftEyeY - eyeHeight * eyePulse / 2f,
            x - size * 0.2f + eyeWidth / 2f, leftEyeY + eyeHeight * eyePulse / 2f,
            paint
        )
        // Fente droite
        val rightEyeY = y - size * 0.25f // Légèrement décalée pour l'asymétrie
        canvas.drawRect(
            x + size * 0.2f - eyeWidth / 2f, rightEyeY - eyeHeight * eyePulse / 2f,
            x + size * 0.2f + eyeWidth / 2f, rightEyeY + eyeHeight * eyePulse / 2f,
            paint
        )


        // Ajout d'un léger effet de lueur aux fentes
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = size * 0.06f
        paint.color = Color.argb( (100 * eyePulse).toInt() , 80, 220, 255) // Lueur cyan plus intense avec la pulsation
        // Dessiner des rectangles de lueur un peu plus grands
        canvas.drawRect(
            x - size * 0.2f - eyeWidth, leftEyeY - eyeHeight * eyePulse * 0.7f,
            x - size * 0.2f + eyeWidth, leftEyeY + eyeHeight * eyePulse * 0.7f,
            paint
        )
        canvas.drawRect(
            x + size * 0.2f - eyeWidth, rightEyeY - eyeHeight * eyePulse * 0.7f,
            x + size * 0.2f + eyeWidth, rightEyeY + eyeHeight * eyePulse * 0.7f,
            paint
        )

        // Restauration du canvas à son état d'origine
        canvas.restore()
    }

    // Collision avec une balle (basée sur la taille)
    fun intersectsBullet(bulletX: Float, bulletY: Float): Boolean {
        // Utiliser une hitbox légèrement plus petite que la taille visuelle pour être juste
        val hitRadius = size * 0.4f
        val distance = hypot(x - bulletX, y - bulletY)
        return distance < hitRadius // La balle n'a pas de rayon ici, on suppose qu'elle est un point
    }

    // Collision avec le joueur
    fun intersectsPlayer(player: Player): Boolean {
        val distance = hypot(x - player.x, y - player.y)
        // Utiliser le rayon combiné (approximatif)
        return distance < (size / 2 + player.size / 2) * 0.8f // Réduire un peu la hitbox pour la sensation
    }

}