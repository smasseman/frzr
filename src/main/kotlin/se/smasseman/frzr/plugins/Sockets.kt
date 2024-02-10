package se.smasseman.frzr.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import org.slf4j.LoggerFactory
import se.smasseman.frzr.*
import java.text.DecimalFormat
import java.time.Duration
import java.time.ZoneId
import java.util.*

fun Application.configureSockets(
    thermometer: Thermometer,
    wanted: Wanted,
    errors: Errors
    ) {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val ctx = newFixedThreadPoolContext(1, "socketContext")
    val log = LoggerFactory.getLogger(this::class.java)
    val connections = Collections.synchronizedSet<DefaultWebSocketSession?>(LinkedHashSet())
    val sendEvent = fun(event: WebSocketEvent) {
        connections.forEach {
            LoggerFactory.getLogger(this::class.java).info("Send $event")
            async(context = ctx) { it.send(event.toJson()) }
        }
    }

    thermometer.addListener(fun(value: Temperature) {
        sendEvent(TemperatureEvent.from(value))
    })

    wanted.addListener(fun(value: WantedValue) {
        sendEvent(WantedEvent.from(value))
    })

    errors.addListener(fun(e: Exception) {
        sendEvent(ErrorEvent.from(e))
    })

    routing {
        webSocket("/ws") { // websocketSession
            log.info("New connection.")
            connections.add(this)
            sendEvent(WantedEvent.from(wanted.get()))
            this.closeReason.await().run {
                log.info("Closed connection")
                connections.remove(this@webSocket)
            }
        }
    }
}

enum class WebSocketEventType {
    TEMPERATURE, WANTED, ERROR
}

abstract class WebSocketEvent(val type: WebSocketEventType) {
    fun toJson(): String {
        return ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this)
    }
}

class TemperatureEvent(private val value: String, val timestamp: String) : WebSocketEvent(WebSocketEventType.TEMPERATURE) {
    companion object {
        private val df = DecimalFormat("#.#")
        fun from(value: Temperature): TemperatureEvent = TemperatureEvent(
            df.format(value.value),
            value.timestamp.withZoneSameInstant(ZoneId.of("UTC")).toLocalTime().withNano(0).toString()
        )
    }

    override fun toString() = this.javaClass.simpleName + "[" + value + "]"
}

class WantedEvent(private val value: Int) : WebSocketEvent(WebSocketEventType.WANTED) {
    companion object {
        fun from(value: WantedValue): WantedEvent = WantedEvent(value.value)
    }

    override fun toString() = this.javaClass.simpleName + "[" + value + "]"
}


class ErrorEvent(private val value: String) : WebSocketEvent(WebSocketEventType.ERROR) {
    companion object {
        fun from(value: Exception): ErrorEvent = ErrorEvent(value.toString())
    }

    override fun toString() = this.javaClass.simpleName + "[" + value + "]"
}
