package io.github.poeschl.pixelflutchallenge.borders

import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import io.github.poeschl.pixelflutchallenge.shared.drawHorizontalLine
import io.github.poeschl.pixelflutchallenge.shared.drawVerticalLine
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color


class Playbox(private val origin: Point, private val size: Pair<Int, Int>) {

    companion object {
        private val DEFAULT_BORDER_COLOR = Color.WHITE
        private const val SPLIT_COUNT = 2
    }

    fun draw(pixelFlutInterface: PixelFlutInterface) {
        drawBorder(pixelFlutInterface)
        drawInternalSplit(pixelFlutInterface)
    }

    private fun drawBorder(pixelFlutInterface: PixelFlutInterface) {
        runBlocking {
            launch {
                drawHorizontalLine(pixelFlutInterface, origin, size.first, DEFAULT_BORDER_COLOR)
            }
            launch {
                drawVerticalLine(pixelFlutInterface, origin, size.second, DEFAULT_BORDER_COLOR)
            }

            launch {
                drawHorizontalLine(pixelFlutInterface, origin.plus(Point(0, size.second - 1)), size.first, DEFAULT_BORDER_COLOR)
            }
            launch {
                drawVerticalLine(pixelFlutInterface, origin.plus(Point(size.first - 1, 0)), size.second, DEFAULT_BORDER_COLOR)
            }
        }
    }

    private fun drawInternalSplit(pixelFlutInterface: PixelFlutInterface) {
        val xSplit = size.first / SPLIT_COUNT
        val ySplit = size.second / SPLIT_COUNT

        runBlocking {
            launch {
                for (i: Int in 0 until SPLIT_COUNT) {
                    drawVerticalLine(pixelFlutInterface, origin.plus(Point(xSplit * i, 0)), size.second, DEFAULT_BORDER_COLOR)
                }
            }
            launch {
                for (i: Int in 0 until SPLIT_COUNT) {
                    drawHorizontalLine(pixelFlutInterface, origin.plus(Point(0, ySplit * i)), size.first, DEFAULT_BORDER_COLOR)
                }
            }
        }
    }
}
