package se.smasseman.frzr

import java.util.*

class Wanted(private var wantedValue: WantedValue) {

    private val listeners = LinkedList<WantedListener>()

    fun addListener(listener: WantedListener) = listeners.add(listener)

    fun get() = wantedValue

    fun inc() = change(1)

    fun dec() = change(-1)

    private fun change(i: Int): WantedValue {
        val newValue = WantedValue(wantedValue.value + i)
        wantedValue = newValue
        listeners.forEach { listener -> listener.updated(newValue) }
        return newValue
    }
}

fun interface WantedListener {
    fun updated(temperature: WantedValue)
}

data class WantedValue(val value: Int)

