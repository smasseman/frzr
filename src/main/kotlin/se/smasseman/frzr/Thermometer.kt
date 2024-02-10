package se.smasseman.frzr

import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Thermometer(private val reader: TemperatureReader) : ListenerManager<Temperature>() {

    private var current: Temperature = reader.read() ?: Temperature(50.0, ZonedDateTime.now())
    private val executor = Executors.newSingleThreadScheduledExecutor()

    init {
        val runnable = Runnable {
            reader.read()?.let {
                current = it
                notifyListeners(it)
            }
        }
        executor.scheduleWithFixedDelay(runnable, 1, 1, TimeUnit.SECONDS)
    }

}

data class Temperature(val value: Double, val timestamp: ZonedDateTime)
