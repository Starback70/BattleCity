package ru.izotov.battlecity.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import ru.izotov.battlecity.R
import ru.izotov.battlecity.sounds.ScoreSoundPlayer

const val SCORE_REQUEST_CODE = 100

class ScoreActivity : AppCompatActivity() {
    private lateinit var score_text_view: TextView
    
    companion object {
        const val EXTRA_SCORE = "extra_score"
        
        fun createIntent(context: Context, score: Int): Intent {
            return Intent(context, ScoreActivity::class.java)
                .apply {
                    putExtra(EXTRA_SCORE, score)
                }
        }
    }
    
    private val scoreSoundPlayer by lazy {
        ScoreSoundPlayer(this, soundReadyListener = {
            startScoreCounting()
        })
    }
    
    private fun startScoreCounting() {
        Thread {
            var currentScore = 0
            while (currentScore <= score) {
                runOnUiThread {
                    score_text_view.text = currentScore.toString()
                    currentScore += 100
                }
                Thread.sleep(150)
            }
            scoreSoundPlayer.pauseScoreSound()
        }.start()
    }
    
    var score = 0
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_score)
        score = intent.getIntExtra(EXTRA_SCORE, 0)
        scoreSoundPlayer.playScoreSound()
        score_text_view = findViewById(R.id.score_text_view)
    }
    
    override fun onBackPressed() {
        setResult(Activity.RESULT_OK)
        finish()
    }
    
    override fun onPause() {
        super.onPause()
        scoreSoundPlayer.pauseScoreSound()
    }
}