package io.github.poeschl.pixelflutchallenge.labyrinth

import de.amr.graph.core.api.Edge
import io.github.poeschl.pixelflutchallenge.shared.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.util.*
import java.util.stream.Stream

class Maze(private val origin: Point, private val mazeCellSize: Pair<Int, Int>) {

    companion object {
        private const val DEBUG = false
        private const val PATH_SIZE = 8
        private const val BORDER_WIDTH = 1
        private val WALL_COLOR = Color.WHITE

        const val CELL_SIZE = PATH_SIZE + BORDER_WIDTH * 2
    }

    private var shadowMaze = Collections.synchronizedSet(mutableSetOf<Pixel>())
    private var mazeSet = setOf<Pixel>()

    fun updateMaze(edges: Stream<Edge>) {
        createGrid()
        edges.parallel().forEach { createEdges(it) }
        mazeSet = shadowMaze.toSet()
        shadowMaze.clear()
    }

    fun draw(drawInterface: PixelFlutInterface) {
        drawInterface.paintPixelSet(mazeSet)
    }

    private fun createGrid() {
        val fullWidth = CELL_SIZE * mazeCellSize.first
        val fullHeight = CELL_SIZE * mazeCellSize.second

        runBlocking {
            launch {
                for (x: Int in 0..mazeCellSize.first) {
                    shadowMaze.addAll(createVerticalPixels(origin.plus(Point(x * CELL_SIZE, 0)), fullHeight, WALL_COLOR))
                }
            }
            launch {
                for (y: Int in 0..mazeCellSize.second) {
                    shadowMaze.addAll(createHorizontalPixels(origin.plus(Point(0, y * CELL_SIZE)), fullWidth, WALL_COLOR))
                }
            }
        }
    }

    private fun createEdges(edge: Edge) {
        val from = getPointOfMazeCell(Math.min(edge.either(), edge.other()))
        val to = getPointOfMazeCell(Math.max(edge.either(), edge.other()))

        when {
            from.x == to.x && from.y != to.y -> drawVerticalPathToBottom(from)
            from.y == to.y && from.x != to.x -> removeVerticalBorderToRightOf(from)
        }
    }

    private fun removeVerticalBorderToRightOf(from: Point) {
        for (yOffset in 0..PATH_SIZE) {
            val wallPoint = from.plus(Point(CELL_SIZE, BORDER_WIDTH + yOffset))
            shadowMaze.removeIf { it.point == origin.plus(wallPoint) }
            if (DEBUG) {
                shadowMaze.add(Pixel(origin.plus(wallPoint), Color.RED))
            }
        }
    }

    private fun drawVerticalPathToBottom(from: Point) {
        for (xOffset in 0..PATH_SIZE) {
            val wallPoint = from.plus(Point(BORDER_WIDTH + xOffset, CELL_SIZE))
            shadowMaze.removeIf { it.point == origin.plus(wallPoint) }
            if (DEBUG) {
                shadowMaze.add(Pixel(origin.plus(wallPoint), Color.RED))
            }
        }
    }

    private fun getPointOfMazeCell(index: Int): Point {
        val y = (index / mazeCellSize.first) * CELL_SIZE
        val x = (index % mazeCellSize.first) * CELL_SIZE
        return Point(x, y)
    }
}
