package io.github.poeschl.pixelflutchallenge.shared

import java.awt.Color

data class Point(val x: Int, val y: Int) {

    fun plus(point: Point): Point {
        return Point(this.x + point.x, this.y + point.y)
    }
}

data class Pixel(val point: Point, val color: Color)
