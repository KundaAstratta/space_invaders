package com.outer.invaders.game.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.outer.invaders.R

class GameWinActivity : AppCompatActivity() {

    // Durée d'affichage de l'écran de victoire en millisecondes (4 secondes)
    private val WIN_SCREEN_DURATION = 4000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_win)

        // Utilise un Handler pour programmer une action après un délai
        Handler(Looper.getMainLooper()).postDelayed({
            // Crée une intention pour retourner à l'écran de démarrage
            // REMARQUE : Remplacez MainActivity::class.java par votre véritable activité de menu principal si elle a un nom différent.
            val intent = Intent(this, MainActivity::class.java)

            // Ces "flags" effacent l'historique des écrans pour que le joueur
            // ne puisse pas revenir en arrière dans le jeu terminé.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

            startActivity(intent)
            finish() // Termine cette activité (GameWinActivity)
        }, WIN_SCREEN_DURATION)
    }
}