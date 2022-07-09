package net.earthcomputer.multiconnect.protocols.v1_15;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleInPlaceConnector;
import net.earthcomputer.multiconnect.protocols.v1_13.mixin.AbstractArrowAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.mixin.TamableAnimalAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.mixin.WolfAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.RedstoneSide;
import java.util.Optional;
import java.util.UUID;

public class Protocol_1_15_2 extends Protocol_1_16 {

    private static final EntityDataAccessor<Optional<UUID>> OLD_PROJECTILE_OWNER = SynchedDataManager.createOldEntityData(EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Byte> OLD_TAMEABLE_FLAGS = SynchedDataManager.createOldEntityData(EntityDataSerializers.BYTE);

    public static void registerConnectors() {
        BlockConnections.registerConnector(Protocols.V1_15_2, new SimpleInPlaceConnector(Blocks.REDSTONE_WIRE, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            boolean north = state.getValue(BlockStateProperties.NORTH_REDSTONE) != RedstoneSide.NONE;
            boolean south = state.getValue(BlockStateProperties.SOUTH_REDSTONE) != RedstoneSide.NONE;
            boolean west = state.getValue(BlockStateProperties.WEST_REDSTONE) != RedstoneSide.NONE;
            boolean east = state.getValue(BlockStateProperties.EAST_REDSTONE) != RedstoneSide.NONE;
            if (north && !south && !west && !east) state = state.setValue(BlockStateProperties.SOUTH_REDSTONE, RedstoneSide.SIDE);
            if (!north && south && !west && !east) state = state.setValue(BlockStateProperties.NORTH_REDSTONE, RedstoneSide.SIDE);
            if (!north && !south && west && !east) state = state.setValue(BlockStateProperties.EAST_REDSTONE, RedstoneSide.SIDE);
            if (!north && !south && !west && east) state = state.setValue(BlockStateProperties.WEST_REDSTONE, RedstoneSide.SIDE);
            world.setBlockState(pos, state);
        }));
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == AbstractArrow.class && data == AbstractArrowAccessor.getPierceLevel()) {
            SynchedDataManager.registerOldEntityData(AbstractArrow.class, OLD_PROJECTILE_OWNER, Optional.empty(), (entity, val) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, EntityDataAccessor<?> data) {
        if (clazz == TamableAnimal.class && data == TamableAnimalAccessor.getDataFlagsId()) {
            SynchedDataManager.registerOldEntityData(TamableAnimal.class, OLD_TAMEABLE_FLAGS, (byte)0, (entity, val) -> {
                byte newVal = val;
                if (entity instanceof Wolf wolf) {
                    wolf.setRemainingPersistentAngerTime((newVal & 2) != 0 ? 400 : 0);
                    newVal = (byte) (newVal & ~2);
                }
                entity.getEntityData().set(TamableAnimalAccessor.getDataFlagsId(), newVal);
            });
            return false;
        }
        if (clazz == Wolf.class && data == WolfAccessor.getDataRemainingAngerTime()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public float getBlockDestroySpeed(BlockState state, float destroySpeed) {
        destroySpeed = super.getBlockDestroySpeed(state, destroySpeed);
        if (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON || state.getBlock() == Blocks.PISTON_HEAD) {
            destroySpeed = 0.5f;
        }
        return destroySpeed;
    }

    @Override
    public float getBlockExplosionResistance(Block block, float resistance) {
        resistance = super.getBlockExplosionResistance(block, resistance);
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD) {
            resistance = 0.5f;
        }
        return resistance;
    }
}
