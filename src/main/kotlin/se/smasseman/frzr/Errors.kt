package se.smasseman.frzr

import io.ktor.utils.io.*
import java.time.ZonedDateTime
import java.util.*

class Errors : ListenerManager<List<Errors.Failure>>() {

    data class Failure(val timestamp: ZonedDateTime, val exception: Exception)

    private val maxSize = 20
    private val errors = LinkedList<Failure>()

    fun error(e: Exception) {
        val list : List<Failure>
        synchronized(errors) {
            errors.add(Failure(ZonedDateTime.now(), e))
            if(errors.size>maxSize) {
                errors.removeFirst()
            }
            list = ArrayList(errors)
        }
        notifyListeners(list)
    }

    fun clear() {
        synchronized(errors) {
            errors.clear()
        }
        notifyListeners(listOf())
    }

    companion object {
        fun systemOut(): Errors {
            return Errors().apply {
                addListener { e -> e.last().exception.printStack() }
            }
        }
    }
}