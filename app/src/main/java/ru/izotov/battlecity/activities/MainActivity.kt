package ru.izotov.battlecity.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.KeyEvent.*
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ru.izotov.battlecity.GameCore
import ru.izotov.battlecity.LevelStorage
import ru.izotov.battlecity.ProgressIndicator
import ru.izotov.battlecity.R
import ru.izotov.battlecity.drawers.BulletDrawer
import ru.izotov.battlecity.drawers.ElementsDrawer
import ru.izotov.battlecity.drawers.EnemyDrawer
import ru.izotov.battlecity.drawers.GridDrawer
import ru.izotov.battlecity.enums.Direction
import ru.izotov.battlecity.enums.Direction.*
import ru.izotov.battlecity.enums.Material.*
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.models.Tank
import ru.izotov.battlecity.sounds.MainSoundPlayer

const val CELL_SIZE = 50
const val VERTICAL_CELL_AMOUNT = 38
const val HORIZONTAL_CELL_AMOUNT = 25
const val VERTICAL_MAX_SIZE = CELL_SIZE * VERTICAL_CELL_AMOUNT
const val HORIZONTAL_MAX_SIZE = CELL_SIZE * HORIZONTAL_CELL_AMOUNT
const val HALF_WIDTH_OF_CONTAINER = VERTICAL_MAX_SIZE / 2

class MainActivity : AppCompatActivity(), ProgressIndicator {
    private lateinit var editor_clear: ImageView
    private lateinit var editor_brick: ImageView
    private lateinit var editor_concrete: ImageView
    private lateinit var editor_grass: ImageView
    private lateinit var total_container: FrameLayout
    private lateinit var container: FrameLayout
    private lateinit var init_title: TextView
    private lateinit var materials_container: LinearLayout
    private var editMode = false
    private lateinit var item: MenuItem
    private var gameStarted = false
    private val playerTank by lazy {
        Tank(
            Element(
                material = PLAYER_TANK,
                coordinate = getPlayerTankCoordinate()
            ), UP, enemyDrawer
        )
    }
    
    private val bulletDrawer by lazy {
        BulletDrawer(
            container,
            elementsDrawer.elementsOnContainer,
            enemyDrawer,
            soundManager,
            gameCore
        )
    }
    
    private val gameCore by lazy {
        GameCore(this)
    }
    
    private val soundManager by lazy {
        MainSoundPlayer(this, this)
    }
    
    private fun getPlayerTankCoordinate() = Coordinate(
        top = HORIZONTAL_MAX_SIZE - PLAYER_TANK.height * CELL_SIZE,
        left = HALF_WIDTH_OF_CONTAINER - 8 * CELL_SIZE
    )
    
    private val eagle = Element(
        material = EAGLE,
        coordinate = getEagleCoordinate()
    )
    
    
    private fun getEagleCoordinate() = Coordinate(
        top = HORIZONTAL_MAX_SIZE - EAGLE.height * CELL_SIZE,
        left = HALF_WIDTH_OF_CONTAINER - EAGLE.width * CELL_SIZE / 2
    )
    
    private val gridDrawer by lazy {
        GridDrawer(container)
    }
    
    private val elementsDrawer by lazy {
        ElementsDrawer(container)
    }
    
    private val levelStorage by lazy {
        LevelStorage(this)
    }
    
    private val enemyDrawer by lazy {
        EnemyDrawer(
            container,
            elementsDrawer.elementsOnContainer,
            soundManager,
            gameCore
        )
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        enemyDrawer.bulletDrawer = bulletDrawer
        container.layoutParams = FrameLayout.LayoutParams(
            VERTICAL_MAX_SIZE,
            HORIZONTAL_MAX_SIZE
        )
        editor_clear.setOnClickListener { elementsDrawer.currentMaterial = EMPTY }
        editor_brick.setOnClickListener { elementsDrawer.currentMaterial = BRICK }
        editor_concrete.setOnClickListener { elementsDrawer.currentMaterial = CONCRETE }
        editor_grass.setOnClickListener { elementsDrawer.currentMaterial = GRASS }
        container.setOnTouchListener { _, event ->
            if (!editMode) {
                return@setOnTouchListener true
            }
            elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        elementsDrawer.drawElementsList(listOf(playerTank.element, eagle))
        hideSettings()
    }
    
    
    private fun init() {
        total_container = findViewById(R.id.total_container)
        container = findViewById(R.id.container)
        materials_container = findViewById(R.id.materials_container)
        editor_clear = findViewById(R.id.editor_clear)
        editor_brick = findViewById(R.id.editor_brick)
        editor_concrete = findViewById(R.id.editor_concrete)
        editor_grass = findViewById(R.id.editor_grass)
        init_title = findViewById(R.id.init_title)
        soundManager.loadSounds()
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        item = menu.findItem(R.id.menu_play)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_settings -> {
                switchEditMode()
                true
            }
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                true
            }
            R.id.menu_play -> {
                if (editMode) {
                    return true
                }
                showIntro()
                if (soundManager.areSoundsReady()) {
                    gameCore.startOrPauseTheGame()
                    if (gameCore.isPlaying()) {
                        resumeTheGame()
                    } else {
                        pauseTheGame()
                    }
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun resumeTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause)
        gameCore.resumeTheGame()
    }
    
    private fun showIntro() {
        if (gameStarted) {
            return
        }
        gameStarted = true
        soundManager.loadSounds()
    }
    
    private fun pauseTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_play)
        gameCore.pauseTheGame()
        soundManager.pauseSounds()
    }
    
    override fun onPause() {
        super.onPause()
        pauseTheGame()
    }
    
    private fun switchEditMode() {
        editMode = !editMode
        if (editMode) {
            showSettings()
        } else {
            hideSettings()
        }
    }
    
    private fun showSettings() {
        gridDrawer.drawGrid()
        materials_container.visibility = VISIBLE
    }
    
    private fun hideSettings() {
        gridDrawer.removeGrid()
        materials_container.visibility = GONE
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyDown(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP -> onButtonPressed(UP)
            KEYCODE_DPAD_LEFT -> onButtonPressed(LEFT)
            KEYCODE_DPAD_DOWN -> onButtonPressed(DOWN)
            KEYCODE_DPAD_RIGHT -> onButtonPressed(RIGHT)
            KEYCODE_SPACE -> bulletDrawer.addNewBulletForTank(playerTank)
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun onButtonPressed(direction: Direction) {
        soundManager.tankMove()
        playerTank.move(direction, container, elementsDrawer.elementsOnContainer)
    }
    
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (!gameCore.isPlaying()) {
            return super.onKeyUp(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP, KEYCODE_DPAD_LEFT, KEYCODE_DPAD_DOWN, KEYCODE_DPAD_RIGHT -> onButtonReleased()
        }
        return super.onKeyUp(keyCode, event)
    }
    
    private fun onButtonReleased() {
        if (enemyDrawer.tanks.isEmpty()) {
            soundManager.tankStop()
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == SCORE_REQUEST_CODE) {
            recreate()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    
    override fun showProgress() {
        container.visibility = GONE
        total_container.setBackgroundResource(R.color.gray)
        init_title.visibility = VISIBLE
    }
    
    override fun dismissProgress() {
        container.visibility = VISIBLE
        total_container.setBackgroundResource(R.color.black)
        init_title.visibility = GONE
        enemyDrawer.startEnemyCreation()
        soundManager.playIntroMusic()
        resumeTheGame()
    }
}