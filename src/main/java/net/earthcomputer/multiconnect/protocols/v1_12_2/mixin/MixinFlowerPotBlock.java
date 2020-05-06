package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.FlowerPotBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlowerPotBlock.class)
public class MixinFlowerPotBlock extends Block implements BlockEntityProvider {

    public MixinFlowerPotBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasBlockEntity() {
        return ConnectionInfo.protocolVersion <= Protocols.V1_12_2;
    }

    @Override
    public BlockEntity createBlockEntity(BlockView view) {
        return hasBlockEntity() ? new FlowerPotBlockEntity() : null;
    }
}
