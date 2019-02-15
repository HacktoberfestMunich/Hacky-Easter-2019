package io.github.poeschl.pixelflutchallenge.shared

import java.awt.Color
import java.io.*
import java.net.Socket

class PixelFlutInterface(address: String, port: Int) {

    companion object {
        private const val SIZE_COMMAND = "SIZE"
        private val SIZE_ANSWER_PATTERN = "SIZE (\\d+) (\\d+)".toRegex()
        private const val GET_PX_COMMAND = "PX %d %d"
        private val GET_PX_ANSWER_PATTERN = "PX (\\d+) (\\d+) (\\w+)".toRegex()
        private const val PAINT_PX_COMMAND = "PX %d %d %s"
    }

    private val socket = Socket(address, port)
    private val writer = PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())))
    private val reader = BufferedReader(InputStreamReader(socket.getInputStream()))

    /**
     * Returns the size of the playground as a pair with width and height.
     *
     * @return Pair<width, height>
     */
    fun getPlaygrounSize(): Pair<Int, Int> {
        writer.println(SIZE_COMMAND)
        writer.flush()

        val sizeAnswer = reader.readLine() ?: ""
        val matchResult = SIZE_ANSWER_PATTERN.matchEntire(sizeAnswer)

        return if (matchResult != null) {
            val groups = matchResult.groupValues
            Pair(Integer.parseInt(groups[1]), Integer.parseInt(groups[2]))
        } else {
            Pair(0, 0)
        }
    }

    /**
     * Paints the pixel on the pixel wall.
     *
     * @param pixel The pixel to be painted
     */
    fun paintPixel(pixel: Pixel) {
        writer.printf(PAINT_PX_COMMAND + '\n', pixel.point.x, pixel.point.y, convertColorToHex(pixel.color))
        writer.flush()
    }

    /**
     * Draw a set of pixels.
     *
     * @param pixels The set of pixels.
     */
    fun paintPixelSet(pixels: Set<Pixel>) {
        pixels.parallelStream().forEach(this::paintPixel)
    }

    /**
     * Retuns the Pixel on the given position.
     *
     * @return The pixel at the point.
     */
    fun getPixel(point: Point): Pixel {
        writer.println(GET_PX_COMMAND)
        writer.flush()

        val pixelAnswer = reader.readLine() ?: ""
        val matcher = GET_PX_ANSWER_PATTERN.matchEntire(pixelAnswer)

        return if (matcher != null) {
            val groups = matcher.groupValues
            return Pixel(
                Point(
                    Integer.parseInt(groups[1]),
                    Integer.parseInt(groups[2])
                ),
                convertHexToColor(groups[3])
            )
        } else {
            Pixel(Point(0, 0), Color.BLACK)
        }
    }

    /**
     * Paints the whole wall black
     */
    fun blank() {
        println("Blanking Screen")
        val set = mutableSetOf<Pixel>()
        val wallSize = getPlaygrounSize()
        for (x: Int in 0..wallSize.first) {
            for (y: Int in 0..wallSize.second) {
                set.add(Pixel(Point(x, y), Color.BLACK))
            }
        }
        this.paintPixelSet(set)
    }

    fun close() {
        writer.close()
        reader.close()
        socket.close()
    }

    private fun convertColorToHex(color: Color): String {
        return String.format("%02X%02X%02X", color.red, color.green, color.blue)
    }

    private fun convertHexToColor(hex: String): Color {
        val red = hex.substring(0..1)
        val green = hex.substring(2..3)
        val blue = hex.substring(4..5)

        return Color(Integer.parseInt(red, 16), Integer.parseInt(green, 16), Integer.parseInt(blue, 16))
    }
}
