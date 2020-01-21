package net.earthcomputer.multiconnect.protocols.v1_14_4;

import io.netty.buffer.Unpooled;
import net.earthcomputer.multiconnect.impl.CurrentChunkDataPacket;
import net.earthcomputer.multiconnect.impl.DataTrackerManager;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.earthcomputer.multiconnect.transformer.ChunkData;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.packet.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.PacketByteBuf;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkSection;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

public class Protocol_1_14_4 extends Protocol_1_15 {

    private static final Field ENDERMAN_HAS_SCREAMED = DataTrackerManager.getTrackedDataField(EndermanEntity.class, 2, "field_20618");
    private static final Field LIVING_STINGER_COUNT = DataTrackerManager.getTrackedDataField(LivingEntity.class, 5, "STINGER_COUNT");
    private static final Field TRIDENT_HAS_ENCHANTMENT_GLINT = DataTrackerManager.getTrackedDataField(TridentEntity.class, 1, "field_21514");
    private static final Field WOLF_BEGGING = DataTrackerManager.getTrackedDataField(WolfEntity.class, 0, "BEGGING");

    private static final TrackedData<Float> OLD_WOLF_HEALTH = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.FLOAT);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            if (!CurrentChunkDataPacket.get().isFullChunk())
                return;
            int verticalStripBitmask = CurrentChunkDataPacket.get().getVerticalStripBitmask();
            buf.enablePassthroughMode();
            for (int sectionY = 0; sectionY < 16; sectionY++) {
                if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                    new ChunkSection(sectionY << 4).fromPacket(buf);
                }
            }
            buf.disablePassthroughMode();

            Biome[] biomeData = new Biome[256];
            for (int i = 0; i < 256; i++) {
                int biomeId = buf.readInt();
                biomeData[i] = Registry.BIOME.get(biomeId);
                if (biomeData[i] == null)
                    throw new RuntimeException("Received invalid biome id: " + biomeId);
            }

            PendingBiomeData.setPendingBiomeData(CurrentChunkDataPacket.get().getX(), CurrentChunkDataPacket.get().getZ(), biomeData);

            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt();
            buf.readInt();
            boolean isFullChunk = buf.readBoolean();
            if (!isFullChunk) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readVarInt();
            buf.readCompoundTag(); // heightmaps
            buf.disablePassthroughMode();
            for (int i = 0; i < 1024; i++)
                buf.pendingRead(Integer.class, 0);
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // player id
            buf.readUnsignedByte(); // game mode
            buf.readInt(); // dimension
            buf.pendingRead(Long.class, 0L); // seed
            buf.readUnsignedByte(); // max players
            buf.readString(16); // generator type
            buf.readVarInt(); // render distance
            buf.readBoolean(); // reduced debug info
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, true); // show death screen
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(MobSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int entityId = buf.readVarInt();
            buf.readUuid();
            buf.readVarInt();
            buf.readDouble();
            buf.readDouble();
            buf.readDouble();
            buf.readByte();
            buf.readByte();
            buf.readByte();
            buf.readShort();
            buf.readShort();
            buf.readShort();
            buf.disablePassthroughMode();
            try {
                PendingDataTrackerEntries.setEntries(entityId, DataTracker.deserializePacket(buf));
            } catch (IOException e) {
                // I don't even know why it's declared to throw an IOException
                throw new AssertionError(e);
            }
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(ParticleS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // type
            buf.readBoolean(); // long distance
            buf.disablePassthroughMode();
            double x = buf.readFloat();
            double y = buf.readFloat();
            double z = buf.readFloat();
            buf.pendingRead(Double.class, x);
            buf.pendingRead(Double.class, y);
            buf.pendingRead(Double.class, z);
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(PlayerRespawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // dimension
            buf.disablePassthroughMode();
            buf.pendingRead(Long.class, 0L); // seed
            buf.applyPendingReads();
        });

        ProtocolRegistry.registerInboundTranslator(PlayerSpawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int entityId = buf.readVarInt();
            buf.readUuid();
            buf.readDouble();
            buf.readDouble();
            buf.readDouble();
            buf.readByte();
            buf.readByte();
            buf.disablePassthroughMode();
            try {
                PendingDataTrackerEntries.setEntries(entityId, DataTracker.deserializePacket(buf));
            } catch (IOException e) {
                // I don't even know why it's declared to throw an IOException
                throw new AssertionError(e);
            }
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, PlayerActionResponseS2CPacket.class);
        insertAfter(packets, SynchronizeTagsS2CPacket.class, PacketInfo.of(PlayerActionResponseS2CPacket.class, PlayerActionResponseS2CPacket::new));
        return packets;
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        super.modifyRegistry(registry);

        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            modifyItemRegistry((ISimpleRegistry<Item>) registry);
        } else if (registry == Registry.ENTITY_TYPE) {
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.SOUND_EVENT) {
            modifySoundEventRegistry((ISimpleRegistry<SoundEvent>) registry);
        } else if (registry == Registry.BLOCK_ENTITY_TYPE) {
            modifyBlockEntityTypeRegistry((ISimpleRegistry<BlockEntityType<?>>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            modifyParticleTypeRegistry((ISimpleRegistry<ParticleType<? extends ParticleEffect>>) registry);
        }
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.BEE_NEST);
        registry.unregister(Blocks.BEEHIVE);
        registry.unregister(Blocks.HONEY_BLOCK);
        registry.unregister(Blocks.HONEYCOMB_BLOCK);
    }

    private void modifyItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.HONEYCOMB);
        registry.unregister(Items.HONEY_BOTTLE);
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.BEE);
    }

    private void modifySoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ENTITY_BEE_DEATH);
        registry.unregister(SoundEvents.ENTITY_BEE_HURT);
        registry.unregister(SoundEvents.ENTITY_BEE_LOOP_AGGRESSIVE);
        registry.unregister(SoundEvents.ENTITY_BEE_LOOP);
        registry.unregister(SoundEvents.ENTITY_BEE_STING);
        registry.unregister(SoundEvents.ENTITY_BEE_POLLINATE);
        registry.unregister(SoundEvents.BLOCK_BEEHIVE_DRIP);
        registry.unregister(SoundEvents.BLOCK_BEEHIVE_ENTER);
        registry.unregister(SoundEvents.BLOCK_BEEHIVE_EXIT);
        registry.unregister(SoundEvents.BLOCK_BEEHIVE_SHEAR);
        registry.unregister(SoundEvents.BLOCK_BEEHIVE_WORK);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_SLIDE);
        registry.unregister(SoundEvents.BLOCK_HONEY_BLOCK_STEP);
        registry.unregister(SoundEvents.ITEM_HONEY_BOTTLE_DRINK);
        registry.unregister(SoundEvents.ENTITY_IRON_GOLEM_DAMAGE);
        registry.unregister(SoundEvents.ENTITY_IRON_GOLEM_REPAIR);

        insertAfter(registry, SoundEvents.ENTITY_PARROT_IMITATE_ENDER_DRAGON, SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_ENDERMAN, "entity.parrot.imitate.enderman");
        insertAfter(registry, SoundEvents.ENTITY_PARROT_IMITATE_MAGMA_CUBE, SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_PANDA, "entity.parrot.imitate.panda");
        insertAfter(registry, SoundEvents.ENTITY_PARROT_IMITATE_PILLAGER, SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_POLAR_BEAR, "entity.parrot.imitate.polar_bear");
        insertAfter(registry, SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON, SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_WOLF, "entity.parrot.imitate.wolf");
        insertAfter(registry, SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE, SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN, "entity.parrot.imitate.zombie_pigman");
    }

    private void modifyBlockEntityTypeRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.BEEHIVE);
    }

    private void modifyParticleTypeRegistry(ISimpleRegistry<ParticleType<? extends ParticleEffect>> registry) {
        registry.unregister(ParticleTypes.DRIPPING_HONEY);
        registry.unregister(ParticleTypes.FALLING_HONEY);
        registry.unregister(ParticleTypes.LANDING_HONEY);
        registry.unregister(ParticleTypes.FALLING_NECTAR);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() == Blocks.BELL && state.get(BellBlock.field_20648)) // powered
            return false;

        return super.acceptBlockState(state);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == LivingEntity.class && data == DataTrackerManager.getTrackedData(Integer.class, LIVING_STINGER_COUNT))
            return false;
        if (clazz == TridentEntity.class && data == DataTrackerManager.getTrackedData(Boolean.class, TRIDENT_HAS_ENCHANTMENT_GLINT))
            return false;
        if (clazz == WolfEntity.class && data == DataTrackerManager.getTrackedData(Boolean.class, WOLF_BEGGING))
            DataTrackerManager.registerOldTrackedData(WolfEntity.class, OLD_WOLF_HEALTH, 20f, LivingEntity::setHealth);
        if (clazz == EndermanEntity.class && data == DataTrackerManager.getTrackedData(Boolean.class, ENDERMAN_HAS_SCREAMED))
            return false;

        return super.acceptEntityData(clazz, data);
    }
}
