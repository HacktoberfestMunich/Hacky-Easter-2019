package io.github.poeschl.pixelflutchallenge.labyrinth

import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.default
import de.amr.graph.grid.impl.OrthogonalGrid
import de.amr.maze.alg.traversal.GrowingTreeAlwaysRandom
import io.github.poeschl.pixelflutchallenge.shared.Painter
import io.github.poeschl.pixelflutchallenge.shared.PixelFlutInterface
import io.github.poeschl.pixelflutchallenge.shared.Point
import io.github.poeschl.pixelflutchallenge.shared.drawRect
import java.awt.Color
import java.util.*
import kotlin.concurrent.schedule
import kotlin.system.measureTimeMillis

fun main(args: Array<String>) {
    ArgParser(args).parseInto(::Args).run {
        println("Start drawing on $host:$port")
        LabyrinthDrawer(host, port).start()
    }
}

class LabyrinthDrawer(host: String, port: Int) : Painter() {
    companion object {

        private const val SPLIT_COUNT = 3
        private val MAZE_CELL = Point(1, 1)
        private val MAZE_START = Point(0, 0)
    }
    private val drawInterface = PixelFlutInterface(host, port)
    private val displaySize = drawInterface.getPlaygroundSize()
    private val daemonTimer = Timer(true)

    private lateinit var areaSize: Pair<Int, Int>
    private lateinit var areaOrigin: Point
    private lateinit var maze: Maze
    private var genTimer: TimerTask? = null

    override fun init() {
        initDimenstions()
        generateMazePixels()
    }

    override fun displayHelp() {
        super.displayHelp()
        println("generate -> Generate a new maze")
        println("timer <delay> -> Set a delay for auto-generation in seconds. Use 0 or any negative value to disable.")
    }

    override fun render() {
        maze.draw(drawInterface)
    }

    override fun afterStop() {
        drawInterface.close()
        daemonTimer.cancel()
    }

    override fun handleInput(input: String) {
        when {
            input == "generate" -> generateMazePixels()
            input.startsWith("timer") && input.split(' ').size == 2 -> {
                val delay = input.split(' ')[1].toLong()
                setTimerTime(delay)
                println("Set timer to $delay seconds")
            }
            else -> println("Not recognized command!")
        }
    }

    private fun initDimenstions() {
        println("Detected size $displaySize")

        val sizeX = (displaySize.first / SPLIT_COUNT)
        val sizeY = (displaySize.second / SPLIT_COUNT)

        val maxCellSize = Pair((sizeX / Maze.CELL_SIZE) - 1, (sizeY / Maze.CELL_SIZE) - 1)
        val centerOffset = Point((sizeX - (maxCellSize.first * Maze.CELL_SIZE)) / 2, (sizeY - (maxCellSize.second * Maze.CELL_SIZE)) / 2)

        areaOrigin = Point(sizeX * MAZE_CELL.x, sizeY * MAZE_CELL.y)
        areaSize = Pair(sizeX, sizeY)

        val mazeOrigin = areaOrigin.plus(centerOffset)

        println("Maze Origin: $mazeOrigin")
        println("Maze Size (cells): $maxCellSize")
        maze = Maze(mazeOrigin, maxCellSize)
    }

    private fun generateMazePixels() {
        print("Update Maze...")
        val genMilli = measureTimeMillis {
            val mazeGrid = createNewMazeGrid(MAZE_START, maze.mazeCellSize)
            print("Redraw Maze...")
            maze.clear()
            drawRect(drawInterface, areaOrigin, areaSize, Color.BLACK)
            maze.updateMaze(mazeGrid.edges())
        }
        println("Maze updated in $genMilli ms")
    }

    private fun createNewMazeGrid(start: Point, size: Pair<Int, Int>): OrthogonalGrid {
        val mazeGen = GrowingTreeAlwaysRandom(size.first, size.second)
        mazeGen.createMaze(start.x, start.y)
        return mazeGen.grid
    }

    private fun setTimerTime(delayInSeconds: Long) {

        if (delayInSeconds < 1) {
            genTimer?.cancel()
        } else {
            genTimer?.cancel()
            genTimer = daemonTimer.schedule(delayInSeconds * 1000) { generateMazePixels() }
        }
    }
}

class Args(parser: ArgParser) {
    val host by parser.storing("--host", help = "The host of the pixelflut server").default("localhost")
    val port by parser.storing("-p", "--port", help = "The port of the server") { toInt() }.default(1234)
}
