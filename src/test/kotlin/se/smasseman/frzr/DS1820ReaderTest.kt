package se.smasseman.frzr

import org.assertj.core.api.Assertions.assertThat
import java.io.File
import java.nio.file.Files
import kotlin.test.Test

class DS1820ReaderTest {

    @Test
    fun read() {
        val rootName = "ds1820"
        val rootDir: File = createTempDir(rootName)
        val subDir = createDir(rootDir, "28-000007602ffa")
        val file = File(subDir, "w1_slave")
        Files.write(
            file.toPath(), """
            a1 01 4b 01 7f ff 0c 10 34 : crc=34 YES
            a1 01 4b 01 7f ff 0c 10 34 t=26062
        """.trimIndent().toByteArray()
        )
        val reader = DS1820Reader.create(Errors.systemOut(), rootDir)
        val t = reader.read()
        assertThat(t).isNotNull
        assertThat(t!!.value).isEqualTo(26.1)
    }

    private fun createDir(parentDirectory: File, name: String): File {
        return File(parentDirectory, name).apply {  mkdir() }

    }

    private fun createTempDir(rootName: String) =
        File.createTempFile(rootName, "").apply {
            delete()
            mkdir()
        }

}