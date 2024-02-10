package se.smasseman.frzr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import se.smasseman.frzr.plugins.*

object Configuration {
    val wanted = Wanted(WantedValue(10))
    val thermometer = Thermometer(SimulatedReader(wanted))

}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets(Configuration.thermometer, Configuration.wanted)
    configureRouting(Configuration.wanted)
}
