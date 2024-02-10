package se.smasseman.frzr

import java.time.ZonedDateTime
import java.util.LinkedList
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Thermometer(private val reader: TemperatureReader) {
    private val listeners = LinkedList<TemperatureListener>()
    private var current: Temperature = reader.read() ?: Temperature(-100.0, ZonedDateTime.now())
    private val executor = Executors.newSingleThreadScheduledExecutor()

    fun addListener(listener: TemperatureListener) {
        listeners.add(listener)
    }

    init {
        val runnable = Runnable {
            val old = current
            val read = reader.read()
            if (read != null) {
                current = read
                listeners.forEach { listener -> listener.updated(current) }
            }
        }
        executor.scheduleWithFixedDelay(runnable, 1, 1, TimeUnit.SECONDS)
    }

}

fun interface TemperatureListener {
    fun updated(temperature: Temperature)
}

data class Temperature(val value: Double, val timestamp: ZonedDateTime)
