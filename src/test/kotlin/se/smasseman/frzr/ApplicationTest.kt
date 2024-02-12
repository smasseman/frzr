package se.smasseman.frzr

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import se.smasseman.frzr.plugins.configureRouting
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting(Wanted(Temperature(0.0)), Errors.systemOut())
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }
}
