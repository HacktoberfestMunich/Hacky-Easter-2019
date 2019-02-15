package io.github.poeschl.pixelflutchallenge.shared

import java.awt.Color

data class Point(val x: Int, val y: Int)

data class Pixel(val point: Point, val color: Color)
