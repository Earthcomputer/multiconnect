package net.earthcomputer.multiconnect.compiler

import net.earthcomputer.multiconnect.ap.Types
import net.earthcomputer.multiconnect.compiler.node.BinaryExpressionOp
import net.earthcomputer.multiconnect.compiler.node.CstIntOp
import net.earthcomputer.multiconnect.compiler.node.FunctionCallOp
import net.earthcomputer.multiconnect.compiler.node.LoadVariableOp
import net.earthcomputer.multiconnect.compiler.node.McNode
import net.earthcomputer.multiconnect.compiler.node.NewOp
import net.earthcomputer.multiconnect.compiler.node.PopStmtOp
import net.earthcomputer.multiconnect.compiler.node.StmtListOp
import net.earthcomputer.multiconnect.compiler.node.VariableId

object IoOps {
    fun skipOver(bufVar: VariableId, type: Types): McNode {
        if (type.skipLength != 0) {
            throw IllegalArgumentException("$type has a constant skip length")
        }
        return when (type) {
            Types.MESSAGE -> throw IllegalArgumentException("One does not simply skip over a message") // should have been handled by the caller
            Types.VAR_INT, Types.VAR_LONG, Types.NBT_COMPOUND -> McNode(PopStmtOp, readType(bufVar, type))
            Types.STRING, Types.BITSET -> {
                var readLength = McNode(
                    FunctionCallOp(CommonClassNames.PACKET_INTRINSICS, "readVarInt", listOf(McType.BYTE_BUF), McType.INT, true),
                    McNode(LoadVariableOp(bufVar, McType.BYTE_BUF))
                )
                if (type == Types.BITSET) {
                    readLength = McNode(
                        BinaryExpressionOp("*", McType.INT, McType.INT),
                        readLength,
                        McNode(CstIntOp(8))
                    )
                }
                McNode(
                    StmtListOp,
                    McNode(
                        PopStmtOp,
                        McNode(
                            FunctionCallOp(CommonClassNames.BYTE_BUF, "skipBytes", listOf(McType.BYTE_BUF, McType.INT), McType.VOID, true, isStatic = false),
                            McNode(LoadVariableOp(bufVar, McType.BYTE_BUF)),
                            readLength
                        )
                    )
                )
            }
            Types.IDENTIFIER -> skipOver(bufVar, Types.STRING)
            else -> throw AssertionError()
        }
    }

    fun readType(bufVar: VariableId, type: Types): McNode {
        fun byteBuf(name: String, returnType: McType): McNode {
            return McNode(
                FunctionCallOp(CommonClassNames.BYTE_BUF, name, listOf(McType.BYTE_BUF), returnType, true, isStatic = false),
                McNode(LoadVariableOp(bufVar, McType.BYTE_BUF))
            )
        }
        fun intrinsic(name: String, returnType: McType): McNode {
            return McNode(
                FunctionCallOp(CommonClassNames.PACKET_INTRINSICS, name, listOf(McType.BYTE_BUF), returnType, true),
                McNode(LoadVariableOp(bufVar, McType.BYTE_BUF))
            )
        }
        return when (type) {
            Types.MESSAGE -> throw IllegalArgumentException("One does not simply read a message") // should have been handled by the caller
            Types.DOUBLE -> byteBuf("readDouble", McType.DOUBLE)
            Types.FLOAT -> byteBuf("readFloat", McType.FLOAT)
            Types.LONG -> byteBuf("readLong", McType.LONG)
            Types.UNSIGNED_BYTE -> byteBuf("readUnsignedByte", McType.SHORT)
            Types.VAR_LONG -> intrinsic("readVarLong", McType.LONG)
            Types.VAR_INT -> intrinsic("readVarInt", McType.INT)
            Types.BOOLEAN -> byteBuf("readBoolean", McType.BOOLEAN)
            Types.BYTE -> byteBuf("readByte", McType.BYTE)
            Types.INT -> byteBuf("readInt", McType.INT)
            Types.SHORT -> byteBuf("readShort", McType.SHORT)
            Types.NBT_COMPOUND -> intrinsic("readNbtCompound", McType.DeclaredType(CommonClassNames.NBT_COMPOUND))
            Types.IDENTIFIER -> McNode(NewOp(CommonClassNames.IDENTIFIER, listOf(McType.STRING)), intrinsic("readString", McType.STRING))
            Types.STRING -> intrinsic("readString", McType.STRING)
            Types.UUID -> McNode(NewOp(CommonClassNames.UUID, listOf(McType.LONG, McType.LONG)), byteBuf("readLong", McType.LONG), byteBuf("readLong", McType.LONG))
            Types.BITSET -> intrinsic("readBitSet", McType.DeclaredType(CommonClassNames.BITSET))
        }
    }

