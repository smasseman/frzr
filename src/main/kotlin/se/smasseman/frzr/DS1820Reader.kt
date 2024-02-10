package se.smasseman.frzr

import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.io.FileFilter
import java.io.FileReader
import java.io.IOException
import java.lang.Exception
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.ZonedDateTime
import java.util.regex.Pattern

class DS1820Reader(private val file: File, private val errors: Errors) : TemperatureReader {

    companion object {
        fun create(errors: Errors, directory: File = File("/sys/bus/w1/devices")): DS1820Reader {
            val dirs: Array<out File> = directory
                .listFiles(FileFilter { it.isDirectory })
                ?: throw Exception("Could not list directories in $directory")

            if (dirs.isEmpty()) {
                throw Exception("No device directories found in $directory")
            }
            if (dirs.size > 1) {
                throw Exception("More then 1 directory found in $directory. Found ${dirs.size} directories. $dirs")
            }
            val file = File(dirs.first(), "w1_slave")
            return DS1820Reader(file, errors)
        }
    }

    override fun read(): Temperature? {
        return try {
            val value: BigDecimal = readFile();
            Temperature(value.toDouble(), ZonedDateTime.now())
        } catch (e: IOException) {
            errors.error(e)
            null
        }
    }

    private fun readFile(): BigDecimal {
        val reader = FileReader(file)
        BufferedReader(reader).use { br ->
            val firstLine = br.readLine() ?: throw IOException("No first line.")
            if (!firstLine.endsWith("YES")) throw IOException("First line ended with $firstLine")
            val secondLine = br.readLine() ?: throw IOException("No second line.")
            val p = Pattern.compile(".*t.(-?[0-9]*)")
            val m = p.matcher(secondLine)
            if (!m.matches()) throw IOException("Failed to parse $secondLine")
            val valueString = m.group(1)
            var value = BigDecimal(valueString)
            value = value.divide(BigDecimal(1000), 1, RoundingMode.HALF_UP)
            return value
        }

    }
}