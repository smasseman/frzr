package se.smasseman.frzr

import java.time.ZonedDateTime

class SimulatedReader(private val wanted: Wanted) : TemperatureReader {
    private var current = 15.0
    override fun read(): Temperature? {
        val diff: Double = current - wanted.get().value
        if (Math.abs(diff) > 1) {
            current += (if (diff < 0) 1 else -1)
        } else {
            current += (if (diff < 0) 0.1 else -0.1)
        }
        return Temperature(current, ZonedDateTime.now())
    }

}
