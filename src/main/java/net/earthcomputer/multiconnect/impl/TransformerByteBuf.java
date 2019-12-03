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
import java.util.function.Supplier;

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
            val = getNotTopLevel(super::readVarInt);
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
            doNotTopLevel(() -> super.writeVarInt(val));
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
            doNotTopLevel(() -> super.writeByteArray(bytes));
        }
        return this;
    }

    public byte[] readByteArray() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readByteArray();
        } else {
            return getNotTopLevel(super::readByteArray);
        }
    }

    public byte[] readByteArray(int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readByteArray(maxSize);
        } else {
            return getNotTopLevel(() -> super.readByteArray(maxSize));
        }
    }

    public PacketByteBuf writeIntArray(int[] ints) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeIntArray(ints);
        } else {
            doNotTopLevel(() -> super.writeIntArray(ints));
        }
        return this;
    }

    public int[] readIntArray() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIntArray();
        } else {
            return getNotTopLevel(super::readIntArray);
        }
    }

    public int[] readIntArray(int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIntArray(maxSize);
        } else {
            return getNotTopLevel(() -> super.readIntArray(maxSize));
        }
    }

    public PacketByteBuf writeLongArray(long[] longs) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeLongArray(longs);
        } else {
            doNotTopLevel(() -> super.writeLongArray(longs));
        }
        return this;
    }

    @Environment(EnvType.CLIENT)
    public long[] readLongArray(long[] dest) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readLongArray(dest);
        } else {
            return getNotTopLevel(() -> super.readLongArray(dest));
        }
    }

    @Environment(EnvType.CLIENT)
    public long[] readLongArray(long[] dest, int maxSize) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readLongArray(dest, maxSize);
        } else {
            return getNotTopLevel(() -> super.readLongArray(dest, maxSize));
        }
    }

    public BlockPos readBlockPos() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readBlockPos();
        } else {
            return getNotTopLevel(super::readBlockPos);
        }
    }

    public PacketByteBuf writeBlockPos(BlockPos pos) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeBlockPos(pos);
        } else {
            doNotTopLevel(() -> super.writeBlockPos(pos));
        }
        return this;
    }

    @Environment(EnvType.CLIENT)
    public ChunkSectionPos readChunkSectionPos() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readChunkSectionPos();
        } else {
            return getNotTopLevel(super::readChunkSectionPos);
        }
    }

    public Text readText() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readText();
        } else {
            return getNotTopLevel(super::readText);
        }
    }

    public PacketByteBuf writeText(Text text) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeText(text);
        } else {
            doNotTopLevel(() -> super.writeText(text));
        }
        return this;
    }

    public <T extends Enum<T>> T readEnumConstant(Class<T> clazz) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readEnumConstant(clazz);
        } else {
            return getNotTopLevel(() -> super.readEnumConstant(clazz));
        }
    }

    public PacketByteBuf writeEnumConstant(Enum<?> val) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeEnumConstant(val);
        } else {
            doNotTopLevel(() -> super.writeEnumConstant(val));
        }
        return this;
    }

    public long readVarLong() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readVarLong();
        } else {
            return getNotTopLevel(super::readVarLong);
        }
    }

    public PacketByteBuf writeUuid(UUID uuid) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeUuid(uuid);
        } else {
            doNotTopLevel(() -> super.writeUuid(uuid));
        }
        return this;
    }

    public UUID readUuid() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readUuid();
        } else {
            return getNotTopLevel(super::readUuid);
        }
    }

    public PacketByteBuf writeVarLong(long val) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeVarLong(val);
        } else {
            doNotTopLevel(() -> super.writeVarLong(val));
        }
        return this;
    }

    public PacketByteBuf writeCompoundTag(CompoundTag nbt) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeCompoundTag(nbt);
        } else {
            doNotTopLevel(() -> super.writeCompoundTag(nbt));
        }
        return this;
    }

    public CompoundTag readCompoundTag() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readCompoundTag();
        } else {
            return getNotTopLevel(super::readCompoundTag);
        }
    }

    public PacketByteBuf writeItemStack(ItemStack stack) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeItemStack(stack);
        } else {
            doNotTopLevel(() -> super.writeItemStack(stack));
        }
        return this;
    }

    public ItemStack readItemStack() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readItemStack();
        } else {
            return getNotTopLevel(super::readItemStack);
        }
    }

    @Environment(EnvType.CLIENT)
    public String readString() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readString();
        } else {
            return getNotTopLevel(super::readString);
        }
    }

    public String readString(int maxLen) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readString(maxLen);
        } else {
            return getNotTopLevel(() -> super.readString(maxLen));
        }
    }

    public PacketByteBuf writeString(String str) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeString(str);
        } else {
            doNotTopLevel(() -> super.writeString(str));
        }
        return this;
    }

    public PacketByteBuf writeString(String string_1, int int_1) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeString(string_1, int_1);
        } else {
            doNotTopLevel(() -> super.writeString(string_1, int_1));
        }
        return this;
    }

    public Identifier readIdentifier() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readIdentifier();
        } else {
            return getNotTopLevel(super::readIdentifier);
        }
    }

    public PacketByteBuf writeIdentifier(Identifier id) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeIdentifier(id);
        } else {
            doNotTopLevel(() -> super.writeIdentifier(id));
        }
        return this;
    }

    public Date readDate() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readDate();
        } else {
            return getNotTopLevel(super::readDate);
        }
    }

    public PacketByteBuf writeDate(Date date) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeDate(date);
        } else {
            doNotTopLevel(() -> super.writeDate(date));
        }
        return this;
    }

    public BlockHitResult readBlockHitResult() {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            return ((PacketByteBuf) getDelegate()).readBlockHitResult();
        } else {
            return getNotTopLevel(super::readBlockHitResult);
        }
    }

    public void writeBlockHitResult(BlockHitResult hitResult) {
        if (getDelegate() instanceof PacketByteBuf && reentrantDepth == 0) {
            ((PacketByteBuf) getDelegate()).writeBlockHitResult(hitResult);
        } else {
            doNotTopLevel(() -> super.writeBlockHitResult(hitResult));
        }
    }

    protected boolean isTopLevel() {
        return reentrantDepth == 0;
    }

    protected void doNotTopLevel(Runnable action) {
        reentrantDepth++;
        try {
            action.run();
        } finally {
            reentrantDepth--;
        }
    }

    protected <T> T getNotTopLevel(Supplier<T> action) {
        reentrantDepth++;
        try {
            return action.get();
        } finally {
            reentrantDepth--;
        }
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
