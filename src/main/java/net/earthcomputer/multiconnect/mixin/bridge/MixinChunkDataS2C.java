package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IChunkDataS2CPacket;
import net.earthcomputer.multiconnect.impl.Utils;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ChunkDataS2CPacket.class)
public abstract class MixinChunkDataS2C implements IChunkDataS2CPacket {
    @Shadow private List<CompoundTag> blockEntities;

    @Unique
    private boolean dataTranslated = false;
    @Unique
    private DimensionType dimension;

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
        for (int i = 0; i < blockEntities.size(); i++) {
            blockEntities.set(i, Utils.datafix(TypeReferences.BLOCK_ENTITY, blockEntities.get(i)));
        }
    }

    @Override
    public void multiconnect_setDimension(DimensionType dimension) {
        this.dimension = dimension;
    }

    @Override
    @Accessor
    public abstract void setData(byte[] data);
}
