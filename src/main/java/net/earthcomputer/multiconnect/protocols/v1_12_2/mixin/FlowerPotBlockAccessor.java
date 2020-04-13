package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.impl.MixinHelper;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(FlowerPotBlock.class)
public interface FlowerPotBlockAccessor {

    @Accessor("field_196451_b")
    static Map<Block, Block> getContentToPotted() {
        return MixinHelper.fakeInstance();
    }

}
