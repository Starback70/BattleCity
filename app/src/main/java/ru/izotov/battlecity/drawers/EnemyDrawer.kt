package ru.izotov.battlecity.drawers

import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.enums.Material.ENEMY_TANK
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.utils.drawElement

private const val MAX_ENEMY_AMOUNT = 20
private const val TIME_ENEMY_RESPAWN: Long = 3000

class EnemyDrawer(private val container: FrameLayout) {
    private val respawnList: List<Coordinate>
    private var enemyCount = 0
    private var currentCoordinate: Coordinate
    
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
    
    fun startEnemyDrawing(elements: MutableList<Element>) {
        Thread {
            while (enemyCount < MAX_ENEMY_AMOUNT) {
                enemyCount++;
                drawEnemy(elements)
                Thread.sleep(TIME_ENEMY_RESPAWN)
            }
        }.start()
    }
    
    private fun drawEnemy(elements: MutableList<Element>) {
        var index = respawnList.indexOf(currentCoordinate) + 1
        if (index == respawnList.size) {
            index = 0
        }
        currentCoordinate = respawnList[index]
        val enemyTankElement = Element(
            material = ENEMY_TANK,
            coordinate = currentCoordinate,
            width = ENEMY_TANK.width,
            height = ENEMY_TANK.height
        )
        enemyTankElement.drawElement(container)
        elements.add(enemyTankElement)
    }
}