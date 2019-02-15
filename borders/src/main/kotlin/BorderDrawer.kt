package io.github.poeschl.pixelflutchallenge.borders

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.github.poeschl.pixelflutchallenge.shared.Pixel
import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).run {
        println("Start drawing on $host:$port")
        BorderDrawer(host, port, splitCount).drawBorders()
    }
}

class BorderDrawer(host: String, port: Int, private val splitCount: Int) {

    companion object {
        private val BORDER_COLOR = Color.WHITE
    }

    private val pixelFlutInterface = PixelFlutInterface(host, port)

    private var displaySize: Pair<Int, Int> = Pair(0, 0)

    fun drawBorders() {
        displaySize = pixelFlutInterface.getPlaygrounSize()
        println("Detected size $displaySize")

        val xSlice = displaySize.first / splitCount
        val ySlice = displaySize.second / splitCount

        pixelFlutInterface.blank()

        runBlocking {
            for (i: Int in 1 until splitCount) {
                println("Vert: ${xSlice * i}")
                launch {
                    drawVerticalLine(xSlice * i, displaySize.second, BORDER_COLOR)
                }
                println("Horz: ${ySlice * i}")
                launch {
                    drawHorizontalLine(ySlice * i, displaySize.first, BORDER_COLOR)
                }
            }
        }

        pixelFlutInterface.close()
    }

    private fun drawVerticalLine(lineX: Int, height: Int, color: Color) {
        val pixelSet = HashSet<Pixel>()
        for (x: Int in lineX - 1..lineX) {
            for (y: Int in 0..height) {
                pixelSet.add(Pixel(Point(x, y), color))
            }
        }
        pixelSet.forEach { pixelFlutInterface.paintPixel(it) }
    }

    private fun drawHorizontalLine(lineY: Int, width: Int, color: Color) {
        val pixelSet = HashSet<Pixel>()
        for (x: Int in 0..width) {
            for (y: Int in lineY - 1..lineY) {
                pixelSet.add(Pixel(Point(x, y), color))
            }
        }
        pixelSet.forEach { pixelFlutInterface.paintPixel(it) }
    }
}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
    val splitCount by parser.storing("-s", "--split", help = "Split times") { toInt() }
}


