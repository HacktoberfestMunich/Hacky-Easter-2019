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
    }

    fun draw(pixelFlutInterfac: PixelFlutInterface) {
        println("Draw Playbox at $origin")
        runBlocking {
            launch {
                drawHorizontalLine(pixelFlutInterfac, origin, size.first, DEFAULT_BORDER_COLOR)
            }
            launch {
                drawVerticalLine(pixelFlutInterfac, origin, size.second, DEFAULT_BORDER_COLOR)
            }

            launch {
                drawHorizontalLine(pixelFlutInterfac, origin.plus(Point(0, size.second - 1)), size.first, DEFAULT_BORDER_COLOR)
            }
            launch {
                drawVerticalLine(pixelFlutInterfac, origin.plus(Point(size.first - 1, 0)), size.second, DEFAULT_BORDER_COLOR)
            }
        }
    }
}
