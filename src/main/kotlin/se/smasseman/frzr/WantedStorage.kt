package se.smasseman.frzr

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.file.Files

class WantedStorage {
    companion object {

        private val logger: Logger = LoggerFactory.getLogger(WantedStorage::class.java)
        private val file = File("wanted.txt").absoluteFile
        private val defaultValue = WantedValue(10)

        init {
            logger.info("Wanted temperature will be stored in $file")
        }

        fun getInitialWantedValue(): WantedValue {
            return try {
                WantedValue(Files.readString(file.toPath()).trim().toInt())
            } catch (e: Exception) {
                logger.error("Failed to read wanted value", e)
                defaultValue
            }
        }

        fun setInitialWantedValue(value: WantedValue) {
            try {
                Files.writeString(file.toPath(), value.value.toString())
            } catch (e: Exception) {
                logger.error("Failed to write wanted value", e)
            }
        }

    }

}
