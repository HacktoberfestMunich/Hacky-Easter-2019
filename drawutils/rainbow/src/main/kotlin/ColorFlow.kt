package io.github.poeschl.pixelflutchallenge.rainbow

import java.awt.Color

class ColorFlow {

    private var lastColor = Color.RED
    private var stage = 0


    fun nextColor(): Color {

        lastColor = when (stage % 6) {
            0 -> increment(Color.YELLOW) { Color(255, lastColor.green + 1, 0) }
            1 -> increment(Color.GREEN) { Color(lastColor.red - 1, 255, 0) }
            2 -> increment(Color.CYAN) { Color(0, 255, lastColor.blue + 1) }
            3 -> increment(Color.BLUE) { Color(0, lastColor.green - 1, 255) }
            4 -> increment(Color.MAGENTA) { Color(lastColor.red + 1, 0, 255) }
            5 -> increment(Color.RED) { Color(255, 0, lastColor.blue - 1) }
            else -> Color.BLACK
        }

        return lastColor;
    }

    private fun increment(colorLimit: Color, colorGen: () -> Color): Color {
        val newColor = colorGen.invoke()
        if (newColor == colorLimit) {
            stage++
        }
        return newColor
    }
}

