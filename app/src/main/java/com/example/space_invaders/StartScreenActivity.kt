package com.example.space_invaders

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_screen)

        findViewById<Button>(R.id.buttonGo).setOnClickListener {
            startActivity(Intent(this, LevelIntroActivity::class.java))
        }

        findViewById<Button>(R.id.buttonOut).setOnClickListener {
            finishAffinity()
        }
    }
}