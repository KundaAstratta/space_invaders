package com.example.space_invaders.audio

import android.content.Context
import android.media.MediaPlayer

class BackgroundSoundManager(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    // Méthode pour jouer un son spécifique
    fun playSound(soundResId: Int, loop: Boolean = true) {
        stopSound() // Arrêter tout son en cours

        mediaPlayer = MediaPlayer.create(context, soundResId)
        mediaPlayer?.isLooping = loop
        mediaPlayer?.start()
    }
    // Méthode pour arrêter le son
    fun stopSound() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    // Méthode pour changer le son
    fun changeSound(newSoundResId: Int) {
        stopSound()
        playSound(newSoundResId)
    }

    // Méthode à appeler lorsque l'activité ou la vue est détruite
    fun release() {
        stopSound()
    }
}
