package net.earthcomputer.multiconnect.protocols.v1_15_2;

import com.google.common.collect.ImmutableSet;
import net.earthcomputer.multiconnect.impl.CurrentChunkDataPacket;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_15_2.mixin.RenameItemStackAttributesFixAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16;
import net.earthcomputer.multiconnect.transformer.ChunkData;
import net.minecraft.block.*;
import net.minecraft.block.enums.JigsawOrientation;
import net.minecraft.block.enums.WallShape;
import net.minecraft.class_5196;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.JigsawGeneratingC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateJigsawC2SPacket;
import net.minecraft.network.packet.s2c.login.LoginSuccessS2CPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.EntityTypeTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.DynamicSerializableUuid;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Protocol_1_15_2 extends Protocol_1_16 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(ChunkData.class, buf -> {
            int verticalStripBitmask = CurrentChunkDataPacket.get().getVerticalStripBitmask();
            buf.enablePassthroughMode();
            for (int sectionY = 0; sectionY < 16; sectionY++) {
                if ((verticalStripBitmask & (1 << sectionY)) != 0) {
                    buf.readShort(); // non-empty block count
                    int paletteSize = ChunkData.skipPalette(buf);
                    // translate from packed chunk data to aligned
                    if (paletteSize == 0 || MathHelper.isPowerOfTwo(paletteSize)) {
                        // shortcut, for powers of 2, elements are already aligned
                        buf.readLongArray(new long[paletteSize * 64]);
                    } else {
                        buf.disablePassthroughMode();
                        long[] oldData = buf.readLongArray(new long[paletteSize * 64]);
                        buf.pendingRead(long[].class, class_5196.method_27288(4096, paletteSize, oldData));
                        buf.enablePassthroughMode();
                    }
                }
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // x
            buf.readInt(); // z
            buf.readBoolean(); // full chunk
            buf.readVarInt(); // vertical strip bitmask
            buf.disablePassthroughMode();
            CompoundTag heightmaps = buf.readCompoundTag();
            if (heightmaps != null) {
                for (String key : heightmaps.getKeys()) {
                    net.minecraft.nbt.Tag tag = heightmaps.get(key);
                    if (tag instanceof LongArrayTag) {
                        heightmaps.putLongArray(key, class_5196.method_27288(256, 9, ((LongArrayTag) tag).getLongArray()));
                    }
                }
            }
            buf.pendingRead(CompoundTag.class, heightmaps);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(LoginSuccessS2CPacket.class, buf -> {
            UUID uuid = UUID.fromString(buf.readString(36));
            int[] uuidArray = DynamicSerializableUuid.method_26275(uuid);
            buf.pendingRead(Integer.class, uuidArray[0]);
            buf.pendingRead(Integer.class, uuidArray[1]);
            buf.pendingRead(Integer.class, uuidArray[2]);
            buf.pendingRead(Integer.class, uuidArray[3]);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityAttributesS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            int count = buf.readInt();
            buf.disablePassthroughMode();
            for (int i = 0; i < count; i++) {
                String oldId = buf.readString(64);
                String newId = RenameItemStackAttributesFixAccessor.getRenames().getOrDefault(oldId, oldId).toLowerCase();
                buf.pendingRead(Identifier.class, new Identifier(newId));
                buf.enablePassthroughMode();
                buf.readDouble(); // base value
                int modifierCount = buf.readVarInt();
                for (int j = 0; j < modifierCount; j++) {
                    buf.readUuid(); // uuid
                    buf.readDouble(); // value
                    buf.readByte(); // type
                }
                buf.disablePassthroughMode();
            }
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerRespawnS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // dimension id
            buf.readLong(); // sha256 seed
            buf.readUnsignedByte(); // game mode
            buf.readString(16); // generator type
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, Boolean.TRUE); // keep attributes
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerOutboundTranslator(UpdateJigsawC2SPacket.class, buf -> {
            buf.passthroughWrite(BlockPos.class); // pos
            buf.skipWrite(Identifier.class); // name
            buf.passthroughWrite(Identifier.class); // target
            buf.passthroughWrite(Identifier.class); // pool
            buf.passthroughWrite(String.class); // final state
            buf.skipWrite(String.class); // joint type
        });
    }

    public static void skipChunkSection(PacketByteBuf buf) {
        buf.readShort(); // non-empty block count
        skipPalettedContainer(buf);
    }

    public static void skipPalettedContainer(PacketByteBuf buf) {
        int paletteSize = ChunkData.skipPalette(buf);
        buf.readLongArray(new long[paletteSize * 64]); // chunk data
    }

    @SuppressWarnings({"EqualsBetweenInconvertibleTypes", "unchecked"})
    @Override
    public void modifyRegistry(ISimpleRegistry<?> registry) {
        if (registry == Registry.BLOCK) {
            modifyBlockRegistry((ISimpleRegistry<Block>) registry);
        } else if (registry == Registry.ITEM) {
            modifyItemRegistry((ISimpleRegistry<Item>) registry);
        }  else if (registry == Registry.ENTITY_TYPE) {
            modifyEntityTypeRegistry((ISimpleRegistry<EntityType<?>>) registry);
        } else if (registry == Registry.BIOME) {
            modifyBiomeRegistry((ISimpleRegistry<Biome>) registry);
        } else if (registry == Registry.PARTICLE_TYPE) {
            modifyParticleTypeRegistry((ISimpleRegistry<ParticleType<? extends ParticleEffect>>) registry);
        } else if (registry == Registry.SOUND_EVENT) {
            modifySoundEventRegistry((ISimpleRegistry<SoundEvent>) registry);
        }
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, PlayerSpawnPositionS2CPacket.class);
        insertAfter(packets, ScoreboardPlayerUpdateS2CPacket.class, PacketInfo.of(PlayerSpawnPositionS2CPacket.class, PlayerSpawnPositionS2CPacket::new));
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, JigsawGeneratingC2SPacket.class);
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof JigsawGeneratingC2SPacket) {
            return false;
        }
        return super.onSendPacket(packet);
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.ANCIENT_DEBRIS);
        registry.unregister(Blocks.BASALT);
        registry.unregister(Blocks.CRIMSON_BUTTON);
        registry.unregister(Blocks.WARPED_BUTTON);
        registry.unregister(Blocks.CRIMSON_DOOR);
        registry.unregister(Blocks.WARPED_DOOR);
        registry.unregister(Blocks.CRIMSON_FENCE);
        registry.unregister(Blocks.WARPED_FENCE);
        registry.unregister(Blocks.CRIMSON_FENCE_GATE);
        registry.unregister(Blocks.WARPED_FENCE_GATE);
        registry.unregister(Blocks.CRIMSON_FUNGUS);
        registry.unregister(Blocks.WARPED_FUNGUS);
        registry.unregister(Blocks.NETHER_SPROUTS);
        registry.unregister(Blocks.CRIMSON_NYLIUM);
        registry.unregister(Blocks.WARPED_NYLIUM);
        registry.unregister(Blocks.CRIMSON_PLANKS);
        registry.unregister(Blocks.WARPED_PLANKS);
        registry.unregister(Blocks.CRIMSON_PRESSURE_PLATE);
        registry.unregister(Blocks.WARPED_PRESSURE_PLATE);
        registry.unregister(Blocks.CRIMSON_ROOTS);
        registry.unregister(Blocks.WARPED_ROOTS);
        registry.unregister(Blocks.CRIMSON_SIGN);
        registry.unregister(Blocks.WARPED_SIGN);
        registry.unregister(Blocks.CRIMSON_SLAB);
        registry.unregister(Blocks.WARPED_SLAB);
        registry.unregister(Blocks.CRIMSON_STAIRS);
        registry.unregister(Blocks.WARPED_STAIRS);
        registry.unregister(Blocks.CRIMSON_STEM);
        registry.unregister(Blocks.WARPED_STEM);
        registry.unregister(Blocks.STRIPPED_CRIMSON_STEM);
        registry.unregister(Blocks.STRIPPED_WARPED_STEM);
        registry.unregister(Blocks.CRIMSON_TRAPDOOR);
        registry.unregister(Blocks.WARPED_TRAPDOOR);
        registry.unregister(Blocks.CRIMSON_WALL_SIGN);
        registry.unregister(Blocks.WARPED_WALL_SIGN);
        registry.unregister(Blocks.NETHERITE_BLOCK);
        registry.unregister(Blocks.SHROOMLIGHT);
        registry.unregister(Blocks.SOUL_FIRE);
        registry.unregister(Blocks.SOUL_LANTERN);
        registry.unregister(Blocks.SOUL_TORCH);
        registry.unregister(Blocks.SOUL_WALL_TORCH);
        registry.unregister(Blocks.SOUL_SOIL);
        registry.unregister(Blocks.WARPED_WART_BLOCK);
        registry.unregister(Blocks.WEEPING_VINES);
        registry.unregister(Blocks.WEEPING_VINES_PLANT);
        registry.unregister(Blocks.TARGET);
        registry.unregister(Blocks.CRYING_OBSIDIAN);
        registry.unregister(Blocks.POTTED_CRIMSON_FUNGUS);
        registry.unregister(Blocks.POTTED_WARPED_FUNGUS);
        registry.unregister(Blocks.POTTED_CRIMSON_ROOTS);
        registry.unregister(Blocks.POTTED_WARPED_ROOTS);
        registry.unregister(Blocks.WARPED_HYPHAE);
        registry.unregister(Blocks.STRIPPED_WARPED_HYPHAE);
        registry.unregister(Blocks.CRIMSON_HYPHAE);
        registry.unregister(Blocks.STRIPPED_CRIMSON_HYPHAE);
        registry.unregister(Blocks.NETHER_GOLD_ORE);
        registry.unregister(Blocks.TWISTING_VINES);
        registry.unregister(Blocks.TWISTING_VINES_PLANT);
        registry.unregister(Blocks.POLISHED_BASALT);
        registry.unregister(Blocks.RESPAWN_ANCHOR);
        registry.unregister(Blocks.LODESTONE);
        registry.unregister(Blocks.CHAIN);
        registry.unregister(Blocks.SOUL_CAMPFIRE);
        registry.unregister(Blocks.BLACKSTONE);
        registry.unregister(Blocks.BLACKSTONE_STAIRS);
        registry.unregister(Blocks.BLACKSTONE_WALL);
        registry.unregister(Blocks.BLACKSTONE_SLAB);
        registry.unregister(Blocks.POLISHED_BLACKSTONE);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_BRICKS);
        registry.unregister(Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS);
        registry.unregister(Blocks.CHISELED_POLISHED_BLACKSTONE);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_BRICK_SLAB);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_BRICK_STAIRS);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_BRICK_WALL);
        registry.unregister(Blocks.GILDED_BLACKSTONE);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_STAIRS);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_SLAB);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_BUTTON);
        registry.unregister(Blocks.POLISHED_BLACKSTONE_WALL);
        registry.unregister(Blocks.CHISELED_NETHER_BRICKS);
        registry.unregister(Blocks.CRACKED_NETHER_BRICKS);
        registry.unregister(Blocks.QUARTZ_BRICKS);
    }

    private void modifyItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.COMPOSTER);
        rename(registry, Items.ZOMBIFIED_PIGLIN_SPAWN_EGG, "zombie_pigman_spawn_egg");
        insertAfter(registry, Items.JIGSAW, Items.COMPOSTER, "composter");
        registry.unregister(Items.CRIMSON_NYLIUM);
        registry.unregister(Items.WARPED_NYLIUM);
        registry.unregister(Items.CRIMSON_PLANKS);
        registry.unregister(Items.WARPED_PLANKS);
        registry.unregister(Items.CRIMSON_STEM);
        registry.unregister(Items.WARPED_STEM);
        registry.unregister(Items.STRIPPED_CRIMSON_STEM);
        registry.unregister(Items.STRIPPED_WARPED_STEM);
        registry.unregister(Items.CRIMSON_FUNGUS);
        registry.unregister(Items.WARPED_FUNGUS);
        registry.unregister(Items.CRIMSON_ROOTS);
        registry.unregister(Items.WARPED_ROOTS);
        registry.unregister(Items.NETHER_SPROUTS);
        registry.unregister(Items.WEEPING_VINES);
        registry.unregister(Items.CRIMSON_SLAB);
        registry.unregister(Items.WARPED_SLAB);
        registry.unregister(Items.CRIMSON_PRESSURE_PLATE);
        registry.unregister(Items.WARPED_PRESSURE_PLATE);
        registry.unregister(Items.CRIMSON_FENCE);
        registry.unregister(Items.WARPED_FENCE);
        registry.unregister(Items.SOUL_SOIL);
        registry.unregister(Items.BASALT);
        registry.unregister(Items.SOUL_TORCH);
        registry.unregister(Items.CRIMSON_TRAPDOOR);
        registry.unregister(Items.WARPED_TRAPDOOR);
        registry.unregister(Items.CRIMSON_FENCE_GATE);
        registry.unregister(Items.WARPED_FENCE_GATE);
        registry.unregister(Items.CRIMSON_STAIRS);
        registry.unregister(Items.WARPED_STAIRS);
        registry.unregister(Items.CRIMSON_BUTTON);
        registry.unregister(Items.WARPED_BUTTON);
        registry.unregister(Items.WARPED_WART_BLOCK);
        registry.unregister(Items.CRIMSON_DOOR);
        registry.unregister(Items.WARPED_DOOR);
        registry.unregister(Items.SOUL_LANTERN);
        registry.unregister(Items.SHROOMLIGHT);
        registry.unregister(Items.NETHERITE_BLOCK);
        registry.unregister(Items.ANCIENT_DEBRIS);
        registry.unregister(Items.HOGLIN_SPAWN_EGG);
        registry.unregister(Items.PIGLIN_SPAWN_EGG);
        registry.unregister(Items.CRIMSON_SIGN);
        registry.unregister(Items.WARPED_SIGN);
        registry.unregister(Items.NETHERITE_SCRAP);
        registry.unregister(Items.NETHERITE_INGOT);
        registry.unregister(Items.NETHERITE_HELMET);
        registry.unregister(Items.NETHERITE_CHESTPLATE);
        registry.unregister(Items.NETHERITE_LEGGINGS);
        registry.unregister(Items.NETHERITE_BOOTS);
        registry.unregister(Items.NETHERITE_SWORD);
        registry.unregister(Items.NETHERITE_SHOVEL);
        registry.unregister(Items.NETHERITE_AXE);
        registry.unregister(Items.NETHERITE_PICKAXE);
        registry.unregister(Items.NETHERITE_HOE);
        registry.unregister(Items.TARGET);
        registry.unregister(Items.CRYING_OBSIDIAN);
        registry.unregister(Items.RESPAWN_ANCHOR);
        registry.unregister(Items.LODESTONE);
        registry.unregister(Items.WARPED_FUNGUS_ON_A_STICK);
        registry.unregister(Items.STRIDER_SPAWN_EGG);
        registry.unregister(Items.STRIPPED_CRIMSON_HYPHAE);
        registry.unregister(Items.STRIPPED_WARPED_HYPHAE);
        registry.unregister(Items.CRIMSON_HYPHAE);
        registry.unregister(Items.WARPED_HYPHAE);
        registry.unregister(Items.POLISHED_BASALT);
        registry.unregister(Items.NETHER_GOLD_ORE);
        registry.unregister(Items.TWISTING_VINES);
        registry.unregister(Items.ZOGLIN_SPAWN_EGG);
        registry.unregister(Items.CHAIN);
        registry.unregister(Items.CRACKED_NETHER_BRICKS);
        registry.unregister(Items.CHISELED_NETHER_BRICKS);
        registry.unregister(Items.QUARTZ_BRICKS);
        registry.unregister(Items.MUSIC_DISC_PIGSTEP);
        registry.unregister(Items.PIGLIN_BANNER_PATTERN);
        registry.unregister(Items.SOUL_CAMPFIRE);
        registry.unregister(Items.BLACKSTONE);
        registry.unregister(Items.BLACKSTONE_SLAB);
        registry.unregister(Items.BLACKSTONE_STAIRS);
        registry.unregister(Items.BLACKSTONE_WALL);
        registry.unregister(Items.GILDED_BLACKSTONE);
        registry.unregister(Items.POLISHED_BLACKSTONE);
        registry.unregister(Items.POLISHED_BLACKSTONE_SLAB);
        registry.unregister(Items.POLISHED_BLACKSTONE_STAIRS);
        registry.unregister(Items.POLISHED_BLACKSTONE_WALL);
        registry.unregister(Items.POLISHED_BLACKSTONE_BUTTON);
        registry.unregister(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        registry.unregister(Items.CHISELED_POLISHED_BLACKSTONE);
        registry.unregister(Items.POLISHED_BLACKSTONE_BRICKS);
        registry.unregister(Items.POLISHED_BLACKSTONE_BRICK_SLAB);
        registry.unregister(Items.POLISHED_BLACKSTONE_BRICK_STAIRS);
        registry.unregister(Items.POLISHED_BLACKSTONE_BRICK_WALL);
        registry.unregister(Items.CRACKED_POLISHED_BLACKSTONE_BRICKS);
    }

    private void modifyEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.DONKEY);
        insertAfter(registry, EntityType.CREEPER, EntityType.DONKEY, "donkey");
        registry.unregister(EntityType.EVOKER);
        insertAfter(registry, EntityType.EVOKER_FANGS, EntityType.EVOKER, "evoker");
        registry.unregister(EntityType.IRON_GOLEM);
        insertAfter(registry, EntityType.VILLAGER, EntityType.IRON_GOLEM, "iron_golem");
        registry.unregister(EntityType.PHANTOM);
        insertAfter(registry, EntityType.ZOMBIE_VILLAGER, EntityType.PHANTOM, "phantom");
        registry.unregister(EntityType.PILLAGER);
        insertAfter(registry, EntityType.VINDICATOR, EntityType.PILLAGER, "pillager");
        registry.unregister(EntityType.PUFFERFISH);
        insertAfter(registry, EntityType.PIG, EntityType.PUFFERFISH, "pufferfish");
        registry.unregister(EntityType.RAVAGER);
        insertAfter(registry, EntityType.PHANTOM, EntityType.RAVAGER, "ravager");
        registry.unregister(EntityType.TRADER_LLAMA);
        registry.unregister(EntityType.TROPICAL_FISH);
        registry.unregister(EntityType.TURTLE);
        insertAfter(registry, EntityType.STRAY, EntityType.TRADER_LLAMA, "trader_llama");
        insertAfter(registry, EntityType.TRADER_LLAMA, EntityType.TROPICAL_FISH, "tropical_fish");
        insertAfter(registry, EntityType.TROPICAL_FISH, EntityType.TURTLE, "turtle");
        registry.unregister(EntityType.ZOMBIFIED_PIGLIN);
        insertAfter(registry, EntityType.PUFFERFISH, EntityType.ZOMBIFIED_PIGLIN, "zombie_pigman");

        registry.unregister(EntityType.HOGLIN);
        registry.unregister(EntityType.PIGLIN);
        registry.unregister(EntityType.STRIDER);
        registry.unregister(EntityType.ZOGLIN);
    }

    private static void modifyBiomeRegistry(ISimpleRegistry<Biome> registry) {
        rename(registry, Biomes.NETHER_WASTES, "nether");
        registry.unregister(Biomes.SOUL_SAND_VALLEY);
        registry.unregister(Biomes.CRIMSON_FOREST);
        registry.unregister(Biomes.WARPED_FOREST);
        registry.unregister(Biomes.BASALT_DELTAS);
    }

    private void modifyParticleTypeRegistry(ISimpleRegistry<ParticleType<? extends ParticleEffect>> registry) {
        registry.unregister(ParticleTypes.ASH);
        registry.unregister(ParticleTypes.CRIMSON_SPORE);
        registry.unregister(ParticleTypes.SOUL_FIRE_FLAME);
        registry.unregister(ParticleTypes.SOUL);
        registry.unregister(ParticleTypes.WARPED_SPORE);
        registry.unregister(ParticleTypes.DRIPPING_OBSIDIAN_TEAR);
        registry.unregister(ParticleTypes.FALLING_OBSIDIAN_TEAR);
        registry.unregister(ParticleTypes.LANDING_OBSIDIAN_TEAR);
        registry.unregister(ParticleTypes.REVERSE_PORTAL);
        registry.unregister(ParticleTypes.WHITE_ASH);
    }

    private void modifySoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE);
        insertAfter(registry, SoundEvents.ITEM_BOOK_PUT, SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, "entity.fishing_bobber.retrieve");
        registry.unregister(SoundEvents.ENTITY_FISHING_BOBBER_SPLASH);
        insertAfter(registry, SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, "entity.fishing_bobber.splash");
        registry.unregister(SoundEvents.ENTITY_FISHING_BOBBER_THROW);
        insertAfter(registry, SoundEvents.ENTITY_FISHING_BOBBER_SPLASH, SoundEvents.ENTITY_FISHING_BOBBER_THROW, "entity.fishing_bobber.throw");

        registry.unregister(SoundEvents.BLOCK_WOOL_BREAK);
        insertAfter(registry, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundEvents.BLOCK_WOOL_BREAK, "block.wool.break");
        registry.unregister(SoundEvents.BLOCK_WOOL_FALL);
        insertAfter(registry, SoundEvents.BLOCK_WOOL_BREAK, SoundEvents.BLOCK_WOOL_FALL, "block.wool.fall");
        registry.unregister(SoundEvents.BLOCK_WOOL_HIT);
        insertAfter(registry, SoundEvents.BLOCK_WOOL_FALL, SoundEvents.BLOCK_WOOL_HIT, "block.wool.hit");
        registry.unregister(SoundEvents.BLOCK_WOOL_PLACE);
        insertAfter(registry, SoundEvents.BLOCK_WOOL_HIT, SoundEvents.BLOCK_WOOL_PLACE, "block.wool.place");
        registry.unregister(SoundEvents.BLOCK_WOOL_STEP);
        insertAfter(registry, SoundEvents.BLOCK_WOOL_PLACE, SoundEvents.BLOCK_WOOL_STEP, "block.wool.step");

        registry.unregister(SoundEvents.BLOCK_WET_GRASS_BREAK);
        insertAfter(registry, SoundEvents.BLOCK_GRASS_STEP, SoundEvents.BLOCK_WET_GRASS_BREAK, "block.wet_grass.break");
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_FALL);
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_BREAK, SoundEvents.BLOCK_WET_GRASS_FALL, "block.wet_grass.fall");
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_HIT);
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_FALL, SoundEvents.BLOCK_WET_GRASS_HIT, "block.wet_grass.hit");
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_PLACE);
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_HIT, SoundEvents.BLOCK_WET_GRASS_PLACE, "block.wet_grass.place");
        registry.unregister(SoundEvents.BLOCK_WET_GRASS_STEP);
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_PLACE, SoundEvents.BLOCK_WET_GRASS_STEP, "block.wet_grass.step");

        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_BREAK);
        insertAfter(registry, SoundEvents.BLOCK_WET_GRASS_STEP, SoundEvents.BLOCK_CORAL_BLOCK_BREAK, "block.coral_block.break");
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_FALL);
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_BREAK, SoundEvents.BLOCK_CORAL_BLOCK_FALL, "block.coral_block.fall");
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_HIT);
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_FALL, SoundEvents.BLOCK_CORAL_BLOCK_HIT, "block.coral_block.hit");
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_PLACE);
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_HIT, SoundEvents.BLOCK_CORAL_BLOCK_PLACE, "block.coral_block.place");
        registry.unregister(SoundEvents.BLOCK_CORAL_BLOCK_STEP);
        insertAfter(registry, SoundEvents.BLOCK_CORAL_BLOCK_PLACE, SoundEvents.BLOCK_CORAL_BLOCK_STEP, "block.coral_block.step");

        registry.unregister(SoundEvents.ENTITY_RAVAGER_AMBIENT);
        insertAfter(registry, SoundEvents.ENTITY_HUSK_STEP, SoundEvents.ENTITY_RAVAGER_AMBIENT, "entity.ravager.ambient");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_ATTACK);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_AMBIENT, SoundEvents.ENTITY_RAVAGER_ATTACK, "entity.ravager.attack");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_CELEBRATE);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_ATTACK, SoundEvents.ENTITY_RAVAGER_CELEBRATE, "entity.ravager.celebrate");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_DEATH);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_CELEBRATE, SoundEvents.ENTITY_RAVAGER_DEATH, "entity.ravager.death");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_HURT);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_DEATH, SoundEvents.ENTITY_RAVAGER_HURT, "entity.ravager.hurt");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_STEP);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_HURT, SoundEvents.ENTITY_RAVAGER_STEP, "entity.ravager.step");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_STUNNED);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_STEP, SoundEvents.ENTITY_RAVAGER_STUNNED, "entity.ravager.stunned");
        registry.unregister(SoundEvents.ENTITY_RAVAGER_ROAR);
        insertAfter(registry, SoundEvents.ENTITY_RAVAGER_STUNNED, SoundEvents.ENTITY_RAVAGER_ROAR, "entity.ravager.roar");

        registry.unregister(SoundEvents.ENTITY_MAGMA_CUBE_DEATH_SMALL);
        insertAfter(registry, SoundEvents.BLOCK_SLIME_BLOCK_STEP, SoundEvents.ENTITY_MAGMA_CUBE_DEATH_SMALL, "entity.magma_cube.death_small");
        registry.unregister(SoundEvents.ENTITY_MAGMA_CUBE_HURT_SMALL);
        insertAfter(registry, SoundEvents.ENTITY_MAGMA_CUBE_DEATH_SMALL, SoundEvents.ENTITY_MAGMA_CUBE_HURT_SMALL, "entity.magma_cube.hurt_small");
        registry.unregister(SoundEvents.ENTITY_MAGMA_CUBE_SQUISH_SMALL);
        insertAfter(registry, SoundEvents.ENTITY_MAGMA_CUBE_HURT_SMALL, SoundEvents.ENTITY_MAGMA_CUBE_SQUISH_SMALL, "entity.magma_cube.squish_small");

        registry.unregister(SoundEvents.MUSIC_DISC_11);
        insertAfter(registry, SoundEvents.EVENT_RAID_HORN, SoundEvents.MUSIC_DISC_11, "music_disc.11");
        registry.unregister(SoundEvents.MUSIC_DISC_13);
        insertAfter(registry, SoundEvents.MUSIC_DISC_11, SoundEvents.MUSIC_DISC_13, "music_disc.13");
        registry.unregister(SoundEvents.MUSIC_DISC_BLOCKS);
        insertAfter(registry, SoundEvents.MUSIC_DISC_13, SoundEvents.MUSIC_DISC_BLOCKS, "music_disc.blocks");
        registry.unregister(SoundEvents.MUSIC_DISC_CAT);
        insertAfter(registry, SoundEvents.MUSIC_DISC_BLOCKS, SoundEvents.MUSIC_DISC_CAT, "music_disc.cat");
        registry.unregister(SoundEvents.MUSIC_DISC_CHIRP);
        insertAfter(registry, SoundEvents.MUSIC_DISC_CAT, SoundEvents.MUSIC_DISC_CHIRP, "music_disc.chirp");
        registry.unregister(SoundEvents.MUSIC_DISC_FAR);
        insertAfter(registry, SoundEvents.MUSIC_DISC_CHIRP, SoundEvents.MUSIC_DISC_FAR, "music_disc.far");
        registry.unregister(SoundEvents.MUSIC_DISC_MALL);
        insertAfter(registry, SoundEvents.MUSIC_DISC_FAR, SoundEvents.MUSIC_DISC_MALL, "music_disc.mall");
        registry.unregister(SoundEvents.MUSIC_DISC_MELLOHI);
        insertAfter(registry, SoundEvents.MUSIC_DISC_MALL, SoundEvents.MUSIC_DISC_MELLOHI, "music_disc.mellohi");
        registry.unregister(SoundEvents.MUSIC_DISC_STAL);
        insertAfter(registry, SoundEvents.MUSIC_DISC_MELLOHI, SoundEvents.MUSIC_DISC_STAL, "music_disc.stal");
        registry.unregister(SoundEvents.MUSIC_DISC_STRAD);
        insertAfter(registry, SoundEvents.MUSIC_DISC_STAL, SoundEvents.MUSIC_DISC_STRAD, "music_disc.strad");
        registry.unregister(SoundEvents.MUSIC_DISC_WAIT);
        insertAfter(registry, SoundEvents.MUSIC_DISC_STRAD, SoundEvents.MUSIC_DISC_WAIT, "music_disc.wait");
        registry.unregister(SoundEvents.MUSIC_DISC_WARD);
        insertAfter(registry, SoundEvents.MUSIC_DISC_WAIT, SoundEvents.MUSIC_DISC_WARD, "music_disc.ward");

        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_BREAK);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_STEP);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_PLACE);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_HIT);
        registry.unregister(SoundEvents.BLOCK_ANCIENT_DEBRIS_FALL);
        registry.unregister(SoundEvents.ITEM_ARMOR_EQUIP_NETHERITE);
        registry.unregister(SoundEvents.BLOCK_BASALT_BREAK);
        registry.unregister(SoundEvents.BLOCK_BASALT_STEP);
        registry.unregister(SoundEvents.BLOCK_BASALT_PLACE);
        registry.unregister(SoundEvents.BLOCK_BASALT_HIT);
        registry.unregister(SoundEvents.BLOCK_BASALT_FALL);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_BONE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_ROOTS_BREAK);
        registry.unregister(SoundEvents.BLOCK_ROOTS_STEP);
        registry.unregister(SoundEvents.BLOCK_ROOTS_PLACE);
        registry.unregister(SoundEvents.BLOCK_ROOTS_HIT);
        registry.unregister(SoundEvents.BLOCK_ROOTS_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_BRICKS_FALL);
        registry.unregister(SoundEvents.BLOCK_STEM_BREAK);
        registry.unregister(SoundEvents.BLOCK_STEM_STEP);
        registry.unregister(SoundEvents.BLOCK_STEM_PLACE);
        registry.unregister(SoundEvents.BLOCK_STEM_HIT);
        registry.unregister(SoundEvents.BLOCK_STEM_FALL);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_BREAK);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_STEP);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_PLACE);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_HIT);
        registry.unregister(SoundEvents.BLOCK_NYLIUM_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_SPROUTS_FALL);
        registry.unregister(SoundEvents.BLOCK_FUNGUS_BREAK);
        registry.unregister(SoundEvents.BLOCK_FUNGUS_STEP);
        registry.unregister(SoundEvents.BLOCK_FUNGUS_PLACE);
        registry.unregister(SoundEvents.BLOCK_FUNGUS_HIT);
        registry.unregister(SoundEvents.BLOCK_FUNGUS_FALL);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_BREAK);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_STEP);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_PLACE);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_HIT);
        registry.unregister(SoundEvents.BLOCK_WEEPING_VINES_FALL);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_WART_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHERITE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_STEP);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHERRACK_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_BREAK);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_FALL);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_HIT);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_PLACE);
        registry.unregister(SoundEvents.BLOCK_NETHER_ORE_STEP);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_BREAK);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_STEP);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_PLACE);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_HIT);
        registry.unregister(SoundEvents.BLOCK_SHROOMLIGHT_FALL);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_BREAK);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_STEP);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_PLACE);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_HIT);
        registry.unregister(SoundEvents.BLOCK_SOUL_SAND_FALL);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_BREAK);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_STEP);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_PLACE);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_HIT);
        registry.unregister(SoundEvents.BLOCK_SOUL_SOIL_FALL);
        registry.unregister(SoundEvents.PARTICLE_SOUL_ESCAPE);

        registry.unregister(SoundEvents.ENTITY_PIGLIN_ADMIRING_ITEM);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_ANGRY);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_CELEBRATE);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_DEATH);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_JEALOUS);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_HURT);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_RETREAT);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_STEP);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_CONVERTED_TO_ZOMBIFIED);

        registry.unregister(SoundEvents.ENTITY_HOGLIN_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_ANGRY);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_ATTACK);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_DEATH);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_HURT);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_RETREAT);
        registry.unregister(SoundEvents.ENTITY_HOGLIN_STEP);

        registry.unregister(SoundEvents.ENTITY_SNOW_GOLEM_SHEAR);

        registry.unregister(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP);
        registry.unregister(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD);
        registry.unregister(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_NETHER_WASTES_LOOP);
        registry.unregister(SoundEvents.AMBIENT_NETHER_WASTES_MOOD);
        registry.unregister(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP);
        registry.unregister(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD);
        registry.unregister(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_WARPED_FOREST_LOOP);
        registry.unregister(SoundEvents.AMBIENT_WARPED_FOREST_MOOD);

        registry.unregister(SoundEvents.BLOCK_SMITHING_TABLE_USE);

        registry.unregister(SoundEvents.BLOCK_LODESTONE_BREAK);
        registry.unregister(SoundEvents.BLOCK_LODESTONE_STEP);
        registry.unregister(SoundEvents.BLOCK_LODESTONE_PLACE);
        registry.unregister(SoundEvents.BLOCK_LODESTONE_HIT);
        registry.unregister(SoundEvents.BLOCK_LODESTONE_FALL);
        registry.unregister(SoundEvents.ITEM_LODESTONE_COMPASS_LOCK);
        registry.unregister(SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE);
        registry.unregister(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE);
        registry.unregister(SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN);

        registry.unregister(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS);
        registry.unregister(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP);
        registry.unregister(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD);

        registry.unregister(SoundEvents.MUSIC_NETHER_BASALT_DELTAS);
        registry.unregister(SoundEvents.MUSIC_NETHER_NETHER_WASTES);
        registry.unregister(SoundEvents.MUSIC_NETHER_SOUL_SAND_VALLEY);
        registry.unregister(SoundEvents.MUSIC_NETHER_CRIMSON_FOREST);
        registry.unregister(SoundEvents.MUSIC_NETHER_WARPED_FOREST);

        insertAfter(registry, SoundEvents.MUSIC_MENU, SoundEvents.MUSIC_NETHER_NETHER_WASTES, "music.nether");

        registry.unregister(SoundEvents.MUSIC_DISC_PIGSTEP);
    }

    @Override
    protected void recomputeStatesForBlock(Block block) {
        if (block == Blocks.JIGSAW) {
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.DOWN_EAST));
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.UP_EAST));
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.NORTH_UP));
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.SOUTH_UP));
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.WEST_UP));
            Block.STATE_IDS.add(Blocks.JIGSAW.getDefaultState().with(JigsawBlock.ORIENTATION, JigsawOrientation.EAST_UP));
        } else {
            super.recomputeStatesForBlock(block);
        }
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() instanceof WallBlock
                && (state.get(WallBlock.EAST_SHAPE) == WallShape.TALL
                || state.get(WallBlock.NORTH_SHAPE) == WallShape.TALL
                || state.get(WallBlock.SOUTH_SHAPE) == WallShape.TALL
                || state.get(WallBlock.WEST_SHAPE) == WallShape.TALL))
            return false;

        return super.acceptBlockState(state);
    }

    @Override
    public Map<Tag.Identified<Block>, Set<Block>> getExtraBlockTags() {
        Map<Tag.Identified<Block>, Set<Block>> tags = super.getExtraBlockTags();
        tags.put(BlockTags.CRIMSON_STEMS, ImmutableSet.of());
        tags.put(BlockTags.WARPED_STEMS, ImmutableSet.of());
        tags.put(BlockTags.LOGS_THAT_BURN, ImmutableSet.<Block>builder()
                .addAll(getExistingTags(tags, BlockTags.LOGS))
                .build());
        tags.put(BlockTags.STONE_PRESSURE_PLATES, ImmutableSet.of(Blocks.STONE_PRESSURE_PLATE));
        tags.put(BlockTags.PRESSURE_PLATES, ImmutableSet.<Block>builder()
                .addAll(getExistingTags(tags, BlockTags.WOODEN_PRESSURE_PLATES))
                .addAll(getExistingTags(tags, BlockTags.PRESSURE_PLATES))
                .build());
        tags.put(BlockTags.WITHER_SUMMON_BASE_BLOCKS, ImmutableSet.of(Blocks.SOUL_SAND));
        tags.put(BlockTags.FIRE, ImmutableSet.of(Blocks.FIRE));
        tags.put(BlockTags.NYLIUM, ImmutableSet.of());
        tags.put(BlockTags.WART_BLOCKS, ImmutableSet.of());
        tags.put(BlockTags.BEACON_BASE_BLOCKS, ImmutableSet.of(Blocks.EMERALD_BLOCK, Blocks.DIAMOND_BLOCK, Blocks.GOLD_BLOCK, Blocks.IRON_BLOCK));
        tags.put(BlockTags.SOUL_SPEED_BLOCKS, ImmutableSet.of());
        tags.put(BlockTags.WALL_POST_OVERRIDE, ImmutableSet.<Block>builder()
                .add(Blocks.TORCH, Blocks.REDSTONE_TORCH, Blocks.TRIPWIRE)
                .addAll(getExistingTags(tags, BlockTags.SIGNS))
                .addAll(getExistingTags(tags, BlockTags.BANNERS))
                .addAll(getExistingTags(tags, BlockTags.PRESSURE_PLATES))
                .build());
        tags.put(BlockTags.CLIMBABLE, ImmutableSet.of(Blocks.LADDER, Blocks.VINE, Blocks.SCAFFOLDING));
        tags.put(BlockTags.PIGLIN_REPELLENTS, ImmutableSet.of());
        tags.put(BlockTags.HOGLIN_REPELLENTS, ImmutableSet.of());
        tags.put(BlockTags.GOLD_ORES, ImmutableSet.of(Blocks.GOLD_ORE));
        tags.put(BlockTags.SOUL_FIRE_BASE_BLOCKS, ImmutableSet.of());
        tags.put(BlockTags.NON_FLAMMABLE_WOOD, ImmutableSet.of());
        tags.put(BlockTags.STRIDER_WARM_BLOCKS, ImmutableSet.of());
        tags.put(BlockTags.CAMPFIRES, ImmutableSet.of(Blocks.CAMPFIRE));
        tags.put(BlockTags.GUARDED_BY_PIGLINS, ImmutableSet.of());
        return tags;
    }

    @Override
    public Map<Tag.Identified<Item>, Set<Item>> getExtraItemTags() {
        Map<Tag.Identified<Block>, Set<Block>> blockTags = getExtraBlockTags();
        Map<Tag.Identified<Item>, Set<Item>> tags = super.getExtraItemTags();
        tags.put(ItemTags.CRIMSON_STEMS, copyBlocks(blockTags, BlockTags.CRIMSON_STEMS));
        tags.put(ItemTags.WARPED_STEMS, copyBlocks(blockTags, BlockTags.WARPED_STEMS));
        tags.put(ItemTags.LOGS_THAT_BURN, copyBlocks(blockTags, BlockTags.LOGS_THAT_BURN));
        tags.put(ItemTags.GOLD_ORES, copyBlocks(blockTags, BlockTags.GOLD_ORES));
        tags.put(ItemTags.SOUL_FIRE_BASE_BLOCKS, copyBlocks(blockTags, BlockTags.SOUL_FIRE_BASE_BLOCKS));
        tags.put(ItemTags.CREEPER_DROP_MUSIC_DISCS, getExistingTags(tags, ItemTags.MUSIC_DISCS));
        tags.put(ItemTags.BEACON_PAYMENT_ITEMS, ImmutableSet.of(Items.EMERALD, Items.DIAMOND, Items.GOLD_INGOT, Items.IRON_INGOT));
        tags.put(ItemTags.PIGLIN_REPELLENTS, ImmutableSet.of());
        tags.put(ItemTags.NON_FLAMMABLE_WOOD, ImmutableSet.of());
        tags.put(ItemTags.STONE_TOOL_MATERIALS, ImmutableSet.of(Items.COBBLESTONE));
        tags.put(ItemTags.FURNACE_MATERIALS, ImmutableSet.of(Items.COBBLESTONE));
        return tags;
    }

    @Override
    public Map<Tag.Identified<EntityType<?>>, Set<EntityType<?>>> getExtraEntityTags() {
        Map<Tag.Identified<EntityType<?>>, Set<EntityType<?>>> tags = super.getExtraEntityTags();
        tags.put(EntityTypeTags.IMPACT_PROJECTILES, ImmutableSet.<EntityType<?>>builder()
                .addAll(getExistingTags(tags, EntityTypeTags.ARROWS))
                .add(EntityType.SNOWBALL, EntityType.FIREBALL, EntityType.SMALL_FIREBALL, EntityType.EGG, EntityType.TRIDENT, EntityType.DRAGON_FIREBALL, EntityType.WITHER_SKULL)
                .build());
        return tags;
    }
}
