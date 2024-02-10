package se.smasseman.frzr.plugins

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.slf4j.LoggerFactory
import se.smasseman.frzr.Wanted
import java.io.File

fun Application.configureRouting(wanted: Wanted) {
    routing {
        get("/") {
            call.respondRedirect("static/index.html")
        }

        get("/static/{file}") {
            call.respondFile(
                File(
                    System.getProperty(
                        "staticfiles",
                        "/Users/jorgensmas/git/smasseman/frzr/src/main/resources/static"
                    )
                ),
                call.parameters["file"].toString()
            )
        }

        post("/wanted/up") {
            call.respond("Incremented to " + wanted.inc())
        }

        post("/wanted/down") {
            call.respond("Decremented to " + wanted.dec())
        }
    }
}
