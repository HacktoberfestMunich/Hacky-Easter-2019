package io.github.poeschl.pixelflutchallenge.labyrinth

import de.amr.graph.core.api.Edge
import io.github.poeschl.pixelflutchallenge.shared.*
import java.awt.Color
import java.util.stream.Stream

class Maze(private val origin: Point, private val mazeCellSize: Pair<Int, Int>) {

    companion object {
        private const val DEBUG = false
        private const val PATH_SIZE = 8
        private const val BORDER_WIDTH = 1
        private val WALL_COLOR = Color.WHITE

        const val CELL_SIZE = PATH_SIZE + BORDER_WIDTH * 2
    }

    private var shadowMaze = mutableSetOf<Pixel>()
    private var mazeSet = mutableSetOf<Pixel>()

    fun updateMaze(edges: Stream<Edge>) {
        for (x: Int in 0 until mazeCellSize.first) {
            for (y: Int in 0 until mazeCellSize.second) {
                createVertexes(Point(x, y))
            }
        }
        edges.forEach { drawEdge(it) }
        mazeSet = shadowMaze.filter { it.color != Color.BLACK }.toHashSet()
    }

    fun draw(drawInterface: PixelFlutInterface) {
        drawInterface.paintPixelSet(mazeSet)
    }

    private fun createVertexes(position: Point) {

        val vertexOrigin = origin.plus(
            Point(position.x * CELL_SIZE, position.y * CELL_SIZE)
        )
        shadowMaze.addAll(createHorizontalPixels(vertexOrigin, CELL_SIZE, WALL_COLOR))
        shadowMaze.addAll(createHorizontalPixels(vertexOrigin.plus(Point(0, CELL_SIZE)), CELL_SIZE, WALL_COLOR))
        shadowMaze.addAll(createVerticalPixels(vertexOrigin, CELL_SIZE, WALL_COLOR))
        shadowMaze.addAll(createVerticalPixels(vertexOrigin.plus(Point(CELL_SIZE, 0)), CELL_SIZE, WALL_COLOR))
        shadowMaze.add(Pixel(vertexOrigin.plus(Point(CELL_SIZE, CELL_SIZE)), WALL_COLOR))
    }

    private fun drawEdge(edge: Edge) {
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
