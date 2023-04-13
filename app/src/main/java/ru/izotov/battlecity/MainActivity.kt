package ru.izotov.battlecity

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
import ru.izotov.battlecity.drawers.*
import ru.izotov.battlecity.enums.Direction
import ru.izotov.battlecity.enums.Direction.*
import ru.izotov.battlecity.enums.Material.*
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.models.Tank

const val CELL_SIZE = 50
const val HORIZONTAL_CELL_AMOUNT = 38
const val VERTICAL_CELL_AMOUNT = 25
const val HORIZONTAL_SIZE = CELL_SIZE * HORIZONTAL_CELL_AMOUNT
const val VERTICAL_SIZE = CELL_SIZE * VERTICAL_CELL_AMOUNT


class MainActivity : AppCompatActivity() {
    private var editMode = false
    private val playerTank = Tank(
        Element(
            R.id.myTank,
            PLAYER_TANK,
            Coordinate(0, 0),
            PLAYER_TANK.width,
            PLAYER_TANK.height
        ), UP
    )
    private lateinit var clear: ImageView
    private lateinit var brick: ImageView
    private lateinit var concrete: ImageView
    private lateinit var grass: ImageView
    private lateinit var eagle: ImageView
    private lateinit var myTank: ImageView
    private lateinit var container: FrameLayout
    private lateinit var materialsContainer: LinearLayout
    private val gridDrawer by lazy {
        GridDrawer(container)
    }
    
    private val elementsDrawer by lazy {
        ElementsDrawer(container)
    }
    
    private val bulletDrawer by lazy {
        BulletDrawer(container)
    }
    
    private val levelStorage by lazy {
        LevelStorage(this)
    }
    
    private val enemyDrawer by lazy {
        EnemyDrawer(container, elementsDrawer.elementsOnContainer)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
        container.layoutParams = FrameLayout.LayoutParams(HORIZONTAL_SIZE, VERTICAL_SIZE)
        clear.setOnClickListener { elementsDrawer.currentMaterial = EMPTY }
        brick.setOnClickListener { elementsDrawer.currentMaterial = BRICK }
        concrete.setOnClickListener { elementsDrawer.currentMaterial = CONCRETE }
        grass.setOnClickListener { elementsDrawer.currentMaterial = GRASS }
        eagle.setOnClickListener { elementsDrawer.currentMaterial = EAGLE }
        container.setOnTouchListener { _, event ->
            elementsDrawer.onTouchContainer(event.x, event.y)
            return@setOnTouchListener true
        }
        elementsDrawer.drawElementsList(levelStorage.loadLevel())
        hideSettings()
//        elementsDrawer.elementsOnContainer.add(playerTank.element)
    }
    
    private fun init() {
        container = findViewById(R.id.container)
        materialsContainer = findViewById(R.id.materials_container)
        myTank = findViewById(R.id.myTank)
        clear = findViewById(R.id.editor_clear)
        brick = findViewById(R.id.editor_brick)
        concrete = findViewById(R.id.editor_concrete)
        grass = findViewById(R.id.editor_grass)
        eagle = findViewById(R.id.editor_eagle)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.settings, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_setting -> {
                switchEditMode()
                true
            }
            R.id.menu_save -> {
                levelStorage.saveLevel(elementsDrawer.elementsOnContainer)
                true
            }
            R.id.menu_play -> {
                startGame()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun startGame() {
        if (editMode) {
            return
        }
        enemyDrawer.startEnemyCreation()
        enemyDrawer.moveEnemyTanks()
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
        materialsContainer.visibility = VISIBLE
    }
    
    private fun hideSettings() {
        gridDrawer.removeGrid()
        materialsContainer.visibility = GONE
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KEYCODE_DPAD_UP -> move(UP)
            KEYCODE_DPAD_LEFT -> move(LEFT)
            KEYCODE_DPAD_DOWN -> move(DOWN)
            KEYCODE_DPAD_RIGHT -> move(RIGHT)
            KEYCODE_SPACE -> bulletDrawer.makeBulletMove(
                myTank,
                playerTank.direction,
                elementsDrawer.elementsOnContainer
            )
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun move(direction: Direction) {
        playerTank.move(direction, container, elementsDrawer.elementsOnContainer)
        
    }
}