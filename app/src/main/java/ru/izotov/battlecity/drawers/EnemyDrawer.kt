package ru.izotov.battlecity.drawers

import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.GameCore
import ru.izotov.battlecity.HALF_WIDTH_OF_CONTAINER
import ru.izotov.battlecity.VERTICAL_MAX_SIZE
import ru.izotov.battlecity.enums.Direction.*
import ru.izotov.battlecity.enums.Material.ENEMY_TANK
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.models.Tank
import ru.izotov.battlecity.utils.checkIfChanceBiggerThanRandom
import ru.izotov.battlecity.utils.drawElement

private const val MAX_ENEMY_AMOUNT = 20
private const val TIME_ENEMY_RESPAWN: Long = 3000
private const val ENEMY_SPEED: Long = 400
private const val CHANCE_OF_SHOT: Int = 10

class EnemyDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>
) {
    private val respawnList: List<Coordinate>
    private var enemyAmount = 0
    private var currentCoordinate: Coordinate
    val tanks = mutableListOf<Tank>()
    lateinit var bulletDrawer: BulletDrawer
    private var gameStarted = false
    
    init {
        respawnList = getRespawnList()
        currentCoordinate = respawnList[0]
    }
    
    private fun getRespawnList(): List<Coordinate> {
        val respawnList = mutableListOf<Coordinate>()
        respawnList.add(Coordinate(0, 0))
        respawnList.add(Coordinate(0, HALF_WIDTH_OF_CONTAINER - CELL_SIZE))
        respawnList.add(Coordinate(0, VERTICAL_MAX_SIZE - 2 * CELL_SIZE))
        return respawnList
    }
    
    fun startEnemyCreation() {
        if (gameStarted) {
            return
        }
        gameStarted = true
        Thread {
            while (enemyAmount < MAX_ENEMY_AMOUNT) {
                if (!GameCore.isPlaying()) {
                    continue
                }
                drawEnemy()
                enemyAmount++
                Thread.sleep(TIME_ENEMY_RESPAWN)
            }
        }.start()
        moveEnemyTanks()
    }
    
    private fun drawEnemy() {
        var index = respawnList.indexOf(currentCoordinate) + 1
        if (index == respawnList.size) {
            index = 0
        }
        currentCoordinate = respawnList[index]
        val enemyTank = Tank(
            Element(
                material = ENEMY_TANK,
                coordinate = currentCoordinate
            ), DOWN, this
        )
        enemyTank.element.drawElement(container)
        tanks.add(enemyTank)
    }
    
    private fun moveEnemyTanks() {
        Thread {
            while (true) {
                if (!GameCore.isPlaying()) {
                    continue
                }
                goThroughAllTanks()
                Thread.sleep(ENEMY_SPEED)
            }
        }.start()
    }
    
    private fun goThroughAllTanks() {
        tanks.toList().forEach {
            it.move(it.direction, container, elements)
            if (checkIfChanceBiggerThanRandom(CHANCE_OF_SHOT)) {
                bulletDrawer.addNewBulletForTank(it)
            }
        }
    }
    
    fun removeTank(tankIndex: Int) {
        if (tankIndex < 0) return
        tanks.removeAt(tankIndex)
    }
}