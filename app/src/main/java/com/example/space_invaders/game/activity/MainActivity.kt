
/*package com.example.space_invaders.game.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.space_invaders.game.SpaceInvadersView

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: SpaceInvadersView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gameView = SpaceInvadersView(this) {
             // Cette lambda sera appelée quand le jeu se termine
            finish() // Retourne à l'écran précédent (StartScreenActivity)
        }
        setContentView(gameView)
    }

}*/
package com.example.space_invaders.game.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.space_invaders.game.SpaceInvadersView

class MainActivity : AppCompatActivity() {
    private lateinit var gameView: SpaceInvadersView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- MODIFICATION PRINCIPALE ICI ---
        // On crée la vue du jeu en lui donnant deux instructions :
        // 1. Quoi faire en cas de défaite (onGameOver)
        // 2. Quoi faire en cas de victoire (onGameWin)
        gameView = SpaceInvadersView(this,
            onGameOver = {
                // Lance l'activité de Game Over
                val intent = Intent(this, GameOverActivity::class.java)
                startActivity(intent)
                finish() // On termine l'activité du jeu
            },
            onGameWin = {
                // Lance l'activité de Victoire
                val intent = Intent(this, GameWinActivity::class.java)
                startActivity(intent)
                finish() // On termine l'activité du jeu
            }
        )

        setContentView(gameView)
    }
}