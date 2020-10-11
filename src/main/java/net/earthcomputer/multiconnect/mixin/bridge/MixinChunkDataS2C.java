package net.earthcomputer.multiconnect.mixin.bridge;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.util.EightWayDirection;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public abstract class MixinChunkDataS2C implements IChunkDataS2CPacket {
    @Shadow private List<CompoundTag> blockEntities;

    @Unique
    private boolean dataTranslated = false;
    @Unique
    private DimensionType dimension;
    @Unique
    private EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdate;

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

    @Inject(method = "read", at = @At("RETURN"))
    private void onRead(CallbackInfo ci) {
        DefaultRegistries<?> defaultBlockEntities = DefaultRegistries.DEFAULT_REGISTRIES.get(Registry.BLOCK_ENTITY_TYPE);
        for (int i = 0; i < blockEntities.size(); i++) {
            CompoundTag blockEntity = blockEntities.get(i);
            Identifier blockEntityId = Identifier.tryParse(blockEntity.getString("id"));
            if (blockEntityId != null) {
                final int i_f = i;
                Registry.BLOCK_ENTITY_TYPE.getOrEmpty(blockEntityId).ifPresent(type -> {
                    if (defaultBlockEntities.defaultEntryToRawId.containsKey(type)) {
                        CompoundTag fixed = Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntity);
                        fixed.putString("id", blockEntityId.toString());
                        blockEntities.set(i_f, fixed);
                    }
                });
            }
        }
    }

    @Override
    public void multiconnect_setDimension(DimensionType dimension) {
        this.dimension = dimension;
    }

    @Override
    public void multiconnect_setBlocksNeedingUpdate(EnumMap<EightWayDirection, ShortSet> blocksNeedingUpdate) {
        this.blocksNeedingUpdate = blocksNeedingUpdate;
    }

    @Override
    public EnumMap<EightWayDirection, ShortSet> multiconnect_getBlocksNeedingUpdate() {
        return blocksNeedingUpdate;
    }

    @Override
    @Accessor
    public abstract void setData(byte[] data);
}
