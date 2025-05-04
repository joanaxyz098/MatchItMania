package music

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.csit284.matchitmania.R

object SoundEffects {
    private lateinit var soundPool: SoundPool
    private var matchSound = 0
    private var tapSound = 0
    private var winSound = 0
    private var loseSound = 0
    private var isLoaded = false
    private var isEnabled = true

    fun init(context: Context) {
        if (isLoaded) return

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(audioAttributes)
            .build()

        matchSound = soundPool.load(context, R.raw.match, 1)
        tapSound = soundPool.load(context, R.raw.block_click, 1)
        winSound = soundPool.load(context, R.raw.game_win, 1)
        loseSound = soundPool.load(context, R.raw.game_lose, 1)

        isLoaded = true
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }

    fun playMatch() {
        if (isEnabled) soundPool.play(matchSound, 1f, 1f, 1, 0, 1f)
    }

    fun playTap() {
        if (isEnabled) soundPool.play(tapSound, 1f, 1f, 1, 0, 1f)
    }

    fun playWin() {
        if (isEnabled) soundPool.play(winSound, 1f, 1f, 1, 0, 1f)
    }

    fun playLose() {
        if (isEnabled) soundPool.play(loseSound, 1f, 1f, 1, 0, 1f)
    }
}
