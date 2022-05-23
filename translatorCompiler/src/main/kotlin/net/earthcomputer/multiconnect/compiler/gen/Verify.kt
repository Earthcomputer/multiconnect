package net.earthcomputer.multiconnect.compiler.gen

import net.earthcomputer.multiconnect.compiler.FileLocations
import net.earthcomputer.multiconnect.compiler.McType
import net.earthcomputer.multiconnect.compiler.MessageInfo
import net.earthcomputer.multiconnect.compiler.MessageVariantInfo
import net.earthcomputer.multiconnect.compiler.PacketType
import net.earthcomputer.multiconnect.compiler.deepComponentType
import net.earthcomputer.multiconnect.compiler.getClassInfoOrNull
import net.earthcomputer.multiconnect.compiler.getMessageVariantInfo
import net.earthcomputer.multiconnect.compiler.groups
import net.earthcomputer.multiconnect.compiler.polymorphicChildren
import net.earthcomputer.multiconnect.compiler.protocols
import net.earthcomputer.multiconnect.compiler.readCsv

fun checkMessages() {
    val usedByProtocol = mutableMapOf<Int, Set<String>>()
    for (protocol in protocols) {
        val used = mutableSetOf<String>()
        fun processUsed(message: String) {
            if (used.add(message)) {
                val info = getMessageVariantInfo(message)
                if ((info.minVersion != null && protocol.id < info.minVersion) || (info.maxVersion != null && protocol.id > info.maxVersion)) {
                    throw IllegalStateException("Protocol $protocol uses message variant $message, but is not supported by that message variant")
                }
                for (field in info.fields) {
                    val type = field.type.realType.deepComponentType()
                    if (type is McType.DeclaredType) {
                        when (val classInfo = getClassInfoOrNull(type.name)) {
                            is MessageInfo -> classInfo.getVariant(protocol.id)?.let { processUsed(it.className) }
                            is MessageVariantInfo -> processUsed(type.name)
                            else -> {}
                        }
                    }
                }
                if (info.polymorphic != null && info.polymorphicParent == null) {
                    for (child in polymorphicChildren[message]!!) {
                        processUsed(child)
                    }
                }
            }
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("cpackets.csv"))) {
            if (getClassInfoOrNull(packet.clazz) !is MessageVariantInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message variant")
            }
            processUsed(packet.clazz)
        }
        for (packet in readCsv<PacketType>(FileLocations.dataDir.resolve(protocol.name).resolve("spackets.csv"))) {
            if (getClassInfoOrNull(packet.clazz) !is MessageVariantInfo) {
                throw IllegalStateException("Packet class ${packet.clazz} is not a message variant")
            }
            processUsed(packet.clazz)
        }
        usedByProtocol[protocol.id] = used
    }

    for (group in groups.values) {
        var lastMaxVersion: Int? = null
        for ((messageIndex, message) in group.withIndex()) {
            val info = getMessageVariantInfo(message)
            if (messageIndex != 0 && (lastMaxVersion == null || info.minVersion == null || info.minVersion <= lastMaxVersion)) {
                throw IllegalStateException("Message $message has a lower min version than the max version of the previous message in the variant group")
            }
            val index = if (info.minVersion == null) protocols.lastIndex else protocols.binarySearch { info.minVersion.compareTo(it.id) }
            if (index < 0) {
                throw IllegalStateException("Message $message has a min version that is not in the protocol list")
            }
            if (info.maxVersion != null && protocols.binarySearch { info.maxVersion.compareTo(it.id) } < 0) {
                throw IllegalStateException("Message $message has a max version that is not in the protocol list")
            }
            if (messageIndex != 0) {
                assert(index != protocols.lastIndex)
                if (protocols[index + 1].id != lastMaxVersion) {
                    throw IllegalStateException("Message $message has a gap in the protocol versions before it")
                }
            }
            lastMaxVersion = info.maxVersion

            if (info.minVersion != null || info.maxVersion != null) {
                for (i in index downTo 0) {
                    val protocol = protocols[i].id
                    // check that this message is used somewhere in this protocol
                    if (usedByProtocol[protocol]?.contains(message) != true) {
                        throw IllegalStateException("Message variant $message is not used in protocol $protocol which it supports")
                    }
                    if (protocol == info.maxVersion) {
                        break
                    }
                }
            }
        }
    }
}
