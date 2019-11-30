package net.earthcomputer.multiconnect.impl;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.NetworkState;
import net.minecraft.network.Packet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

public class TransformerByteBuf extends PacketByteBuf {

    private ChannelHandlerContext context;
    private Class<? extends Packet<?>> packetClass;
    private int reentrantDepth = 0;

    public TransformerByteBuf() {
        super(null);
    }

    public TransformerByteBuf(ByteBuf delegate, ChannelHandlerContext context) {
        super(delegate);
        this.context = context;
    }

    public void setUserData(int val) {
        if (getDelegate() instanceof TransformerByteBuf)
            ((TransformerByteBuf) getDelegate()).setUserData(val);
    }

    @Override
    public int readVarInt() {
        int val;
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            val = ((PacketByteBuf) getDelegate()).readVarInt();
        } else {
            reentrantDepth++;
            try {
                val = super.readVarInt();
            } finally {
                reentrantDepth--;
            }
        }

        if (packetClass == null) {
            NetworkState state = context.channel().attr(ClientConnection.ATTR_KEY_PROTOCOL).get();
            packetClass = ((INetworkState) state).getPacketHandlerMap().get(NetworkSide.CLIENTBOUND).get(val);
            List<TransformerByteBuf> transformers = new ArrayList<>(0);
            // populate with newer transformers first
            ConnectionInfo.protocol.transformPacketClientbound(packetClass, transformers);
            ByteBuf delegate = getDelegate();
            for (TransformerByteBuf transformer : transformers) {
                transformer.setDelegate(delegate);
                transformer.context = context;
                transformer.packetClass = packetClass;
                delegate = transformer;
            }
            setDelegate(delegate);
        }

