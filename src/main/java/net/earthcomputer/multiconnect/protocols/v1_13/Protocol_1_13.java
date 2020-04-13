package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13.mixin.ProjectileEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TNTBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.play.client.CEditBookPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class Protocol_1_13 extends Protocol_1_13_1 {

    public static void registerTranslators() {
        ProtocolRegistry.registerOutboundTranslator(CEditBookPacket.class, buf -> {
            buf.passthroughWrite(ItemStack.class); // book item
            buf.passthroughWrite(Boolean.class); // signed
            buf.skipWrite(Hand.class); // hand
        });
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);
        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        }
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.DEAD_TUBE_CORAL);
        registry.unregister(Blocks.DEAD_BRAIN_CORAL);
        registry.unregister(Blocks.DEAD_HORN_CORAL);
        registry.unregister(Blocks.DEAD_BUBBLE_CORAL);
        registry.unregister(Blocks.DEAD_FIRE_CORAL);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() == Blocks.TNT && state.get(TNTBlock.UNSTABLE))
            return false;
        return super.acceptBlockState(state);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, DataParameter<?> data) {
        if (clazz == AbstractArrowEntity.class && data == ProjectileEntityAccessor.getOptionalUuid())
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
