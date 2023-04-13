package ru.izotov.battlecity.models

import android.view.View
import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE
import ru.izotov.battlecity.enums.Direction
import ru.izotov.battlecity.utils.checkViewCanMoveThroughBorder
import ru.izotov.battlecity.utils.getElementByCoordinates

class Tank(
    val element: Element,
    var direction: Direction
) {
    fun move(
        direction: Direction,
        container: FrameLayout,
        elementsOnContainer: List<Element>
    ) {
        val view = container.findViewById<View>(element.viewId) ?: return
        val currentCoordinate = getTankCurrentCoordinate(view) //
        // safe before
        // change
        this.direction = direction
        view.rotation = direction.rotation
        val nextCoordinate = getTankNextCoordinate(view) // safe after change
        if (view.checkViewCanMoveThroughBorder(nextCoordinate)
            && element.checkTankCanMoveTroughMaterial(nextCoordinate, elementsOnContainer)
        ) {
            container.removeView(view)
            container.addView(view, 0)
            element.coordinate = nextCoordinate
        } else {
            (view.layoutParams as FrameLayout.LayoutParams).topMargin = currentCoordinate.top
            (view.layoutParams as FrameLayout.LayoutParams).leftMargin = currentCoordinate.left
        }
    }
    
    private fun getTankCurrentCoordinate(tank: View): Coordinate {
        return Coordinate(
            (tank.layoutParams as FrameLayout.LayoutParams).topMargin,
            (tank.layoutParams as FrameLayout.LayoutParams).leftMargin,
        )
    }
    
    private fun getTankNextCoordinate(view: View): Coordinate {
        val layoutParams = view.layoutParams as FrameLayout.LayoutParams
        when (direction) {
            Direction.UP -> {
                view.rotation = 0f
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += -CELL_SIZE
            }
            Direction.DOWN -> {
                view.rotation = 180f
                (view.layoutParams as FrameLayout.LayoutParams).topMargin += CELL_SIZE
            }
            Direction.RIGHT -> {
                view.rotation = 90f
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += CELL_SIZE
            }
            Direction.LEFT -> {
                view.rotation = 270f
                (view.layoutParams as FrameLayout.LayoutParams).leftMargin += -CELL_SIZE
            }
        }
        return Coordinate(layoutParams.topMargin, layoutParams.leftMargin)
    }
    
    
    private fun Element.checkTankCanMoveTroughMaterial(
        coordinate: Coordinate,
        elementsOnContainer: List<Element>
    ): Boolean {
        for (anyCoordinate in getTankCoordinates(coordinate)) {
            val element = getElementByCoordinates(anyCoordinate, elementsOnContainer)
            if (element != null && !element.material.tankCanGoThrough) {
                if (this == element) continue
                return false
            }
        }
        return true
    }
    
    private fun getTankCoordinates(leftTopCoordinate: Coordinate): List<Coordinate> {
        val coordinateList = mutableListOf<Coordinate>()
        coordinateList.add(leftTopCoordinate)
        coordinateList.add(
            Coordinate(
                leftTopCoordinate.top + CELL_SIZE,
                leftTopCoordinate.left
            )
        ) // bottom left
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