        return val;
    }

    @Override
    public PacketByteBuf writeVarInt(int val) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeVarInt(val);
        } else {
            reentrantDepth++;
            try {
                super.writeVarInt(val);
            } finally {
                reentrantDepth--;
            }
        }

        if (packetClass == null) {
            NetworkState state = context.channel().attr(ClientConnection.ATTR_KEY_PROTOCOL).get();
            packetClass = ((INetworkState) state).getPacketHandlerMap().get(NetworkSide.SERVERBOUND).get(val);
            List<TransformerByteBuf> transformers = new ArrayList<>(0);
            // populate with newer transformers first
            ConnectionInfo.protocol.transformPacketServerbound(packetClass, transformers);
            ByteBuf delegate = getDelegate();
            for (int i = transformers.size() - 1; i >= 0; i--) {
                TransformerByteBuf transformer = transformers.get(i);
                transformer.setDelegate(delegate);
                transformer.context = context;
                transformer.packetClass = packetClass;
                delegate = transformer;
            }
            setDelegate(delegate);
        }

        return this;
    }

    public PacketByteBuf writeByteArray(byte[] bytes) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeByteArray(bytes);
        } else {
            reentrantDepth++;
            try {
                super.writeByteArray(bytes);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public byte[] readByteArray() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readByteArray();
        } else {
            reentrantDepth++;
            try {
                return super.readByteArray();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public byte[] readByteArray(int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readByteArray(maxSize);
        } else {
            reentrantDepth++;
            try {
                return super.readByteArray(maxSize);
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeIntArray(int[] ints) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeIntArray(ints);
        } else {
            reentrantDepth++;
            try {
                super.writeIntArray(ints);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public int[] readIntArray() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIntArray();
        } else {
            reentrantDepth++;
            try {
                return super.readIntArray();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public int[] readIntArray(int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIntArray(maxSize);
        } else {
            reentrantDepth++;
            try {
                return super.readIntArray(maxSize);
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeLongArray(long[] longs) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeLongArray(longs);
        } else {
            reentrantDepth++;
            try {
                super.writeLongArray(longs);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    @Environment(EnvType.CLIENT)
    public long[] readLongArray(long[] dest) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readLongArray(dest);
        } else {
            reentrantDepth++;
            try {
                return super.readLongArray(dest);
            } finally {
                reentrantDepth--;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public long[] readLongArray(long[] dest, int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readLongArray(dest, maxSize);
        } else {
            reentrantDepth++;
            try {
                return super.readLongArray(dest, maxSize);
            } finally {
                reentrantDepth--;
            }
        }
    }

    public BlockPos readBlockPos() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readBlockPos();
        } else {
            reentrantDepth++;
            try {
                return super.readBlockPos();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeBlockPos(BlockPos pos) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeBlockPos(pos);
        } else {
            reentrantDepth++;
            try {
                super.writeBlockPos(pos);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    @Environment(EnvType.CLIENT)
    public ChunkSectionPos readChunkSectionPos() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readChunkSectionPos();
        } else {
            reentrantDepth++;
            try {
                return super.readChunkSectionPos();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public Text readText() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readText();
        } else {
            reentrantDepth++;
            try {
                return super.readText();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeText(Text text) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeText(text);
        } else {
            reentrantDepth++;
            try {
                super.writeText(text);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public <T extends Enum<T>> T readEnumConstant(Class<T> clazz) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readEnumConstant(clazz);
        } else {
            reentrantDepth++;
            try {
                return super.readEnumConstant(clazz);
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeEnumConstant(Enum<?> val) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeEnumConstant(val);
        } else {
            reentrantDepth++;
            try {
                super.writeEnumConstant(val);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public long readVarLong() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readVarLong();
        } else {
            reentrantDepth++;
            try {
                return super.readVarLong();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeUuid(UUID uuid) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeUuid(uuid);
        } else {
            reentrantDepth++;
            try {
                super.writeUuid(uuid);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public UUID readUuid() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readUuid();
        } else {
            reentrantDepth++;
            try {
                return super.readUuid();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeVarLong(long val) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeVarLong(val);
        } else {
            reentrantDepth++;
            try {
                super.writeVarLong(val);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public PacketByteBuf writeCompoundTag(CompoundTag nbt) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeCompoundTag(nbt);
        } else {
            reentrantDepth++;
            try {
                super.writeCompoundTag(nbt);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public CompoundTag readCompoundTag() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readCompoundTag();
        } else {
            reentrantDepth++;
            try {
                return super.readCompoundTag();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeItemStack(ItemStack stack) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeItemStack(stack);
        } else {
            reentrantDepth++;
            try {
                super.writeItemStack(stack);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public ItemStack readItemStack() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readItemStack();
        } else {
            reentrantDepth++;
            try {
                return super.readItemStack();
            } finally {
                reentrantDepth--;
            }
        }
    }

    @Environment(EnvType.CLIENT)
    public String readString() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readString();
        } else {
            reentrantDepth++;
            try {
                return super.readString();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public String readString(int maxLen) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readString(maxLen);
        } else {
            reentrantDepth++;
            try {
                return super.readString(maxLen);
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeString(String str) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeString(str);
        } else {
            reentrantDepth++;
            try {
                super.writeString(str);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public PacketByteBuf writeString(String string_1, int int_1) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeString(string_1, int_1);
        } else {
            reentrantDepth++;
            try {
                super.writeString(string_1, int_1);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public Identifier readIdentifier() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIdentifier();
        } else {
            reentrantDepth++;
            try {
                return super.readIdentifier();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeIdentifier(Identifier id) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeIdentifier(id);
        } else {
            reentrantDepth++;
            try {
                super.writeIdentifier(id);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public Date readDate() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readDate();
        } else {
            reentrantDepth++;
            try {
                return super.readDate();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public PacketByteBuf writeDate(Date date) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeDate(date);
        } else {
            reentrantDepth++;
            try {
                super.writeDate(date);
            } finally {
                reentrantDepth--;
            }
        }
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readBlockHitResult();
        } else {
            reentrantDepth++;
            try {
                return super.readBlockHitResult();
            } finally {
                reentrantDepth--;
            }
        }
    }

    public void writeBlockHitResult(BlockHitResult hitResult) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeBlockHitResult(hitResult);
        } else {
            reentrantDepth++;
            try {
                super.writeBlockHitResult(hitResult);
            } finally {
                reentrantDepth--;
            }
        }
    }

    protected boolean isTopLevel() {
        return reentrantDepth == 0;
    }

    private void setDelegate(ByteBuf delegate) {
        try {
            DELEGATE_FIELD.set(this, delegate);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    private ByteBuf getDelegate() {
        return ((IPacketByteBuf) this).getParent();
    }

    private static final Field DELEGATE_FIELD;
    static {
        try {
            DELEGATE_FIELD = Arrays.stream(PacketByteBuf.class.getDeclaredFields())
                    .filter(it -> it.getType() == ByteBuf.class)
                    .findFirst().orElseThrow(NoSuchFieldException::new);
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.set(DELEGATE_FIELD, DELEGATE_FIELD.getModifiers() & ~Modifier.FINAL);
            DELEGATE_FIELD.setAccessible(true);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }
}
