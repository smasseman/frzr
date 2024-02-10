package se.smasseman.frzr

import com.pi4j.io.gpio.digital.DigitalOutput
import java.time.ZonedDateTime

class Brain(thermometer: Thermometer, wanted: Wanted, private val output: DigitalOutput) {
    private var wanted: WantedValue = wanted.get()
    private var temperature: Temperature = Temperature(this.wanted.value.toDouble(), ZonedDateTime.now())

    init {
        thermometer.addListener {
            this.temperature = it
            doSmartStuff()
        }
        wanted.addListener {
            this.wanted = it
            doSmartStuff()
        }
    }

    private fun doSmartStuff() {
        if (temperature.value <= wanted.value.toDouble()) {
            output.low()
        } else {
            output.high()
        }
    }
}
