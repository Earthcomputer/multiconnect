@file:JvmName("Main")

package net.earthcomputer.multiconnect.compiler

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import java.io.File

const val BYTE_BUF = "io.netty.buffer.ByteBuf"
const val COMMON_TYPES = "net.earthcomputer.multiconnect.packets.CommonTypes"
const val STRING = "java.lang.String"

lateinit var jsonDir: File
lateinit var dataDir: File
lateinit var outputDir: File

lateinit var protocols: List<Protocol>

fun main(args: Array<String>) {
    jsonDir = File(args[0])
    dataDir = File(args[1])
    outputDir = File(args[2])
    if (!outputDir.exists()) {
        outputDir.mkdirs()
    }

    protocols = readCsv(File(dataDir, "protocols.csv"))
    for (protocol in protocols) {
        ProtocolCompiler.compile(protocol.name)
    }
}

inline fun <reified T> readCsv(file: File): List<T> {
    return file.useLines { lines ->
        val itr = lines
            .map { it.substringBefore('#') }
            .filter { it.isNotBlank() }
            .iterator()
        val keys = itr.next().split(" ")
        itr.asSequence()
            .map { line ->
                Json.decodeFromJsonElement<T>(JsonObject(
                    keys.zip(line.split(" ").map { JsonPrimitive(it) }).toMap()
                ))
            }
            .toList()
    }
}
