package io.github.poeschl.pixelflutchallenge.shared

import java.awt.Color
import java.util.stream.Collectors
import java.util.stream.IntStream

fun drawHorizontalLine(drawInterface: PixelFlutInterface, start: Point, width: Int, color: Color) {

    val pixelSet = IntStream.range(start.x, start.x + width)
        .parallel()
        .mapToObj { Pixel(Point(it, start.y), color) }
        .collect(Collectors.toSet())

    drawInterface.paintPixelSet(pixelSet)
}

fun drawVerticalLine(drawInterface: PixelFlutInterface, start: Point, height: Int, color: Color) {

    val pixelSet = IntStream.range(start.y, start.y + height)
        .parallel()
        .mapToObj { Pixel(Point(start.x, it), color) }
        .collect(Collectors.toSet())

    drawInterface.paintPixelSet(pixelSet)
}

fun drawRect(drawInterface: PixelFlutInterface, origin: Point, size: Pair<Int, Int>, color: Color) {
    val pixels = mutableSetOf<Pixel>()
    for (x: Int in origin.x until (origin.x + size.first)) {
        for (y: Int in origin.y until (origin.y + size.second)) {
            pixels.add(Pixel(Point(x, y), color))
        }
    }

    drawInterface.paintPixelSet(pixels)
}
