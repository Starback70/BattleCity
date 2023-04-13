package ru.izotov.battlecity.drawers

import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
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
    private var moveAllTanksThread: Thread? = null
    
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
        Thread {
            while (enemyAmount < MAX_ENEMY_AMOUNT) {
                drawEnemy()
                enemyAmount++
                Thread.sleep(TIME_ENEMY_RESPAWN)
            }
        }.start()
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
            ), DOWN, BulletDrawer(container, elements, this)
        )
        enemyTank.element.drawElement(container)
        tanks.add(enemyTank)
    }
    
    fun moveEnemyTanks() {
        Thread {
            while (true) {
                goThroughAllTanks()
                Thread.sleep(ENEMY_SPEED)
            }
        }.start()
    }
    
    private fun goThroughAllTanks() {
        moveAllTanksThread = Thread{
            tanks.forEach {
                it.move(it.direction, container, elements)
                if (checkIfChanceBiggerThanRandom(CHANCE_OF_SHOT)) {
                    it.bulletDrawer.makeBulletMove(it)
                }
            }
        }
        moveAllTanksThread?.start()
    }
    
    fun removeTank(tankIndex: Int) {
        if (tankIndex < 0) return
        moveAllTanksThread?.join()
        tanks.removeAt(tankIndex)
    }
}