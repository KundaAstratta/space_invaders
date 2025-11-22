package com.example.space_invaders.game.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.space_invaders.R

class GameOverActivity : AppCompatActivity() { // Ou Fragment si vous utilisez des fragments

    private lateinit var scoreTextView: TextView
    private val gameOverDelay = 5000L // 15 secondes en millisecondes

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over) // Créez ce layout

        scoreTextView = findViewById(R.id.scoreTextView) // Assurez-vous d'avoir ce TextView dans votre layout

        //val finalScore = intent.getIntExtra("finalScore", 0) // Récupérez le score depuis l'intent
        //scoreTextView.text = "Final score : $finalScore"
        scoreTextView.text = "Reboot...."


        // Démarrez un délai avant de revenir à l'écran de début
        Handler(Looper.getMainLooper()).postDelayed({
            finish() // Terminez cette activité et revenez à l'activité précédente
        }, gameOverDelay)
    }
}