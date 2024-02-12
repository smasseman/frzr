package se.smasseman.frzr

class Wanted(private var wantedValue: Temperature) : ListenerManager<Temperature>() {

    fun get() = wantedValue

    fun inc() = change(1)

    fun dec() = change(-1)

    private fun change(i: Int): Temperature {
        val newValue = Temperature(wantedValue.value + i)
        wantedValue = newValue
        notifyListeners(newValue)
        return newValue
    }
}

