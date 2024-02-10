package se.smasseman.frzr

import io.ktor.utils.io.*
import java.lang.Exception
import java.util.LinkedList

class Errors {
    private val listeners = LinkedList<ErrorListener>()
    fun addListener(listener: ErrorListener) = listeners.add(listener)
    fun error(e: Exception) {
        listeners.forEach { listener -> listener.error(e) }
    }

    companion object {
        fun systemOut(): Errors {
            return Errors().apply {
                addListener { e -> e.printStack() }
            }
        }
    }
}

fun interface ErrorListener {
    fun error(e: Exception)
}