package ru.izotov.battlecity.drawers

import android.view.View
import android.widget.FrameLayout
import ru.izotov.battlecity.CELL_SIZE


class GridDrawer(private val container: FrameLayout) {
    private val allLines = mutableListOf<View>()
    
    fun removeGrid() {
        allLines.forEach{
            container.removeView(it)
        }
    }
    
    fun drawGrid() {
        drawHorizontalLines()
        drawVerticalLines()
    }
    
    private fun drawHorizontalLines() {
        var topMargin = 0
        while (topMargin <= container.layoutParams.height) {
            val line = View(container.context)
            val layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 2)
            topMargin += CELL_SIZE
            layoutParams.topMargin = topMargin
            line.layoutParams = layoutParams
            line.setBackgroundColor(this.container.resources.getColor(android.R.color.white))
            allLines.add(line)
            container.addView(line)
        }
    }
    
    private fun drawVerticalLines() {
        var leftMargin = 0
        while (leftMargin <= container.layoutParams.width) {
            val line = View(container.context)
            val layoutParams = FrameLayout.LayoutParams(2, FrameLayout.LayoutParams.MATCH_PARENT)
            leftMargin += CELL_SIZE
            layoutParams.leftMargin = leftMargin
            line.layoutParams = layoutParams
            line.setBackgroundColor(this.container.resources.getColor(android.R.color.white))
            allLines.add(line)
            container.addView(line)
        }
    }
    
}