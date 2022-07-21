package net.earthcomputer.multiconnect.protocols.v1_14;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_14.mixin.EnderManAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14.mixin.LivingEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14.mixin.ThrownTridentAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14.mixin.WolfAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.projectile.ThrownTrident;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class Protocol_1_14_4 extends Protocol_1_15 {

    public static final EntityDataAccessor<Float> OLD_WOLF_HEALTH = SynchedDataManager.createOldEntityData(EntityDataSerializers.FLOAT);

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == Wolf.class && data == WolfAccessor.getDataInterestedId()) {
            SynchedDataManager.registerOldEntityData(Wolf.class, OLD_WOLF_HEALTH, 20f, LivingEntity::setHealth);
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getDataStingerCountId())
            return false;
        if (clazz == ThrownTrident.class && data == ThrownTridentAccessor.getIdFoil())
            return false;
        if (clazz == EnderMan.class && data == EnderManAccessor.getDataStaredAt())
            return false;

        return super.acceptEntityData(clazz, data);
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        destroySpeed = super.getBlockDestroySpeed(state, destroySpeed);
        Block block = state.getBlock();
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            destroySpeed = 0.8f;
        }
        return destroySpeed;
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        resistance = super.getBlockExplosionResistance(block, resistance);
        if (block == Blocks.END_STONE_BRICKS || block == Blocks.END_STONE_BRICK_SLAB || block == Blocks.END_STONE_BRICK_STAIRS || block == Blocks.END_STONE_BRICK_WALL) {
            resistance = 0.8f;
        }
        return resistance;
    }
}
