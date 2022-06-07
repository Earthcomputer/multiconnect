package net.earthcomputer.multiconnect.protocols.v1_16_5;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_5.mixin.ShulkerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class Protocol_1_16_5 extends Protocol_1_17 {
    public static final int BIOME_ARRAY_LENGTH = 1024;
    private static short lastActionId = 0;

    private static final TrackedData<Optional<BlockPos>> OLD_SHULKER_ATTACHED_POSITION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    public static final Key<Boolean> FULL_CHUNK_KEY = Key.create("fullChunk", true);

    public static short getLastScreenActionId() {
        return lastActionId;
    }

    public static short nextScreenActionId() {
        return ++lastActionId;
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        if (state.getBlock() instanceof InfestedBlock) {
            return 0;
        }
        return super.getBlockHardness(state, hardness);
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        if (block instanceof InfestedBlock) {
            return 0.75f;
        }
        return super.getBlockResistance(block, resistance);
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == ShulkerEntity.class && data == ShulkerEntityAccessor.getPeekAmount()) {
            DataTrackerManager.registerOldTrackedData(ShulkerEntity.class, OLD_SHULKER_ATTACHED_POSITION, Optional.empty(), (entity, pos) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getFrozenTicks()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
