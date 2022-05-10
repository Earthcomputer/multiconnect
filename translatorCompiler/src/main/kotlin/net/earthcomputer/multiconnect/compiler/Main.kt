@file:JvmName("Main")

package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.compiler.gen.ProtocolCompiler
import net.earthcomputer.multiconnect.compiler.gen.checkMessages
import java.io.File
import java.util.BitSet

object FileLocations {
    lateinit var jsonDir: File
    lateinit var dataDir: File
    lateinit var outputDir: File
}

fun main(args: Array<String>) {
    FileLocations.jsonDir = File(args[0])
    FileLocations.dataDir = File(args[1])
    FileLocations.outputDir = File(args[2])
    if (!FileLocations.outputDir.exists()) {
        FileLocations.outputDir.mkdirs()
    }

    protocols = readCsv(File(FileLocations.dataDir, "protocols.csv"))
    protocolNamesById = protocols.associate { it.id to it.name }
    protocolDatafixVersionsById = protocols.associate { it.id to it.datafixVersion }
    allPackets = protocols.flatMap {  protocol ->
        listOf("spackets.csv", "cpackets.csv").flatMap { fileName ->
            readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve(fileName)).map { it.clazz }
        }
    }.toSet()
    fillIndexes()
    checkMessages()

    for (protocol in protocols) {
        ProtocolCompiler(protocol.name, protocol.id).compile()
    }

    ProtocolCompiler(protocols[0].name, protocols[0].id).compileDefaultConstructors()
}

private val packageClassSplitRegex = "(.+?)\\.([A-Z].*)".toRegex()
fun splitPackageClass(className: String): Pair<String, String> {
    val match = packageClassSplitRegex.matchEntire(className) ?: throw IllegalArgumentException("Invalid class name: $className")
    return match.groupValues[1] to match.groupValues[2]
}

/**
 * See PacketIntrinsics.makeRLEBitSet
 */
fun encodeRLEBitSet(bitset: BitSet): String {
    return buildString {
        fun appendVarChar(value_: Int) {
            if (value_ == 0) {
                append('\u0000')
                return
            }
            var value = value_
            while (value > 0) {
                val char = ((if ((value and 0x7fff) != value) 0x8000 else 0) or (value and 0x7fff)).toChar()
                append(char)
                value = value ushr 15
            }
        }

        var prevIndex = 0
        var index = bitset.nextSetBit(0)
        while (index >= 0) {
            appendVarChar(index - prevIndex)
            prevIndex = index
            index = bitset.nextClearBit(index + 1)
            appendVarChar(index - prevIndex)
            prevIndex = index
            index = bitset.nextSetBit(index + 1)
        }
    }
}

