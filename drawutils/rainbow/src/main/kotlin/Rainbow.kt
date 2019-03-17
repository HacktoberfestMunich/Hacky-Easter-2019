package io.github.poeschl.pixelflutchallenge.rainbow

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.github.poeschl.pixelflutchallenge.shared.Painter
import io.github.poeschl.pixelflutchallenge.shared.Pixel
import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import java.awt.Color
import kotlin.random.Random

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).run {
        println("Start drawing on $host:$port")
        BorderDrawer(host, port).start()
    }
}

class BorderDrawer(host: String, port: Int) : Painter() {

    companion object {
        private const val SPLIT_COUNT = 3
    }

    private val pixelFlutInterface = PixelFlutInterface(host, port)
    private val colorFlow = ColorFlow()

    private val displaySize = pixelFlutInterface.getPlaygroundSize()
    private val origin = getRandomCellOrigin()
    private val cellSize = getCellSize()

    private var lastPoint = origin
    private var color = Color.RED

    override fun init() {
        println("Detected size $displaySize")


        println("Rainbows")
    }

    override fun render() {

        var x = lastPoint.x
        var y = lastPoint.y + 1

        if (y > cellSize.second) {
            y %= cellSize.second
            x = (x + 1) % cellSize.first
            color = colorFlow.nextColor()
        }

        lastPoint = Point(x, y)
        pixelFlutInterface.paintPixelSet(setOf(Pixel(lastPoint.plus(origin), color)))
    }

    override fun handleInput(input: String) {
    }

    override fun afterStop() {
        pixelFlutInterface.close()
    }

    private fun getRandomCellOrigin(): Point {
        val cellSize = getCellSize()

        val randomX = Random.nextInt(SPLIT_COUNT * 2)
        val randomY = Random.nextInt(SPLIT_COUNT * 2)

        return Point(cellSize.first * randomX + 1, cellSize.second * randomY + 1)
    }

    private fun getCellSize(): Pair<Int, Int> {
        val cellX = displaySize.first / (SPLIT_COUNT * 2)
        val cellY = displaySize.second / (SPLIT_COUNT * 2)
        return Pair(cellX, cellY)
    }
}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
}


