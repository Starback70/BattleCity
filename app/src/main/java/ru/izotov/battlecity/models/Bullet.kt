package ru.izotov.battlecity.models

import android.view.View
import ru.izotov.battlecity.enums.Direction

data class Bullet(
    val view: View,
    val direction: Direction,
    val tank: Tank,
    var canMoveFurther: Boolean = true
)