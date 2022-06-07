package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.EndermanEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.LivingEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.TridentEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.WolfEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.TridentEntity;

public class Protocol_1_14_4 extends Protocol_1_15 {

    public static final TrackedData<Float> OLD_WOLF_HEALTH = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.FLOAT);

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == WolfEntity.class && data == WolfEntityAccessor.getBegging()) {
            DataTrackerManager.registerOldTrackedData(WolfEntity.class, OLD_WOLF_HEALTH, 20f, LivingEntity::setHealth);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getStingerCount())
            return false;
        if (clazz == TridentEntity.class && data == TridentEntityAccessor.getHasEnchantmentGlint())
            return false;
        if (clazz == EndermanEntity.class && data == EndermanEntityAccessor.getProvoked())
            return false;

        return super.acceptEntityData(clazz, data);
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        hardness = super.getBlockHardness(state, hardness);
        Block block = state.getBlock();
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            hardness = 0.8f;
        }
        return hardness;
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        resistance = super.getBlockResistance(block, resistance);
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            resistance = 0.8f;
        }
        return resistance;
    }
}
