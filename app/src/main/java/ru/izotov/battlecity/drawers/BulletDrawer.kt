package ru.izotov.battlecity.drawers

import android.app.Activity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.GameCore.isPlaying
import ru.izotov.battlecity.R
import ru.izotov.battlecity.SoundManager
import ru.izotov.battlecity.enums.Direction
import ru.izotov.battlecity.enums.Direction.*
import ru.izotov.battlecity.enums.Material
import ru.izotov.battlecity.models.Bullet
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.models.Tank
import ru.izotov.battlecity.utils.*

private const val BULLET_WIDTH = 15
private const val BULLET_HEIGHT = 25

class BulletDrawer(
    private val container: FrameLayout,
    private val elements: MutableList<Element>,
    private val enemyDrawer: EnemyDrawer
) {
    init {
        moveAllBullets()
    }
    
    fun addNewBulletForTank(tank: Tank) {
        val view = container.findViewById<View>(tank.element.viewId) ?: return
        if (tank.alreadyHasBullet()) return
        allBullets.add(Bullet(createBullet(view, tank.direction), tank.direction, tank))
        SoundManager.bulletShot()
    }
    
    private fun Tank.alreadyHasBullet(): Boolean = allBullets.firstOrNull { it.tank == this } != null
    
    private var allBullets = mutableListOf<Bullet>()
    private fun moveAllBullets() {
        Thread {
            while (true ) {
                if (!isPlaying()) {
                    continue
                }
                interactWithAllBullets()
                Thread.sleep(30)
            }
        }.start()
    }
    
    private fun interactWithAllBullets() {
        allBullets.toList().forEach { bullet ->
            val view = bullet.view
            if (bullet.canBulletGoFurther()) {
                when (bullet.direction) {
                    UP -> (view.layoutParams as FrameLayout.LayoutParams).topMargin -= BULLET_HEIGHT
                    DOWN -> (view.layoutParams as FrameLayout.LayoutParams).topMargin += BULLET_HEIGHT
                    LEFT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin -= BULLET_HEIGHT
                    RIGHT -> (view.layoutParams as FrameLayout.LayoutParams).leftMargin += BULLET_HEIGHT
                }
                chooseBehaviorInTermsOfDirections(bullet)
                container.runOnUiThread {
                    container.removeView(view)
                    container.addView(view)
                }
            } else {
                stopBullet(bullet)
            }
            bullet.stopIntersectingBullets()
        }
        removeIntersectingBullets()
    }
    
    private fun removeIntersectingBullets() {
        val removingList = allBullets.filter { !it.canBulletGoFurther() }
        removingList.forEach {
            container.runOnUiThread { container.removeView(it.view) }
        }
        allBullets.removeAll(removingList)
    }
    
    private fun Bullet.stopIntersectingBullets() {
        val bulletCoordinate = this.view.getViewCoordinate()
        for (bulletInList in allBullets) {
            val coordinateInList = bulletInList.view.getViewCoordinate()
            if (this == bulletInList) {
                continue
            }
            if (coordinateInList == bulletCoordinate) {
                stopBullet(this)
                stopBullet(bulletInList)
                return
            }
        }
    }
    
    private fun Bullet.canBulletGoFurther() =
        this.view.checkViewCanMoveThroughBorder(this.view.getViewCoordinate()) && canMoveFurther
    
    
    private fun chooseBehaviorInTermsOfDirections(bullet: Bullet) {
        when (bullet.direction) {
            DOWN, UP -> {
                compareCollections(getCoordinatesForTopOrBottomDirection(bullet), bullet)
            }
            LEFT, RIGHT -> {
                compareCollections(getCoordinatesForLeftOrRightDirection(bullet), bullet)
            }
        }
    }
    
    private fun compareCollections(
        detectedCoordinatesList: List<Coordinate>,
        bullet: Bullet
    ) {
        for (coordinate in detectedCoordinatesList) {
            var element = getElementByCoordinates(coordinate, elements)
            if (element == null) {
                element = getTankByCoordinates(coordinate, enemyDrawer.tanks)
            }
            if (element == bullet.tank.element) {
                continue
            }
            removeElementsAndStopBullet(element, bullet)
        }
    }
    
    private fun removeElementsAndStopBullet(
        element: Element?,
        bullet: Bullet
    ) {
        if (element != null) {
            if (element.material.bulletCanGoThrough) {
                return
            }
            if (bullet.tank.element.material == Material.ENEMY_TANK && element.material == Material
                    .ENEMY_TANK
            ) {
                stopBullet(bullet)
                return
            }
            if (element.material.simpleBulletCanDestroy) {
                stopBullet(bullet)
                removeView(element)
                elements.remove(element)
                removeTank(element)
            } else {
                stopBullet(bullet)
            }
        }
    }
    
    private fun removeTank(element: Element) {
        val tanksElements = enemyDrawer.tanks.map { it.element }
        val tankIndex = tanksElements.indexOf(element)
        if (tankIndex < 0) return
        SoundManager.bulletBurst()
        enemyDrawer.removeTank(tankIndex)
    }
    
    private fun stopBullet(bullet: Bullet) {
        bullet.canMoveFurther = false
    }
    
    private fun removeView(element: Element) {
        val activity = container.context as Activity
        activity.runOnUiThread {
            container.removeView(activity.findViewById(element.viewId))
        }
    }
    
    
    private fun getCoordinatesForTopOrBottomDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val leftCell = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        val rightCell = leftCell + CELL_SIZE
        val topCoordinate = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        return listOf(
            Coordinate(topCoordinate, leftCell), Coordinate(topCoordinate, rightCell)
        )
    }
    
    private fun getCoordinatesForLeftOrRightDirection(bullet: Bullet): List<Coordinate> {
        val bulletCoordinate = bullet.view.getViewCoordinate()
        val topCell = bulletCoordinate.top - bulletCoordinate.top % CELL_SIZE
        val bottomCell = topCell + CELL_SIZE
        val leftCoordinate = bulletCoordinate.left - bulletCoordinate.left % CELL_SIZE
        return listOf(
            Coordinate(topCell, leftCoordinate), Coordinate(bottomCell, leftCoordinate)
        )
    }
    
    private fun createBullet(myTank: View, currentDirection: Direction): ImageView {
        return ImageView(container.context).apply {
            this.setImageResource(R.drawable.bullet)
            this.layoutParams = FrameLayout.LayoutParams(BULLET_WIDTH, BULLET_HEIGHT)
            val bulletCoordinate = getBulletCoordinates(this, myTank, currentDirection)
            (this.layoutParams as FrameLayout.LayoutParams).topMargin = bulletCoordinate.top
            (this.layoutParams as FrameLayout.LayoutParams).leftMargin = bulletCoordinate.left
            this.rotation = currentDirection.rotation
        }
    }
    
    private fun getBulletCoordinates(
        bullet: ImageView, myTank: View, currentDirection: Direction
    ): Coordinate {
        val tankLeftTopCoordinate = Coordinate(myTank.top, myTank.left)
        return when (currentDirection) {
            UP -> Coordinate(
                top = tankLeftTopCoordinate.top - bullet.layoutParams.height,
                left = getDistanceToMiddleOfTank(tankLeftTopCoordinate.left, bullet.layoutParams.width)
            )
            DOWN -> Coordinate(
                top = tankLeftTopCoordinate.top + myTank.layoutParams.height,
                left = getDistanceToMiddleOfTank(tankLeftTopCoordinate.left, bullet.layoutParams.width)
            )
            LEFT -> Coordinate(
                top = getDistanceToMiddleOfTank(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                left = tankLeftTopCoordinate.left - bullet.layoutParams.width
            )
            RIGHT -> Coordinate(
                top = getDistanceToMiddleOfTank(tankLeftTopCoordinate.top, bullet.layoutParams.height),
                left = tankLeftTopCoordinate.left + myTank.layoutParams.width
            )
        }
    }
    
    private fun getDistanceToMiddleOfTank(startCoordinate: Int, bulletSize: Int): Int {
        return startCoordinate + (CELL_SIZE - bulletSize / 2)
    }
}