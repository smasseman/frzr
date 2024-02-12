package se.smasseman.frzr

import java.time.ZonedDateTime

data class TemperatureReading(val value: Temperature, val timestamp: ZonedDateTime)