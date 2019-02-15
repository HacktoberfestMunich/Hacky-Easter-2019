package io.github.poeschl.pixelflutchallenge.shared

import kotlin.concurrent.thread

abstract class Painter {

    private var runningRender = true
    private var runningInput = true

    fun start() {
        init()

        thread {
            while (runningRender) {
                render()
            }
        }

        println("'quit' to quit \\_o_/")
        var input: String
        do {
            print("> ")
            input = readLine() ?: ""

            if (input == "quit") {
                runningRender = false
                runningInput = false
            } else {
                handleInput(input)
            }

        } while (runningInput)

        afterStop()
    }

    abstract fun init()

    abstract fun render()

    abstract fun handleInput(input: String)

    abstract fun afterStop()
}
