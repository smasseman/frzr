package se.smasseman.frzr.plugins

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import org.slf4j.LoggerFactory
import java.time.Duration
import java.util.*

val LOG = LoggerFactory.getLogger("LOG")

fun interface Sender {
    fun send(message: Array<Pair<String, Any>>)
}

fun Application.configureSockets(onInit: Sender.() -> Unit, onConnect: Sender.() -> Unit) {
   install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    val ctx = newFixedThreadPoolContext(1, "socketContext")
    val connections = Collections.synchronizedSet<DefaultWebSocketSession?>(LinkedHashSet())
    val objectMapper = ObjectMapper()
    val sendEvent = fun(event: Array<Pair<String, Any>>) {
        val eventObject : Map<String, Any> = event.toMap()
        LOG.debug("Send {}", eventObject)
        val json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(eventObject)
        connections.forEach {
            async(context = ctx) {
                it.send(json)
            }
        }
    }

    onInit(sendEvent)

    routing {
        webSocket("/ws") { // websocketSession
            LOG.info("New connection.")
            connections.add(this)
            onConnect(sendEvent)
            this.closeReason.await().run {
                LOG.info("Closed connection")
                connections.remove(this@webSocket)
            }
        }
    }
}
