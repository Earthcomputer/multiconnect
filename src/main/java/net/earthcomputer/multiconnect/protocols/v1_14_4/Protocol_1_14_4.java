package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.EndermanEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.LivingEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.TridentEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_14_4.mixin.WolfEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.earthcomputer.multiconnect.protocols.v1_15_2.Protocol_1_15_2;
import net.earthcomputer.multiconnect.protocols.generic.ChunkData;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.BitSet;
import java.util.List;

public class Protocol_1_14_4 extends Protocol_1_15 {

    public static final TrackedData<Float> OLD_WOLF_HEALTH = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.FLOAT);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            ChunkDataS2CPacket packet = ChunkDataTranslator.current().getPacket();
            if (!ChunkDataTranslator.current().isFullChunk())
                return;
            BitSet verticalStripBitmask = packet.getVerticalStripBitmask();
            buf.enablePassthroughMode();
            for (int sectionY = 0; sectionY < 16; sectionY++) {
                if (verticalStripBitmask.get(sectionY)) {
                    Protocol_1_15_2.skipChunkSection(buf);
                }
            }
            buf.disablePassthroughMode();

            Registry<Biome> biomeRegistry = ChunkDataTranslator.current().getRegistryManager().get(Registry.BIOME_KEY);
            Biome[] biomeData = new Biome[256];
            for (int i = 0; i < 256; i++) {
                int biomeId = buf.readInt();
                biomeData[i] = biomeRegistry.get(biomeId);
                if (biomeData[i] == null)
                    biomeData[i] = biomeRegistry.get(0); // Some servers send invalid biome IDs... for whatever reason
            }

            ChunkDataTranslator.current().setUserData("biomeData", biomeData);

            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // x
            buf.readInt(); // z
            boolean isFullChunk = buf.readBoolean();
            if (!isFullChunk) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readVarInt(); // vertical strip bitmask
            buf.readNbt(); // heightmaps
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
            PendingDataTrackerEntries.setEntries(entityId, DataTrackerManager.deserializePacket(buf));
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
            PendingDataTrackerEntries.setEntries(entityId, DataTrackerManager.deserializePacket(buf));
            buf.applyPendingReads();
        });
    }

    @Override
    public void postTranslateChunk(ChunkDataTranslator translator, ChunkData data) {
        if (translator.isFullChunk()) {
            Biome[] biomeData = (Biome[]) translator.getUserData("biomeData");
            ((IChunkDataS2CPacket) translator.getPacket()).set_1_14_4_biomeData(biomeData);
        }

        super.postTranslateChunk(translator, data);
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, PlayerActionResponseS2CPacket.class);
        insertAfter(packets, SynchronizeTagsS2CPacket.class, PacketInfo.of(PlayerActionResponseS2CPacket.class, PlayerActionResponseS2CPacket::new));
        return packets;
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_14_4, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_14_4, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_14_4, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_14_4, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
        mutator.mutate(Protocols.V1_14_4, Registry.BLOCK_ENTITY_TYPE, this::mutateBlockEntityTypeRegistry);
        mutator.mutate(Protocols.V1_14_4, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.BEE_NEST);
        registry.unregister(Blocks.BEEHIVE);
        registry.unregister(Blocks.HONEY_BLOCK);
        registry.unregister(Blocks.HONEYCOMB_BLOCK);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.HONEYCOMB);
        registry.unregister(Items.HONEY_BOTTLE);
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.BEE);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
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

    private void mutateBlockEntityTypeRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.BEEHIVE);
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.unregister(ParticleTypes.DRIPPING_HONEY);
        registry.unregister(ParticleTypes.FALLING_HONEY);
        registry.unregister(ParticleTypes.LANDING_HONEY);
        registry.unregister(ParticleTypes.FALLING_NECTAR);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() == Blocks.BELL && state.get(BellBlock.POWERED)) // powered
            return false;

        return super.acceptBlockState(state);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == LivingEntity.class && data == LivingEntityAccessor.getStingerCount())
            return false;
        if (clazz == TridentEntity.class && data == TridentEntityAccessor.getHasEnchantmentGlint())
            return false;
        if (clazz == WolfEntity.class && data == WolfEntityAccessor.getBegging())
            DataTrackerManager.registerOldTrackedData(WolfEntity.class, OLD_WOLF_HEALTH, 20f, LivingEntity::setHealth);
        if (clazz == EndermanEntity.class && data == EndermanEntityAccessor.getProvoked())
            return false;

        return super.acceptEntityData(clazz, data);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.TALL_FLOWERS, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.PEONY, Blocks.ROSE_BUSH);
        tags.addTag(BlockTags.FLOWERS, BlockTags.SMALL_FLOWERS);
        tags.addTag(BlockTags.FLOWERS, BlockTags.TALL_FLOWERS);
        tags.add(BlockTags.BEEHIVES);
        tags.add(BlockTags.CROPS, Blocks.BEETROOTS, Blocks.CARROTS, Blocks.POTATOES, Blocks.WHEAT, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
        tags.add(BlockTags.BEE_GROWABLES);
        tags.add(BlockTags.SHULKER_BOXES,
                Blocks.SHULKER_BOX,
                Blocks.BLACK_SHULKER_BOX,
                Blocks.BLUE_SHULKER_BOX,
                Blocks.BROWN_SHULKER_BOX,
                Blocks.CYAN_SHULKER_BOX,
                Blocks.GRAY_SHULKER_BOX,
                Blocks.GREEN_SHULKER_BOX,
                Blocks.LIGHT_BLUE_SHULKER_BOX,
                Blocks.LIGHT_GRAY_SHULKER_BOX,
                Blocks.LIME_SHULKER_BOX,
                Blocks.MAGENTA_SHULKER_BOX,
                Blocks.ORANGE_SHULKER_BOX,
                Blocks.PINK_SHULKER_BOX,
                Blocks.PURPLE_SHULKER_BOX,
                Blocks.RED_SHULKER_BOX,
                Blocks.WHITE_SHULKER_BOX,
                Blocks.YELLOW_SHULKER_BOX);
        tags.add(BlockTags.PORTALS, Blocks.NETHER_PORTAL, Blocks.END_PORTAL, Blocks.END_GATEWAY);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        copyBlocks(tags, blockTags, ItemTags.TALL_FLOWERS, BlockTags.TALL_FLOWERS);
        copyBlocks(tags, blockTags, ItemTags.FLOWERS, BlockTags.FLOWERS);
        tags.add(ItemTags.LECTERN_BOOKS, Items.WRITTEN_BOOK, Items.WRITABLE_BOOK);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.BEEHIVE_INHABITORS);
        tags.add(EntityTypeTags.ARROWS, EntityType.ARROW, EntityType.SPECTRAL_ARROW);
        super.addExtraEntityTags(tags);
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
