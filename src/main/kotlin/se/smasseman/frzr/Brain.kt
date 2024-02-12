package se.smasseman.frzr

import com.pi4j.io.gpio.digital.DigitalOutput


class Brain(thermometer: Thermometer, wanted: Wanted, private val output: DigitalOutput) {

    private data class State(val wanted: Temperature, val current: Temperature) {
        fun withWanted(w: Temperature) = State(w, current)
        fun withCurrent(c: Temperature) = State(wanted, c)
        fun currentHigherThenWanted() = current.value > wanted.value
    }

    private var state = State(wanted.get(), wanted.get())

    init {
        thermometer.addListener {
            synchronized(this@Brain) {
                state = state.withCurrent(it.value)
            }
            doSmartStuff()
        }
        wanted.addListener {
            synchronized(this@Brain) {
                state = state.withWanted(it)
            }
            doSmartStuff()
        }
    }

    private fun doSmartStuff() {
        if (state.currentHigherThenWanted()) {
            output.high()
        } else {
            output.low()
        }
    }
}
