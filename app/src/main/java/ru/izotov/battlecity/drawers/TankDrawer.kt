package ru.izotov.battlecity.drawers

import android.view.View
import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.enums.Direction
import ru.izotov.battlecity.models.Coordinate
import ru.izotov.battlecity.models.Element
import ru.izotov.battlecity.utils.checkViewCanMoveThroughBorder
import ru.izotov.battlecity.utils.getElementByCoordinates

class TankDrawer(private val container: FrameLayout) {
    var currentDirection = Direction.UP
    
    fun move(myTank: View, direction: Direction, elementsOnContainer: List<Element>) {
        val layoutParams = myTank.layoutParams as FrameLayout.LayoutParams
        val currentCoordinate = Coordinate(layoutParams.topMargin, layoutParams.leftMargin) // safe before
        // change
        currentDirection = direction
        myTank.rotation = direction.rotation
        when (direction) {
            Direction.UP -> {
                myTank.rotation = 0f
                (myTank.layoutParams as FrameLayout.LayoutParams).topMargin += -CELL_SIZE
            }
            Direction.DOWN -> {
                myTank.rotation = 180f
                (myTank.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            Direction.RIGHT -> {
                myTank.rotation = 90f
                (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
            Direction.LEFT -> {
                myTank.rotation = 270f
                (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin += -CELL_SIZE
            }
        }
        val nextCoordinate = Coordinate(layoutParams.topMargin, layoutParams.leftMargin) // safe after change
        if (myTank.checkViewCanMoveThroughBorder(nextCoordinate)
            && checkTankCanMoveTroughMaterial(nextCoordinate, elementsOnContainer)
        ) {
            container.removeView(myTank)
            container.addView(myTank, 0)
        } else {
            (myTank.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (myTank.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left
        }
    }
    
    
    private fun checkTankCanMoveTroughMaterial(
        coordinate: Coordinate,
        elementsOnContainer: List<Element>
    ): Boolean {
        getTankCoordinates(coordinate).forEach {
            val element = getElementByCoordinates(it, elementsOnContainer)
            if (element != null && !element.material.tankCanGoThrough) {
                return false
            }
        }
        return true
    }
    
    private fun getTankCoordinates(leftTopCoordinate: Coordinate): List<Coordinate> {
        val coordinateList = mutableListOf<Coordinate>()
        coordinateList.add(leftTopCoordinate)
        coordinateList.add(Coordinate(leftTopCoordinate.top + CELL_SIZE, leftTopCoordinate.left)) // bottom left
        coordinateList.add(Coordinate(leftTopCoordinate.top, leftTopCoordinate.left + CELL_SIZE)) // top right
        coordinateList.add(
            Coordinate(
                leftTopCoordinate.top + CELL_SIZE,
                leftTopCoordinate.left + CELL_SIZE
            )
        ) // bottom right
        return coordinateList
    }
}