    fun writeType(bufVar: VariableId, type: Types, value: McNode): McNode {
        fun byteBuf(name: String, type: McType, value: McNode): McNode {
            return McNode(PopStmtOp,
                McNode(FunctionCallOp(CommonClassNames.BYTE_BUF, name, listOf(McType.BYTE_BUF, type), McType.VOID, true, isStatic = false),
                    McNode(LoadVariableOp(bufVar, McType.BYTE_BUF)),
                    value.castIfNecessary(type)
                )
            )
        }
        fun intrinsic(name: String, type: McType, value: McNode): McNode {
            return McNode(PopStmtOp,
                McNode(FunctionCallOp(CommonClassNames.PACKET_INTRINSICS, name, listOf(McType.BYTE_BUF, type), McType.VOID, true),
                    McNode(LoadVariableOp(bufVar, McType.BYTE_BUF)),
                    value.castIfNecessary(type)
                )
            )
        }
        return when (type) {
            Types.MESSAGE -> throw IllegalArgumentException("One does not simply write a message") // should have been handled by the caller
            Types.DOUBLE -> byteBuf("writeDouble", McType.DOUBLE, value)
            Types.FLOAT -> byteBuf("writeFloat", McType.FLOAT, value)
            Types.LONG -> byteBuf("writeLong", McType.LONG, value)
            Types.UNSIGNED_BYTE -> byteBuf("writeByte", McType.INT, value)
            Types.VAR_LONG -> intrinsic("writeVarLong", McType.LONG, value)
            Types.VAR_INT -> intrinsic("writeVarInt", McType.INT, value)
            Types.BOOLEAN -> byteBuf("writeBoolean", McType.BOOLEAN, value)
            Types.BYTE -> byteBuf("writeByte", McType.INT, value)
            Types.INT -> byteBuf("writeInt", McType.INT, value)
            Types.SHORT -> byteBuf("writeShort", McType.INT, value)
            Types.NBT_COMPOUND -> intrinsic("writeNbtCompound", McType.DeclaredType(CommonClassNames.NBT_COMPOUND), value)
            Types.IDENTIFIER -> intrinsic("writeString", McType.STRING, McNode(
                FunctionCallOp(CommonClassNames.IDENTIFIER, "toString", listOf(McType.DeclaredType(CommonClassNames.IDENTIFIER)), McType.STRING, false, isStatic = false),
                value
            ))
            Types.STRING -> intrinsic("writeString", McType.STRING, value)
            Types.UUID -> McNode(
                StmtListOp,
                byteBuf("writeLong", McType.LONG, McNode(
                    FunctionCallOp(CommonClassNames.UUID, "getMostSignificantBits", listOf(McType.DeclaredType(CommonClassNames.UUID)), McType.LONG, false, isStatic = false),
                    value
                )),
                byteBuf("writeLong", McType.LONG, McNode(
                    FunctionCallOp(CommonClassNames.UUID, "getLeastSignificantBits", listOf(McType.DeclaredType(CommonClassNames.UUID)), McType.LONG, false, isStatic = false),
                    value
                ))
            )
            Types.BITSET -> intrinsic("writeBitSet", McType.DeclaredType(CommonClassNames.BITSET), value)
        }
    }
}

val Types.skipLength
    get() = when (this) {
        Types.BOOLEAN -> 1
        Types.BYTE -> 1
        Types.UNSIGNED_BYTE -> 1
        Types.SHORT -> 2
        Types.INT -> 4
        Types.FLOAT -> 4
        Types.LONG -> 8
        Types.DOUBLE -> 8
        Types.UUID -> 16
        else -> 0
    }
