package ru.izotov.battlecity

import android.app.Activity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import ru.izotov.battlecity.activities.SCORE_REQUEST_CODE
import ru.izotov.battlecity.activities.ScoreActivity

// object = singleton
class GameCore(private val activity: Activity) {
    @Volatile
    private var isPlay = false
    private var isPlayerTankOrBaseDestroyed = false
    private var isPlayerWin = false
    
    fun startOrPauseTheGame() {
        isPlay = !isPlay
    }
    
    fun isPlaying() = isPlay && !isPlayerTankOrBaseDestroyed && !isPlayerWin
    
    fun pauseTheGame() {
        isPlay = false
    }
    
    fun playerWon(score: Int) {
        isPlayerWin = true
        activity.startActivityForResult(ScoreActivity.createIntent(activity, score), SCORE_REQUEST_CODE)
    }
    
    fun destroyPlayerOrBase() {
        isPlayerTankOrBaseDestroyed = true
        pauseTheGame()
        animateEndGame()
    }
    
    private fun animateEndGame() {
        activity.runOnUiThread {
            val endGameText = activity.findViewById<TextView>(R.id.game_over_text)
            endGameText.visibility = View.VISIBLE
            val slideUp = AnimationUtils.loadAnimation(activity, R.anim.slide_up)
            endGameText.startAnimation(slideUp)
        }
    }
}