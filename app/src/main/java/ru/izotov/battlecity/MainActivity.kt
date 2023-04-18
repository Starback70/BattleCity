package ru.izotov.battlecity

import android.annotation.SuppressLint
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import ru.izotov.battlecity.GameCore.isPlaying
import ru.izotov.battlecity.GameCore.startOrPauseTheGame
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

const val CELL_SIZE = 50
const val VERTICAL_CELL_AMOUNT = 38
const val HORIZONTAL_CELL_AMOUNT = 25
const val VERTICAL_MAX_SIZE = CELL_SIZE * VERTICAL_CELL_AMOUNT
const val HORIZONTAL_MAX_SIZE = CELL_SIZE * HORIZONTAL_CELL_AMOUNT
const val HALF_WIDTH_OF_CONTAINER = VERTICAL_MAX_SIZE / 2

class MainActivity : AppCompatActivity() {
    private lateinit var item: MenuItem
    private lateinit var editor_clear: ImageView
    private lateinit var editor_brick: ImageView
    private lateinit var editor_concrete: ImageView
    private lateinit var editor_grass: ImageView
    private lateinit var container: FrameLayout
    private lateinit var materials_container: LinearLayout
    private var editMode = false
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
            enemyDrawer
        )
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
        EnemyDrawer(container, elementsDrawer.elementsOnContainer)
    }
    
    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        container.layoutParams = FrameLayout.LayoutParams(VERTICAL_MAX_SIZE, HORIZONTAL_MAX_SIZE)
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
        container = findViewById(R.id.container)
        materials_container = findViewById(R.id.materials_container)
        editor_clear = findViewById(R.id.editor_clear)
        editor_brick = findViewById(R.id.editor_brick)
        editor_concrete = findViewById(R.id.editor_concrete)
        editor_grass = findViewById(R.id.editor_grass)
        enemyDrawer.bulletDrawer = bulletDrawer
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
                startOrPauseTheGame()
                if (isPlaying()) {
                    startTheGame()
                } else {
                    pauseTheGame()
                }
                startTheGame()
                
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun startTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_pause)
        enemyDrawer.startEnemyCreation()
    }
    
    private fun pauseTheGame() {
        item.icon = ContextCompat.getDrawable(this, R.drawable.ic_play)
        GameCore.pauseTheGame()
    }
    
    override fun onPause() {
        super.onPause()
        GameCore.pauseTheGame()
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
        if (!isPlaying()) {
            return super.onKeyDown(keyCode, event)
        }
        when (keyCode) {
            KEYCODE_DPAD_UP -> move(UP)
            KEYCODE_DPAD_LEFT -> move(LEFT)
            KEYCODE_DPAD_DOWN -> move(DOWN)
            KEYCODE_DPAD_RIGHT -> move(RIGHT)
            KEYCODE_SPACE -> bulletDrawer.addNewBulletForTank(playerTank)
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun move(direction: Direction) {
        playerTank.move(direction, container, elementsDrawer.elementsOnContainer)
    }
}