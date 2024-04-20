package ru.spbsu.kotlin

import java.io.File
import java.io.InputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun lastModified(file: File): Int {
    val lastModifiedMilli = file.lastModified()
    val instant = Instant.ofEpochMilli(lastModifiedMilli)
    val localDateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())

    val seconds = localDateTime.second

    return seconds
}

fun File.scanFiles(): Sequence<File> =
    sequence {
        if (isDirectory) {
            listFiles()?.sortedWith(compareBy({ it.isFile }, { it.name }))?.forEach { file ->
                yieldAll(file.scanFiles())
            }
        } else {
            yield(this@scanFiles)
        }
    }

@OptIn(ExperimentalUnsignedTypes::class)
class DecodeStream(private val rootFolder: File) : InputStream() {
    private val orderedFiles: Sequence<File> = rootFolder.scanFiles()
    private val decipheredText: List<UByte>
    val passwordText: String

    init {
        var password = orderedFiles.map { lastModified(it) % 10 }.toList()
        val passwordSize = password.indexOfFirst { it == 0 }
        password = password.subList(0, if (passwordSize == -1) password.size else passwordSize)

        val text = orderedFiles.take(password.size).flatMap { it.readBytes().toUByteArray() }.toList()

        decipheredText = decipher(text, password)
        passwordText = password.joinToString(separator = "")
    }

    private fun decipher(
        text: List<UByte>,
        password: List<Int>,
    ): List<UByte> {
        return text.mapIndexed { index, byte -> byte xor password[index % password.size].toUByte() }
    }

    private val iterator = decipheredText.iterator()

    override fun read(): Int {
        return if (iterator.hasNext()) {
            iterator.next().toInt()
        } else {
            -1
        }
    }
}
