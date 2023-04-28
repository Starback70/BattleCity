package ru.izotov.battlecity

import android.app.Activity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView

// object = singleton
class GameCore(private val activity: Activity) {
    @Volatile
    private var isPlay = false
    private var isPlayerTankOrBaseDestroyed = false
    
    fun startOrPauseTheGame() {
        isPlay = !isPlay
    }
    
    fun isPlaying() = isPlay && !isPlayerTankOrBaseDestroyed
    
    fun pauseTheGame() {
        isPlay = false
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