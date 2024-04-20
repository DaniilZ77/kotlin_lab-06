package ru.spbsu.kotlin

import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.io.*
import java.util.zip.GZIPInputStream


class EncodeStreamTest {
    private val rootOutputFolder = File("./output-folder/")
    private val rootInputFolder = File("./input-folder/")

    @BeforeEach
    fun cleanFolder() {
        recreateFolder(rootOutputFolder)
        recreateFolder(rootInputFolder)
    }

    private fun recreateFolder(folder: File) {
        if (folder.exists()) {
            folder.deleteRecursively()
        }
        folder.mkdirs()
    }

    @Test
    fun goodPasswordChars () {
        assertDoesNotThrow { EncodeStream(rootOutputFolder, "12345") }
        assertDoesNotThrow {  EncodeStream(rootOutputFolder, "9876543") }
    }

    @Test
    fun wrongPasswordChars () {
        assertThrows<IllegalArgumentException> { EncodeStream(rootOutputFolder, "") }
        assertThrows<IllegalArgumentException> { EncodeStream(rootOutputFolder, "aaa") }
        assertThrows<IllegalArgumentException> { EncodeStream(rootOutputFolder, "012345") }
        assertThrows<IllegalArgumentException> { EncodeStream(rootOutputFolder, "98760543") }
    }

    @Test
    fun testEncodeDecode () {
        val writer = OutputStreamWriter(EncodeStream(rootOutputFolder, "123456"), "UTF-8")
        val testText = "Some text to test our strange cipher"
        writer.use{
            it.write(testText)
        }
        val reader = InputStreamReader(DecodeStream(rootOutputFolder), "UTF-8")
        reader.use{
            assertEquals(testText, it.readText())
        }
    }

    private fun decompress(tgzFile: String, output: File) {
        TarArchiveInputStream(GZIPInputStream(FileInputStream(tgzFile))).use { tarInputStream ->
            do {
                val entry: TarArchiveEntry = tarInputStream.nextTarEntry ?: break
                if (entry.isDirectory) {
                    continue
                }
                val currentFile = File(output, entry.name)
                val parent = currentFile.parentFile
                if (!parent.exists()) {
                    parent.mkdirs()
                }
                FileOutputStream(currentFile).use {
                    tarInputStream.copyTo(it)
                }
                currentFile.setLastModified(entry.lastModifiedDate.time)
            }
            while(true)
        }
    }

    @Test
    fun decodeSample1_1() {
        decompress("./sample1.1.tgz", rootInputFolder)
        val reader = InputStreamReader(DecodeStream(rootInputFolder), "UTF-8")
        reader.use{
            assertEquals("Кто тесты не писал, тот в цирке не смеётся!", it.readText())
        }
    }

    @Test
    fun decodeSample1_2() {
        decompress("./sample1.2.tgz", rootInputFolder)
        val reader = InputStreamReader(DecodeStream(rootInputFolder), "UTF-8")
        reader.use{
            assertEquals("Кто тесты не писал, тот в цирке не смеётся!", it.readText())
        }
    }

    @Test
    fun decodeSample2_1() {
        decompress("./sample2.1.tgz", rootInputFolder)
        val reader = InputStreamReader(DecodeStream(rootInputFolder), "UTF-8")
        reader.use{
            assertEquals("Скажете тоже, олимпиадное программирование", it.readText())
        }
    }

    @Test
    fun decodeSample2_2() {
        decompress("./sample2.2.tgz", rootInputFolder)
        val reader = InputStreamReader(DecodeStream(rootInputFolder), "UTF-8")
        reader.use{
            assertEquals("Скажете тоже, олимпиадное программирование", it.readText())
        }
    }

}