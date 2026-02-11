package com.outer.invaders.game.activity

import android.content.Intent
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.outer.invaders.R

class LevelIntroActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_level_intro)

        val imageViewBackground: ImageView = findViewById(R.id.imageViewCosmicHorrors) // Assurez-vous d'avoir cet ID dans votre layout

        // Chargement et démarrage de l'animation de fondu
        val animation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        animation.fillAfter = true // Conserver l'état final de l'animation
        imageViewBackground.startAnimation(animation)

        // Démarrage de MainActivity après la fin de l'animation
        animation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}
            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                startActivity(Intent(this@LevelIntroActivity, MainActivity::class.java))
                finish() // Termine l'activité LevelIntroActivity
            }
        })
    }
}
