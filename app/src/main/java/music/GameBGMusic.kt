package music

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.csit284.matchitmania.R

object GameBGMusic {
    var mediaPlayer: MediaPlayer? = null
    private var isInitialized = false

    fun initialize(context: Context) {
        if (!isInitialized) {
            try {
                mediaPlayer = MediaPlayer.create(context, R.raw.in_game)
                mediaPlayer?.isLooping = true
                isInitialized = true
                mediaPlayer?.setVolume(0.2f, 0.5f) // 🔊 Set volume to 20%
                Log.d("BackgroundMusic", "Music initialized successfully")
            } catch (e: Exception) {
                Log.e("BackgroundMusic", "Failed to initialize music: ${e.message}")
            }
        }
    }

    fun play() {
        try {
            if (mediaPlayer?.isPlaying == false) {
                mediaPlayer?.start()
                Log.d("BackgroundMusic", "Music started playing")
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Failed to play music: ${e.message}")
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                Log.d("BackgroundMusic", "Music paused")
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Failed to pause music: ${e.message}")
        }
    }

    fun release() {
        try {
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
            Log.d("BackgroundMusic", "Music released")
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Failed to release music: ${e.message}")
        }
    }

    fun stop() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
            }
            mediaPlayer?.release()
            mediaPlayer = null
            isInitialized = false
            Log.d("BackgroundMusic", "Music stopped and resources released")
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Failed to stop music: ${e.message}")
        }
    }
}
