
package com.example.space_invaders.game.activity

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

}