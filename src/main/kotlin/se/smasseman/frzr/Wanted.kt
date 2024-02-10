package se.smasseman.frzr

class Wanted(private var wantedValue: WantedValue) : ListenerManager<WantedValue>() {

    fun get() = wantedValue

    fun inc() = change(1)

    fun dec() = change(-1)

    private fun change(i: Int): WantedValue {
        val newValue = WantedValue(wantedValue.value + i)
        wantedValue = newValue
        notifyListeners(newValue)
        return newValue
    }
}

data class WantedValue(val value: Int)

