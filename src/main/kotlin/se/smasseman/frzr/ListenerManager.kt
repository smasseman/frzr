package se.smasseman.frzr

import java.util.*


open class ListenerManager<E> {

    private val listeners = LinkedList<Listener<E>>()

    fun addListener(listener: Listener<E>) = listeners.add(listener)

    protected fun notifyListeners(e: E) {
        listeners.forEach { listener -> listener.event(e) }
    }
}

fun interface Listener<E> {
    fun event(e: E)
}