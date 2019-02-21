package io.github.poeschl.pixelflutchallenge.labyrinth

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.amr.graph.grid.impl.OrthogonalGrid
import de.amr.maze.alg.traversal.GrowingTreeAlwaysRandom
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
        private val MAZE_START = Point(0, 0)
    }

    private val drawInterface = PixelFlutInterface(host, port)

    private val displaySize = drawInterface.getPlaygroundSize()

    fun start() {
        println("Detected size $displaySize")

        val sizeX = (displaySize.first / SPLIT_COUNT)
        val sizeY = (displaySize.second / SPLIT_COUNT)

        val maxMazeSize = Pair((sizeX / Maze.CELL_SIZE) - 1, (sizeY / Maze.CELL_SIZE) - 1)
        val centerOffset = Point((sizeX - (maxMazeSize.first * Maze.CELL_SIZE)) / 2, (sizeY - (maxMazeSize.second * Maze.CELL_SIZE)) / 2)
        val cellOrigin = Point(sizeX * MAZE_CELL.x, sizeY * MAZE_CELL.y)
        val mazeOrigin = cellOrigin.plus(centerOffset)

        println("Maze Origin: $mazeOrigin")
        println("Maze Size (cells): $maxMazeSize")

        drawRect(drawInterface, cellOrigin, Pair(sizeX, sizeY), Color.BLACK)

        val mazeGrid = createNewMazeGrid(MAZE_START, maxMazeSize)
        val mazeDrawer = Maze(mazeOrigin, maxMazeSize)

        println("Update draw")
        mazeDrawer.updateMaze(mazeGrid.edges())

        println("Draw maze")
        mazeDrawer.draw(drawInterface)

        drawInterface.close()
    }

    private fun createNewMazeGrid(start: Point, size: Pair<Int, Int>): OrthogonalGrid {
        println("Generate Maze")
        val mazeGen = GrowingTreeAlwaysRandom(size.first, size.second)
        mazeGen.createMaze(start.x, start.y)
        return mazeGen.grid
    }

}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
}
