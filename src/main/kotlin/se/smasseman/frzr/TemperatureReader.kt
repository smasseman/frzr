package se.smasseman.frzr

fun interface TemperatureReader {
    fun read() : Temperature?
}
