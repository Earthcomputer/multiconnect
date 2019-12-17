package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.packet.BookUpdateC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

import java.lang.reflect.Field;
import java.util.Optional;

public class Protocol_1_13 extends Protocol_1_13_1 {

    private static final Field PROJECTILE_UUID = DataTrackerManager.getTrackedDataField(ProjectileEntity.class, 1, "OPTIONAL_UUID");

    @Override
    public void registerTranslators() {
        ProtocolRegistry.registerOutboundTranslator(BookUpdateC2SPacket.class, buf -> {
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
        if (state.getBlock() == Blocks.TNT && state.get(TntBlock.UNSTABLE))
            return false;
        return super.acceptBlockState(state);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == ProjectileEntity.class && data == DataTrackerManager.getTrackedData(Optional.class, PROJECTILE_UUID))
            return false;
        return super.acceptEntityData(clazz, data);
    }
}
