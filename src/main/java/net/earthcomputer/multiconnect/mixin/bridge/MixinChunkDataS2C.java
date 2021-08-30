package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.ints.IntSet;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public abstract class MixinChunkDataS2C implements IChunkDataS2CPacket {
    @Shadow @Final private List<NbtCompound> blockEntities;

    @Unique
    private boolean dataTranslated = false;
    @Unique
    private DimensionType dimension;
    @Unique
    private EnumMap<EightWayDirection, IntSet> blocksNeedingUpdate;

    @Override
    public boolean multiconnect_isDataTranslated() {
        return dataTranslated;
    }

    @Override
    public void multiconnect_setDataTranslated(boolean dataTranslated) {
        this.dataTranslated = dataTranslated;
    }

    @Override
    public DimensionType multiconnect_getDimension() {
        return dimension;
    }

    @Inject(method = "<init>(Lnet/minecraft/network/PacketByteBuf;)V", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        DefaultRegistries<?> defaultBlockEntities = DefaultRegistries.DEFAULT_REGISTRIES.get(Registry.BLOCK_ENTITY_TYPE);
        for (int i = 0; i < blockEntities.size(); i++) {
            NbtCompound blockEntity = blockEntities.get(i);
            BlockEntityType<?> blockEntityType;
            if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
                blockEntityType = Protocol_1_10.getBlockEntityById(blockEntity.getString("id"));
            } else {
                Identifier blockEntityId = Identifier.tryParse(blockEntity.getString("id"));
                blockEntityType = blockEntityId == null ? null : Registry.BLOCK_ENTITY_TYPE.getOrEmpty(blockEntityId).orElse(null);
            }
            if (blockEntityType != null) {
                if (defaultBlockEntities.defaultEntryToRawId.containsKey(blockEntityType)) {
                    NbtCompound fixed = Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntity);
                    fixed.putString("id", String.valueOf(Registry.BLOCK_ENTITY_TYPE.getId(blockEntityType)));
                    blockEntities.set(i, fixed);
                }
            }
        }
    }

    @Override
    public void multiconnect_setDimension(DimensionType dimension) {
        this.dimension = dimension;
    }

    @Override
    public void multiconnect_setBlocksNeedingUpdate(EnumMap<EightWayDirection, IntSet> blocksNeedingUpdate) {
        this.blocksNeedingUpdate = blocksNeedingUpdate;
    }

    @Override
    public EnumMap<EightWayDirection, IntSet> multiconnect_getBlocksNeedingUpdate() {
        return blocksNeedingUpdate;
    }

    @Override
    @Accessor
    @Mutable
    public abstract void setData(byte[] data);

    @Override
    @Accessor
    @Mutable
    public abstract void setVerticalStripBitmask(BitSet verticalStripBitmask);
}
