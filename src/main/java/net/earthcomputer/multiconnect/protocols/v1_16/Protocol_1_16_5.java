package net.earthcomputer.multiconnect.protocols.v1_16;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.mixin.ShulkerAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.state.BlockState;
import java.util.*;

public class Protocol_1_16_5 extends Protocol_1_17 {
    public static final int BIOME_ARRAY_LENGTH = 1024;
    private static short lastActionId = 0;

    private static final EntityDataAccessor<Optional<BlockPos>> OLD_SHULKER_ATTACHED_POSITION = SynchedDataManager.createOldEntityData(EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public static short getLastScreenActionId() {
        return lastActionId;
    }

    public static short nextScreenActionId() {
        return ++lastActionId;
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0;
        }
        return super.getBlockDestroySpeed(state, destroySpeed);
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        if (block instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockExplosionResistance(block, resistance);
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Shulker.class && data == ShulkerAccessor.getDataPeekId()) {
            SynchedDataManager.registerOldEntityData(Shulker.class, OLD_SHULKER_ATTACHED_POSITION, Optional.empty(), (entity, pos) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getDataTicksFrozen()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
