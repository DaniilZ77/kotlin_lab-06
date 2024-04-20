package ru.spbsu.kotlin

import java.io.File
import java.io.OutputStream
import java.util.Calendar
import kotlin.experimental.xor
import kotlin.io.path.Path
import kotlin.math.max
import kotlin.math.sqrt
import kotlin.random.Random

fun getRandomName(length: Int): String {
    val alphabet = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length).map { alphabet.random() }.joinToString("")
}

fun fixLastModified(toFix: Int): Long {
    val time = Calendar.getInstance().timeInMillis
    val lastThree = time % 1000
    return (time / 10000 * 10 + toFix) * 1000 + lastThree
}

class EncodeStream(private val rootFolder: File, private val password: String) : OutputStream() {
    companion object {
        private const val FACTOR = 3
    }

    private val orderedFiles: List<File>

    private val chosenDirectories: HashMap<Int, Int> = hashMapOf()

    private var text = mutableListOf<Byte>()

    init {
        if (password.any { (it - '0') !in 1..9 } || password.isEmpty()) {
            throw IllegalArgumentException("Преступник не любит цифру 0!!!")
        }

        val nameSize = max(sqrt(password.length.toDouble()).toInt(), 10)

        val amountOfDirs = FACTOR * password.length

        for (i in 1..password.length) {
            val randomDirectory = Random.nextInt(amountOfDirs)

            chosenDirectories.run { set(randomDirectory, getOrDefault(randomDirectory, 0) + 1) }
        }

        generateFolders(rootFolder, 0, amountOfDirs, nameSize)

        orderedFiles = rootFolder.scanFiles().toList()
    }

    private fun generateFolders(
        currentFolder: File,
        currentDir: Int,
        amountOfDirs: Int,
        nameSize: Int,
    ) {
        if (currentDir > amountOfDirs) {
            return
        }

        val directory =
            Path(currentFolder.absolutePath, currentDir.toString() + getRandomName(nameSize))
                .toFile()
        directory.mkdirs()

        for (fileIndex in 1..chosenDirectories.getOrDefault(currentDir, 0)) {
            val newTXT = Path(directory.absolutePath, fileIndex.toString() + getRandomName(nameSize)).toFile()
            newTXT.createNewFile()
        }

        generateFolders(directory, 2 * currentDir + 1, amountOfDirs, nameSize)
        generateFolders(directory, 2 * currentDir + 2, amountOfDirs, nameSize)
    }

    override fun write(b: Int) {
        text.add(b.toByte() xor (password[text.size % password.length] - '0').toByte())
    }

    private fun fflushEncoded() {
        var sizeOfText = text.size

        orderedFiles.forEachIndexed { index, file ->
            val amountOfBytes = Random.nextInt(sizeOfText)
            val toDrop = text.size - sizeOfText
            if (sizeOfText > 0) {
                file.writeBytes(text.drop(toDrop).take(amountOfBytes).toByteArray())
            }
            file.setLastModified(fixLastModified(password[index] - '0'))
            sizeOfText -= amountOfBytes
        }

        if (sizeOfText > 0) {
            orderedFiles.last().writeBytes(text.takeLast(sizeOfText).toByteArray())
            orderedFiles.last().setLastModified(fixLastModified(password.last() - '0'))
        }
    }

    override fun close() {
        super.close()

        fflushEncoded()
    }
}
