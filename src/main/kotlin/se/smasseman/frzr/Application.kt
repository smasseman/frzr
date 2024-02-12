package se.smasseman.frzr

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.plugin.mock.platform.MockPlatform
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProvider
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProvider
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import org.slf4j.LoggerFactory
import se.smasseman.frzr.Errors.Failure
import se.smasseman.frzr.plugins.configureRouting
import se.smasseman.frzr.plugins.configureSockets
import java.text.DecimalFormat
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.stream.IntStream

object Configuration {
    val wanted = Wanted(WantedStorage.getInitialWantedValue())
    val thermometer = Thermometer(
        if (isOsX()) {
            SimulatedReader(wanted)
        } else {
            DS1820Reader.create()
        }
    )
    val output = createOutput()

    init {
        Brain(thermometer, wanted, output)
        wanted.addListener { WantedStorage.setInitialWantedValue(it) }
        simulateErrors()
    }

    private fun simulateErrors() {
        if (false) {
            Thread {
                IntStream.range(0, 100).forEach {
                    Thread.sleep(1000)
                    Errors.error(NullPointerException("Fel nr $it"))
                }
            }.start()
        }
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

        val pinNumber = (System.getProperty("pin", "20")).toString().toInt()
        LoggerFactory.getLogger(Configuration.javaClass).info("Pin number is $pinNumber")

        val config = DigitalOutput.newConfigBuilder(pi4j)
            .id(pinNumber.toString())
            .name(pinNumber.toString())
            .address(pinNumber)
            .shutdown(DigitalState.HIGH)
            .initial(DigitalState.HIGH)
            .provider(outputProviderName)

        return pi4j.create(config)
    }
}

fun isOsX() = System.getProperties()["os.name"] == "Mac OS X"

fun main() {
    try {
        embeddedServer(
            Netty,
            port = System.getProperty("port", "8080").toString().toInt(),
            host = "0.0.0.0",
            module = Application::module,
        )
            .start(wait = true)
            .addShutdownHook { Configuration.thermometer.terminate() }
    } catch (e: Exception) {
        LoggerFactory.getLogger("MAIN").error("Faield to start", e)
        Configuration.thermometer.terminate();
    }
}

fun Application.module() {
    configureSockets({
        Configuration.thermometer.addListener { send(temperatureEvent(it)) }
        Configuration.wanted.addListener { send(wantedEvent(it)) }
        Errors.addListener { send(errorEvent(it)) }
        Configuration.output.addListener(DigitalStateChangeListener {
            send(onOffEvent(it.state()))
        })
    }) {
        send(wantedEvent(Configuration.wanted.get()))
        send(onOffEvent(Configuration.output.state()))
    }
    configureRouting(Configuration.wanted)
}

private fun temperatureEvent(value: TemperatureReading): Array<Pair<String, Any>> = arrayOf(
    "type" to "TEMPERATURE",
    "value" to DecimalFormat("#.#").format(value.value.value),
    "timestamp" to timeToString(value.timestamp)
)

private fun timeToString(value: ZonedDateTime) =
    value.withZoneSameInstant(ZoneId.of("Europe/Stockholm")).toLocalTime().withNano(0).toString()

private fun timeAndDateToString(value: ZonedDateTime) =
    value.withZoneSameInstant(ZoneId.of("Europe/Stockholm")).run {
        toLocalDate().toString() + " " + toLocalTime().withNano(0).toString()
    }

private fun errorEvent(list: List<Failure>): Array<Pair<String, Any>> = arrayOf(
    "type" to "ERROR",
    "errors" to list.associate { timeAndDateToString(it.timestamp) to it.exception.toString() }
)

fun wantedEvent(value: Temperature): Array<Pair<String, Any>> = arrayOf(
    "type" to "WANTED",
    "value" to value.value
)

fun onOffEvent(state: DigitalState): Array<Pair<String, Any>> = arrayOf(
    "type" to "ON_OFF",
    "value" to if (state.isHigh) "OFF" else "ON"
)
