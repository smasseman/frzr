package se.smasseman.frzr

import org.slf4j.LoggerFactory
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class Thermometer(private val reader: TemperatureReader, private val errors: Errors) : ListenerManager<TemperatureReading>() {

    private var current: TemperatureReading = reader.read() ?: TemperatureReading(Temperature(50.0), ZonedDateTime.now())
    private val executor = Executors.newSingleThreadScheduledExecutor()

    init {
        val runnable = Runnable {
            try {
                reader.read()?.let {
                    current = it
                    notifyListeners(it)
                }
            } catch (e: Exception) {
                LoggerFactory.getLogger(this@Thermometer::class.java).error("Failed to read", e)
                errors.error(e)
            }
        }
        executor.scheduleWithFixedDelay(runnable, 1, 1, TimeUnit.SECONDS)
    }

    fun terminate() {
        executor.shutdown()
    }

}

