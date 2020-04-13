package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.FlowerPotBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FlowerPotBlock.class)
public class MixinFlowerPotBlock extends Block implements ITileEntityProvider {

    public MixinFlowerPotBlock(Properties settings) {
        super(settings);
    }

    @Override
    public boolean hasTileEntity() {
        return ConnectionInfo.protocolVersion <= Protocols.V1_12_2;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader view) {
        return hasTileEntity() ? new FlowerPotBlockEntity() : null;
    }
}
