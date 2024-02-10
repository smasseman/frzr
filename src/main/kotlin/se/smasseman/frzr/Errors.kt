package se.smasseman.frzr

import io.ktor.utils.io.*

class Errors : ListenerManager<Exception>() {

    fun error(e: Exception) {
        notifyListeners(e)
    }

    companion object {
        fun systemOut(): Errors {
            return Errors().apply {
                addListener { e -> e.printStack() }
            }
        }
    }
}