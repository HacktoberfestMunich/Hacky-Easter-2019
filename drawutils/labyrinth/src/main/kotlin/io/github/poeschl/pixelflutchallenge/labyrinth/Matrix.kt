package io.github.poeschl.pixelflutchallenge.labyrinth

import io.github.poeschl.pixelflutchallenge.shared.Pixel
import io.github.poeschl.pixelflutchallenge.shared.Point

class PixelMatrix(private val xSize: Int, private val ySize: Int) {

    private val dataArray = initDataArray()

    fun insert(pixel: Pixel) {
        val coord = pixel.point
        dataArray[coord.y][coord.x] = pixel
    }

    fun remove(point: Point) {
        dataArray[point.y][point.x] = null
    }

    fun getPixelSet(): Set<Pixel> {
        return dataArray.flatten().filter { it != null }.map { it!! }.toSet()
    }

    private fun initDataArray(): Array<Array<Pixel?>> {
        return Array<Array<Pixel?>>(ySize + 1) { Array(xSize + 1) { null } }
    }
}
