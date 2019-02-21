package io.github.poeschl.pixelflutchallenge.labyrinth

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.amr.graph.grid.impl.OrthogonalGrid
import de.amr.maze.alg.traversal.RandomBFS
import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import io.github.poeschl.pixelflutchallenge.shared.drawRect
import java.awt.Color

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).run {
        println("Start drawing on $host:$port")
        LabyrinthDrawer(host, port).start()
    }
}

class LabyrinthDrawer(private val host: String, private val port: Int) {

    companion object {
        private const val SPLIT_COUNT = 3
        private val MAZE_CELL = Point(1, 1)
        private val MAZE_SIZE = Pair(10, 10)
    }

    private val drawInterface = PixelFlutInterface(host, port)

    private val displaySize = drawInterface.getPlaygroundSize()

    fun start() {
        println("Detected size $displaySize")

        val mazeOrigin = Point(
            (displaySize.first / SPLIT_COUNT) * MAZE_CELL.x,
            (displaySize.second / SPLIT_COUNT) * MAZE_CELL.y
        )
        println("Maze Origin: $mazeOrigin")

        drawRect(drawInterface, mazeOrigin, Pair(displaySize.first / SPLIT_COUNT, displaySize.second / SPLIT_COUNT), Color.BLACK)

        val mazeGrid = createNewMazeGrid(Point(0, 0), MAZE_SIZE)
        val mazeDrawer = Maze(mazeOrigin, MAZE_SIZE)

        mazeGrid.edges().forEach { println("${it.either()} -> ${it.other()}") }
        mazeDrawer.updateMaze(mazeGrid.edges())

        println("Draw maze")
        mazeDrawer.draw(drawInterface)

        drawInterface.close()
    }

    private fun createNewMazeGrid(start: Point, size: Pair<Int, Int>): OrthogonalGrid {
        val mazeGen = RandomBFS(size.first, size.second)
        mazeGen.createMaze(start.x, start.y)
        return mazeGen.grid
    }

}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
}
