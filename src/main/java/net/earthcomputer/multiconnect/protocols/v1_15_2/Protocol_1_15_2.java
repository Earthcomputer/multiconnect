package net.earthcomputer.multiconnect.protocols.v1_15_2;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.BlockConnections;
import net.earthcomputer.multiconnect.protocols.generic.blockconnections.connectors.SimpleInPlaceConnector;
import net.earthcomputer.multiconnect.protocols.v1_13_2.mixin.ProjectileEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.TameableEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.WolfEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.minecraft.block.*;
import net.minecraft.block.enums.WireConnection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.state.property.Properties;

import java.util.Optional;
import java.util.UUID;

public class Protocol_1_15_2 extends Protocol_1_16 {

    private static final TrackedData<Optional<UUID>> OLD_PROJECTILE_OWNER = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_UUID);
    private static final TrackedData<Byte> OLD_TAMEABLE_FLAGS = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BYTE);

    public static void registerConnectors() {
        BlockConnections.registerConnector(Protocols.V1_15_2, new SimpleInPlaceConnector(Blocks.REDSTONE_WIRE, (world, pos) -> {
            BlockState state = world.getBlockState(pos);
            boolean north = state.get(Properties.NORTH_WIRE_CONNECTION) != WireConnection.NONE;
            boolean south = state.get(Properties.SOUTH_WIRE_CONNECTION) != WireConnection.NONE;
            boolean west = state.get(Properties.WEST_WIRE_CONNECTION) != WireConnection.NONE;
            boolean east = state.get(Properties.EAST_WIRE_CONNECTION) != WireConnection.NONE;
            if (north && !south && !west && !east) state = state.with(Properties.SOUTH_WIRE_CONNECTION, WireConnection.SIDE);
            if (!north && south && !west && !east) state = state.with(Properties.NORTH_WIRE_CONNECTION, WireConnection.SIDE);
            if (!north && !south && west && !east) state = state.with(Properties.EAST_WIRE_CONNECTION, WireConnection.SIDE);
            if (!north && !south && !west && east) state = state.with(Properties.WEST_WIRE_CONNECTION, WireConnection.SIDE);
            world.setBlockState(pos, state);
        }));
    }

    @Override
    public void preAcceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PersistentProjectileEntity.class && data == ProjectileEntityAccessor.getPierceLevel()) {
            DataTrackerManager.registerOldTrackedData(PersistentProjectileEntity.class, OLD_PROJECTILE_OWNER, Optional.empty(), (entity, val) -> {});
        }
        super.preAcceptEntityData(clazz, data);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == TameableEntity.class && data == TameableEntityAccessor.getTameableFlags()) {
            DataTrackerManager.registerOldTrackedData(TameableEntity.class, OLD_TAMEABLE_FLAGS, (byte)0, (entity, val) -> {
                byte newVal = val;
                if (entity instanceof WolfEntity wolf) {
                    wolf.setAngerTime((newVal & 2) != 0 ? 400 : 0);
                    newVal = (byte) (newVal & ~2);
                }
                entity.getDataTracker().set(TameableEntityAccessor.getTameableFlags(), newVal);
            });
            return false;
        }
        if (clazz == WolfEntity.class && data == WolfEntityAccessor.getAngerTime()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public float getBlockHardness(BlockState state, float hardness) {
        hardness = super.getBlockHardness(state, hardness);
        if (state.getBlock() == Blocks.PISTON || state.getBlock() == Blocks.STICKY_PISTON || state.getBlock() == Blocks.PISTON_HEAD) {
            hardness = 0.5f;
        }
        return hardness;
    }

    @Override
    public float getBlockResistance(Block block, float resistance) {
        resistance = super.getBlockResistance(block, resistance);
        if (block == Blocks.PISTON || block == Blocks.STICKY_PISTON || block == Blocks.PISTON_HEAD) {
            resistance = 0.5f;
        }
        return resistance;
    }
}
