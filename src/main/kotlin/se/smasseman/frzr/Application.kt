package se.smasseman.frzr

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import se.smasseman.frzr.plugins.*

object Configuration {
    val errors = Errors()
    val wanted = Wanted(WantedValue(10))
    val thermometer = Thermometer(
        if (isOsX()) {
            SimulatedReader(wanted)
        } else {
            DS1820Reader.create(errors)
        }
    )
}

fun isOsX() = System.getProperties()["os.name"] == "Mac OS X"

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets(Configuration.thermometer, Configuration.wanted, Configuration.errors)
    configureRouting(Configuration.wanted)
}
