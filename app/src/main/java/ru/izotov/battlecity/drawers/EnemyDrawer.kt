package ru.izotov.battlecity.drawers

import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.enums.Direction.*
import ru.izotov.battlecity.enums.Material.ENEMY_TANK
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.models.Tank
import ru.izotov.battlecity.utils.drawElement

private const val MAX_ENEMY_AMOUNT = 20
private const val TIME_ENEMY_RESPAWN: Long = 3000
private const val ENEMY_SPEED: Long = 400

class EnemyDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>
) {
    private val respawnList: List<Coordinate>
    private var enemyCount = 0
    private var currentCoordinate: Coordinate
    private val tanks = mutableListOf<Tank>()
    
    init {
        respawnList = getRespawnList()
        currentCoordinate = respawnList[0]
    }
    
    private fun getRespawnList(): List<Coordinate> {
        val respawnList = mutableListOf<Coordinate>()
        respawnList.add(Coordinate(0, 0))
        respawnList.add(Coordinate(0, container.width / 2 - CELL_SIZE))
        respawnList.add(Coordinate(0, container.width - 2 * CELL_SIZE))
        return respawnList
    }
    
    fun startEnemyCreation() {
        Thread {
            while (enemyCount < MAX_ENEMY_AMOUNT) {
                enemyCount++;
                drawEnemy()
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
                coordinate = currentCoordinate,
                width = ENEMY_TANK.width,
                height = ENEMY_TANK.height
            ), DOWN
        )
        enemyTank.element.drawElement(container)
        elements.add(enemyTank.element)
        tanks.add(enemyTank)
    }
    
    fun moveEnemyTanks() {
        Thread {
            while (true) {
                removeInconsistentTanks()
                tanks.forEach {
                    it.move(it.direction, container, elements)
                }
                Thread.sleep(ENEMY_SPEED)
            }
        }.start()
    }
    
    private fun removeInconsistentTanks() {
        tanks.removeAll(getInconsistentTanks())
    }
    
    private fun getInconsistentTanks(): List<Tank> {
        val removingTanks = mutableListOf<Tank>()
        val allTanksElements = elements.filter { it.material == ENEMY_TANK }
        tanks.forEach {
            if (!allTanksElements.contains(it.element)) {
                removingTanks.add(it)
            }
        }
        return removingTanks
    }
}