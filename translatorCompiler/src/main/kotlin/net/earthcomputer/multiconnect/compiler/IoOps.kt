package net.earthcomputer.multiconnect.compiler

object IoOps {
    fun readVarIntOp(): McNodeOp {
        return FunctionCallOp(COMMON_TYPES, "readVarInt", listOf(McType.DeclaredType(BYTE_BUF)), McType.INT, true)
    }
}
