package net.earthcomputer.multiconnect.compiler

import kotlinx.serialization.Serializable

@Serializable
data class Protocol(val id: Int, val name: String)

@Serializable
data class PacketType(val id: Int, val clazz: String)
