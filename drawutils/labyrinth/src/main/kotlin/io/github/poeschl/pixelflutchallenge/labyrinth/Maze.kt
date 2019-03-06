package io.github.poeschl.pixelflutchallenge.labyrinth

import de.amr.graph.core.api.Edge
import io.github.poeschl.pixelflutchallenge.shared.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.awt.Color
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.IntStream
import java.util.stream.Stream
import kotlin.concurrent.withLock

class Maze(private val origin: Point, val mazeCellSize: Pair<Int, Int>) {

    companion object {
        private const val DEBUG = false
        private const val PATH_SIZE = 8
        private const val BORDER_WIDTH = 1
        private val WALL_COLOR = Color.WHITE
        private val START_COLOR = Color.CYAN
        private val END_COLOR = Color.MAGENTA
        private val DRAW_LOCK = ReentrantLock()

        const val CELL_SIZE = PATH_SIZE + BORDER_WIDTH * 2
    }

    private val fullWidth = CELL_SIZE * mazeCellSize.first
    private val fullHeight = CELL_SIZE * mazeCellSize.second

    private var shadowMatrix = PixelMatrix(fullWidth, fullHeight)
    private var mazeSet = setOf<Pixel>()

    fun updateMaze(edges: Stream<Edge>) {
        createGrid()
        edges.parallel().forEach { createEdges(it) }
        mazeSet = shadowMatrix.getPixelSet().map { Pixel(it.point.plus(origin), it.color) }.toSet()
        shadowMatrix = PixelMatrix(fullWidth, fullHeight)
    }

    fun draw(drawInterface: PixelFlutInterface) {
        DRAW_LOCK.withLock {
            drawInterface.paintPixelSet(mazeSet)
        }
    }

    fun clear() {
        DRAW_LOCK.withLock {
            mazeSet = setOf()
        }
    }

    private fun createGrid() {
        runBlocking {
            launch {
                IntStream.rangeClosed(0, mazeCellSize.first)
                    .parallel()
                    .mapToObj { x -> createVerticalPixels(Point(x * CELL_SIZE, 0), fullHeight, WALL_COLOR) }
                    .flatMap { it.parallelStream() }
                    .forEach { shadowMatrix.insert(it) }
            }
            launch {
                IntStream.rangeClosed(0, mazeCellSize.second)
                    .parallel()
                    .mapToObj { y -> createHorizontalPixels(Point(0, y * CELL_SIZE), fullWidth, WALL_COLOR) }
                    .flatMap { it.parallelStream() }
                    .forEach { shadowMatrix.insert(it) }
            }
            launch {
                createRectPixels(Point(BORDER_WIDTH, BORDER_WIDTH), Pair(PATH_SIZE + 1, PATH_SIZE + 1), START_COLOR)
                    .parallelStream()
                    .forEach { shadowMatrix.insert(it) }
            }
            launch {
                createRectPixels(
                    Point(fullWidth - CELL_SIZE, fullHeight - CELL_SIZE)
                        .plus(Point(BORDER_WIDTH, BORDER_WIDTH)),
                    Pair(PATH_SIZE + 1, PATH_SIZE + 1),
                    END_COLOR
                )
                    .parallelStream()
                    .forEach { shadowMatrix.insert(it) }
            }
        }
    }

    private fun createEdges(edge: Edge) {
        val from = getOriginPointOfCell(Math.min(edge.either(), edge.other()))
        val to = getOriginPointOfCell(Math.max(edge.either(), edge.other()))

        when {
            from.x == to.x && from.y != to.y -> drawVerticalPathToBottom(from)
            from.y == to.y && from.x != to.x -> removeVerticalBorderToRightOf(from)
        }
    }

    private fun removeVerticalBorderToRightOf(from: Point) {
        for (yOffset in 0..PATH_SIZE) {
            val wallPoint = from.plus(Point(CELL_SIZE, BORDER_WIDTH + yOffset))
            shadowMatrix.remove(wallPoint)
            if (DEBUG) {
                shadowMatrix.insert(Pixel(wallPoint, Color.RED))
            }
        }
    }

    private fun drawVerticalPathToBottom(from: Point) {
        for (xOffset in 0..PATH_SIZE) {
            val wallPoint = from.plus(Point(BORDER_WIDTH + xOffset, CELL_SIZE))
            shadowMatrix.remove(wallPoint)
            if (DEBUG) {
                shadowMatrix.insert(Pixel(wallPoint, Color.RED))
            }
        }
    }

    private fun getOriginPointOfCell(index: Int): Point {
        val y = (index / mazeCellSize.first) * CELL_SIZE
        val x = (index % mazeCellSize.first) * CELL_SIZE
        return Point(x, y)
    }
}
