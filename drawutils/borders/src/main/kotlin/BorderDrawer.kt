package io.github.poeschl.pixelflutchallenge.borders

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import io.github.poeschl.pixelflutchallenge.shared.Painter
import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import io.github.poeschl.pixelflutchallenge.shared.drawRect
import java.awt.Color

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).run {
        println("Start drawing on $host:$port")
        BorderDrawer(host, port).start()
    }
}

class BorderDrawer(host: String, port: Int) : Painter() {
    companion object {

        private const val SPLIT_COUNT = 3
        private val FINAL_CHALLENGE_POSITION = Math.floor(SPLIT_COUNT / 2.0).toInt()
    }
    private val pixelFlutInterface = PixelFlutInterface(host, port)

    private val displaySize = pixelFlutInterface.getPlaygrounSize()

    private val playboxes = mutableListOf<Playbox>()

    override fun init() {
        println("Detected size $displaySize")

        val xSlice = displaySize.first / SPLIT_COUNT
        val ySlice = displaySize.second / SPLIT_COUNT

        for (x: Int in 0 until SPLIT_COUNT) {
            for (y: Int in 0 until SPLIT_COUNT) {
                if (x == FINAL_CHALLENGE_POSITION && y == FINAL_CHALLENGE_POSITION) {
                    drawRect(pixelFlutInterface, Point(xSlice * x, ySlice * y), Pair(xSlice, ySlice), Color.RED)
                } else {
                    playboxes.add(Playbox(Point(xSlice * x, ySlice * y), Pair(xSlice, ySlice)))
                }
            }
        }
        println("Setup ${playboxes.size} playboxes")
    }

    override fun render() {
        playboxes.parallelStream().forEach { it.draw(pixelFlutInterface) }
    }

    override fun handleInput(input: String) {
        when (input) {
            "blank" -> pixelFlutInterface.blank()
        }
    }

    override fun afterStop() {
        pixelFlutInterface.close()
    }
}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
}


