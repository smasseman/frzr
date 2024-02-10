package se.smasseman.frzr

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.plugin.mock.platform.MockPlatform
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProvider
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import se.smasseman.frzr.plugins.*

object Configuration {
    val errors = Errors()
    val wanted = Wanted(WantedStorage.getInitialWantedValue())
    val thermometer = Thermometer(
        if (isOsX()) {
            SimulatedReader(wanted)
        } else {
            DS1820Reader.create(errors)
        }
    )
    val output = createOutput()

    init {
        Brain(thermometer, wanted, output)
        wanted.addListener { WantedStorage.setInitialWantedValue(it) }
    }

    private fun createOutput(): DigitalOutput {
        val outputProviderName = if (isOsX()) "mock-digital-output" else "pigpio-digital-output"
        val pi4j: Context = if (isOsX())
            Pi4J.newContextBuilder()
                .add(MockPlatform())
                .add(
                    MockDigitalInputProvider.newInstance(),
                    MockDigitalOutputProvider.newInstance()
                )
                .build()
        else
            Pi4J.newAutoContext()

        val pinNumber = (System.getProperty("pin", "20")).toString().toInt();
        LoggerFactory.getLogger(Configuration.javaClass).info("Pin number is $pinNumber")

        val config = DigitalOutput.newConfigBuilder(pi4j)
            .id(pinNumber.toString())
            .name(pinNumber.toString())
            .address(pinNumber)
            .shutdown(DigitalState.HIGH)
            .initial(DigitalState.HIGH)
            .provider(outputProviderName)

        return pi4j.create(config);
    }
}

fun isOsX() = System.getProperties()["os.name"] == "Mac OS X"

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureSockets(Configuration.thermometer, Configuration.wanted, Configuration.output, Configuration.errors)
    configureRouting(Configuration.wanted)
}
