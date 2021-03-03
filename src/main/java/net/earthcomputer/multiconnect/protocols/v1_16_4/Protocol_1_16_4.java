package net.earthcomputer.multiconnect.protocols.v1_16_4;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.CommandTreeS2CAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.DimensionTypeAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.EntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.ShulkerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_4.mixin.TagGroupSerializedAccessor;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.earthcomputer.multiconnect.transformer.Codecked;
import net.earthcomputer.multiconnect.transformer.TransformerByteBuf;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.*;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.option.ChatVisibility;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.packet.c2s.play.ClientSettingsC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stat;
import net.minecraft.stat.StatType;
import net.minecraft.tag.*;
import net.minecraft.text.Text;
import net.minecraft.util.Arm;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.event.GameEvent;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Protocol_1_16_4 extends Protocol_1_17 {
    public static final int BIOME_ARRAY_LENGTH = 1024;

    private static final TrackedData<Optional<BlockPos>> OLD_SHULKER_ATTACHED_POSITION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // entity id
            buf.readBoolean(); // hardcode
            buf.readByte(); // game mode
            buf.readByte(); // previous game mode
            buf.disablePassthroughMode();
            int numDimensions = buf.readVarInt();
            Set<RegistryKey<World>> dimensionIds = new HashSet<>(numDimensions);
            for (int i = 0; i < numDimensions; i++) {
                dimensionIds.add(RegistryKey.of(Registry.DIMENSION, buf.readIdentifier()));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<Collection<RegistryKey<World>>>) (Class<?>) Collection.class, (Class<RegistryKey<World>>) (Class<?>) RegistryKey.class, dimensionIds);
            Codec<Set<Identifier>> dimensionSetCodec = Codec.list(Utils.singletonKeyCodec("name", Identifier.CODEC))
                    .xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
            Utils.translateDynamicRegistries(
                    buf,
                    Utils.singletonKeyCodec("minecraft:dimension_type", Utils.singletonKeyCodec("value", dimensionSetCodec)),
                    ImmutableSet.of(new Identifier("overworld"), new Identifier("the_nether"), new Identifier("the_end"), new Identifier("overworld_caves"))::equals
            );
            translateDimensionType(buf);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerRespawnS2CPacket.class, buf -> {
            translateDimensionType(buf);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int x = buf.readInt();
            int z = buf.readInt();
            buf.disablePassthroughMode();
            boolean fullChunk = buf.readBoolean();
            PendingFullChunkData.setPendingFullChunk(new ChunkPos(x, z), fullChunk);
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // vertical strip bitmask
            buf.enablePassthroughMode();
            buf.readCompoundTag(); // heightmaps
            if (fullChunk) {
                buf.readIntArray(BiomeArray.DEFAULT_LENGTH);
            } else {
                // TODO: get the actual biome array from somewhere
                buf.pendingRead(int[].class, new int[BIOME_ARRAY_LENGTH]);
            }
            int dataLength = buf.readVarInt();
            buf.readBytes(new byte[dataLength]); // data
            buf.disablePassthroughMode();
            int numBlockEntities = buf.readVarInt();
            List<CompoundTag> blockEntities = new ArrayList<>(numBlockEntities);
            for (int i = 0; i < numBlockEntities; i++) {
                blockEntities.add(buf.readCompoundTag());
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<CompoundTag>>) (Class<?>) List.class, CompoundTag.class, blockEntities);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(LightUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // x
            buf.readVarInt(); // z
            buf.readBoolean(); // trust edges
            buf.disablePassthroughMode();
            int skyLightMask = buf.readVarInt();
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {skyLightMask})); // sky light mask
            int blockLightMask = buf.readVarInt();
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {blockLightMask})); // block light mask
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // filled sky light mask
            buf.pendingRead(BitSet.class, BitSet.valueOf(new long[] {buf.readVarInt()})); // filled block light mask
            int numSkyUpdates = Integer.bitCount(skyLightMask);
            List<byte[]> skyUpdates = new ArrayList<>(numSkyUpdates);
            for (int i = 0; i < numSkyUpdates; i++) {
                skyUpdates.add(buf.readByteArray(2048));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<byte[]>>) (Class<?>) List.class, byte[].class, skyUpdates);
            int numBlockUpdates = Integer.bitCount(blockLightMask);
            List<byte[]> blockUpdates = new ArrayList<>(numBlockUpdates);
            for (int i = 0; i < numBlockUpdates; i++) {
                blockUpdates.add(buf.readByteArray(2048));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<byte[]>>) (Class<?>) List.class, byte[].class, blockUpdates);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(SynchronizeTagsS2CPacket.class, buf -> {
            Map<RegistryKey<? extends Registry<?>>, TagGroup.Serialized> tags = new HashMap<>(5);
            tags.put(RegistryKey.ofRegistry(new Identifier("block")), readTagGroupNetworkData(buf));
            tags.put(RegistryKey.ofRegistry(new Identifier("item")), readTagGroupNetworkData(buf));
            tags.put(RegistryKey.ofRegistry(new Identifier("fluid")), readTagGroupNetworkData(buf));
            tags.put(RegistryKey.ofRegistry(new Identifier("entity_type")), readTagGroupNetworkData(buf));
            tags.put(RegistryKey.ofRegistry(new Identifier("game_event")), TagGroupSerializedAccessor.createTagGroupSerialized(new HashMap<>()));
            buf.pendingReadMapUnchecked(Identifier.class, TagGroup.Serialized.class, tags);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerPositionLookS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readDouble(); // x
            buf.readDouble(); // y
            buf.readDouble(); // z
            buf.readFloat(); // yaw
            buf.readFloat(); // pitch
            buf.readUnsignedByte(); // flags
            buf.readVarInt(); // teleport id
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, false);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ResourcePackSendS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(32767); // url
            buf.readString(40); // hash
            buf.disablePassthroughMode();
            buf.pendingRead(Boolean.class, false); // required
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerSpawnPositionS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readBlockPos(); // pos
            buf.disablePassthroughMode();
            buf.pendingRead(Float.class, 0f); // angle
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ExplosionS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            float x = buf.readFloat();
            float y = buf.readFloat();
            float z = buf.readFloat();
            buf.readFloat(); // power
            buf.disablePassthroughMode();
            int blockX = MathHelper.floor(x);
            int blockY = MathHelper.floor(y);
            int blockZ = MathHelper.floor(z);
            int numAffectedBlocks = buf.readInt();
            List<BlockPos> affectedBlocks = new ArrayList<>();
            for (int i = 0; i < numAffectedBlocks; i++) {
                affectedBlocks.add(new BlockPos(buf.readByte() + blockX, buf.readByte() + blockY, buf.readByte() + blockZ));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<BlockPos>>) (Class<?>) List.class, BlockPos.class, affectedBlocks);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityAttributesS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            int numEntries = buf.readInt();
            List<EntityAttributesS2CPacket.Entry> entries = new ArrayList<>(numEntries);
            for (int i = 0; i < numEntries; i++) {
                Identifier attributeId = buf.readIdentifier();
                EntityAttribute attribute = Registry.ATTRIBUTE.get(attributeId);
                double baseValue = buf.readDouble();
                int modifierCount = buf.readVarInt();
                List<EntityAttributeModifier> modifiers = new ArrayList<>(modifierCount);
                for (int j = 0; j < modifierCount; j++) {
                    UUID uuid = buf.readUuid();
                    modifiers.add(new EntityAttributeModifier(uuid, "Unknown synced attribute modifier", buf.readDouble(), EntityAttributeModifier.Operation.fromId(buf.readByte())));
                }
                entries.add(new EntityAttributesS2CPacket.Entry(attribute, baseValue, modifiers));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<EntityAttributesS2CPacket.Entry>>) (Class<?>) List.class, EntityAttributesS2CPacket.Entry.class, entries);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(StatisticsS2CPacket.class, buf -> {
            int numStats = buf.readVarInt();
            Object2IntMap<Stat<?>> stats = new Object2IntOpenHashMap<>(numStats);
            for (int i = 0; i < numStats; i++) {
                StatType<?> statType = Registry.STAT_TYPE.get(buf.readVarInt());
                if (statType != null) {
                    Stat<?> stat = (Stat<?>) statType.getRegistry().get(buf.readVarInt());
                    int value = buf.readVarInt();
                    stats.put(stat, value);
                }
            }
            buf.pendingReadMapUnchecked(Stat.class, VarInt.class, stats);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(CommandSuggestionsS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // id
            int start = buf.readVarInt();
            int length = buf.readVarInt();
            buf.disablePassthroughMode();
            StringRange range = StringRange.between(start, start + length);
            int numSuggestions = buf.readVarInt();
            List<Suggestion> suggestions = new ArrayList<>(numSuggestions);
            for (int i = 0; i < numSuggestions; i++) {
                String value = buf.readString();
                Text tooltip = buf.readBoolean() ? buf.readText() : null;
                suggestions.add(new Suggestion(range, value, tooltip));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<Suggestion>>) (Class<?>) List.class, Suggestion.class, suggestions);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(CommandTreeS2CPacket.class, buf -> {
            int numNodes = buf.readVarInt();
            List<CommandTreeS2CPacket.CommandNodeData> nodes = new ArrayList<>(numNodes);
            for (int i = 0; i < numNodes; i++) {
                nodes.add(CommandTreeS2CAccessor.callReadCommandNode(buf));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<CommandTreeS2CPacket.CommandNodeData>>) (Class<?>) List.class, CommandTreeS2CPacket.CommandNodeData.class, nodes);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerListS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            PlayerListS2CPacket.class_5893 operation = buf.readEnumConstant(PlayerListS2CPacket.class_5893.class);
            buf.disablePassthroughMode();
            int numEntries = buf.readVarInt();
            List<PlayerListS2CPacket.Entry> entries = new ArrayList<>(numEntries);
            for (int i = 0; i < numEntries; i++) {
                switch (operation) {
                    case ADD_PLAYER: {
                        GameProfile profile = new GameProfile(buf.readUuid(), buf.readString(16));
                        int numProperties = buf.readVarInt();
                        for (int j = 0; j < numProperties; j++) {
                            String propertyKey = buf.readString();
                            String value = buf.readString();
                            if (buf.readBoolean()) {
                                String signature = buf.readString();
                                profile.getProperties().put(propertyKey, new Property(propertyKey, value, signature));
                            } else {
                                profile.getProperties().put(propertyKey, new Property(propertyKey, value));
                            }
                        }
                        GameMode gameMode = GameMode.byId(buf.readVarInt());
                        int latency = buf.readVarInt();
                        Text displayName = buf.readBoolean() ? buf.readText() : null;
                        entries.add(new PlayerListS2CPacket.Entry(profile, latency, gameMode, displayName));
                    }
                    break;
                    case UPDATE_GAME_MODE: {
                        GameProfile profile = new GameProfile(buf.readUuid(), null);
                        GameMode gameMode = GameMode.byId(buf.readVarInt());
                        entries.add(new PlayerListS2CPacket.Entry(profile, 0, gameMode, null));
                    }
                    break;
                    case UPDATE_LATENCY: {
                        GameProfile profile = new GameProfile(buf.readUuid(), null);
                        int latency = buf.readVarInt();
                        entries.add(new PlayerListS2CPacket.Entry(profile, latency, null, null));
                    }
                    break;
                    case UPDATE_DISPLAY_NAME: {
                        GameProfile profile = new GameProfile(buf.readUuid(), null);
                        Text displayName = buf.readBoolean() ? buf.readText() : null;
                        entries.add(new PlayerListS2CPacket.Entry(profile, 0, null, displayName));
                    }
                    break;
                    case REMOVE_PLAYER: {
                        GameProfile profile = new GameProfile(buf.readUuid(), null);
                        entries.add(new PlayerListS2CPacket.Entry(profile, 0, null, null));
                    }
                    break;
                }
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<PlayerListS2CPacket.Entry>>) (Class<?>) List.class, PlayerListS2CPacket.Entry.class, entries);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(UnlockRecipesS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            UnlockRecipesS2CPacket.Action action = buf.readEnumConstant(UnlockRecipesS2CPacket.Action.class);
            RecipeBookOptions.fromPacket(buf); // options
            buf.disablePassthroughMode();
            int numRecipeIdsToChange = buf.readVarInt();
            List<Identifier> recipeIdsToChange = new ArrayList<>(numRecipeIdsToChange);
            for (int i = 0; i < numRecipeIdsToChange; i++) {
                recipeIdsToChange.add(buf.readIdentifier());
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<Identifier>>) (Class<?>) List.class, Identifier.class, recipeIdsToChange);
            if (action == UnlockRecipesS2CPacket.Action.INIT) {
                int numRecipeIdsToInit = buf.readVarInt();
                List<Identifier> recipeIdsToInit = new ArrayList<>(numRecipeIdsToInit);
                for (int i = 0; i < numRecipeIdsToInit; i++) {
                    recipeIdsToInit.add(buf.readIdentifier());
                }
                //noinspection unchecked
                buf.pendingReadCollection((Class<List<Identifier>>) (Class<?>) List.class, Identifier.class, recipeIdsToInit);
            }
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntitiesDestroyS2CPacket.class, buf -> {
            int numEntities = buf.readVarInt();
            IntList entities = new IntArrayList(numEntities);
            for (int i = 0; i < numEntities; i++) {
                entities.add(buf.readVarInt());
            }
            buf.pendingRead(IntList.class, entities);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(class_5900.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(16); // name
            int mode = buf.readByte();
            buf.disablePassthroughMode();
            if (mode != 0 && mode != 3 && mode != 4) {
                buf.applyPendingReads();
                return;
            }
            if (mode == 0) {
                buf.enablePassthroughMode();
                buf.readText(); // display name
                buf.readByte(); // options
                buf.readString(40); // nametag visibility
                buf.readString(40); // collision rule
                buf.readEnumConstant(Formatting.class); // color
                buf.readText(); // prefix
                buf.readText(); // suffix
                buf.disablePassthroughMode();
            }
            int numPlayers = buf.readVarInt();
            List<String> players = new ArrayList<>(numPlayers);
            for (int i = 0; i < numPlayers; i++) {
                players.add(buf.readString(40));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<String>>) (Class<?>) List.class, String.class, players);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(AdvancementUpdateS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readBoolean(); // clear current
            buf.disablePassthroughMode();
            int numToEarn = buf.readVarInt();
            Map<Identifier, Advancement.Task> toEarn = new HashMap<>(numToEarn);
            for (int i = 0; i < numToEarn; i++) {
                toEarn.put(buf.readIdentifier(), Advancement.Task.fromPacket(buf));
            }
            buf.pendingReadMap(Identifier.class, Advancement.Task.class, toEarn);
            int numToRemove = buf.readVarInt();
            Set<Identifier> toRemove = new LinkedHashSet<>(numToRemove);
            for (int i = 0; i < numToRemove; i++) {
                toRemove.add(buf.readIdentifier());
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<Collection<Identifier>>) (Class<?>) Collection.class, Identifier.class, toRemove);
            int numToSetProgress = buf.readVarInt();
            Map<Identifier, AdvancementProgress> toSetProgress = new HashMap<>(numToSetProgress);
            for (int i = 0; i < numToSetProgress; i++) {
                toSetProgress.put(buf.readIdentifier(), AdvancementProgress.fromPacket(buf));
            }
            buf.pendingReadMap(Identifier.class, AdvancementProgress.class, toSetProgress);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(SynchronizeRecipesS2CPacket.class, buf -> {
            int numRecipes = buf.readVarInt();
            List<Recipe<?>> recipes = new ArrayList<>(numRecipes);
            for (int i = 0; i < numRecipes; i++) {
                recipes.add(SynchronizeRecipesS2CPacket.readRecipe(buf));
            }
            //noinspection unchecked
            buf.pendingReadCollection((Class<List<Recipe<?>>>) (Class<?>) List.class, (Class<Recipe<?>>) (Class<?>) Recipe.class, recipes);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerOutboundTranslator(ClientSettingsC2SPacket.class, buf -> {
            buf.passthroughWrite(String.class); // language
            buf.passthroughWrite(Byte.class); // view distance
            buf.passthroughWrite(ChatVisibility.class); // chat visibility
            buf.passthroughWrite(Boolean.class); // chat colors
            buf.passthroughWrite(Byte.class); // player model bitmask
            buf.passthroughWrite(Arm.class); // main arm
            buf.skipWrite(Boolean.class); // no filtering
        });
    }

    public static TagGroup.Serialized readTagGroupNetworkData(TransformerByteBuf buf) {
        int numTags = buf.readVarInt();
        Map<Identifier, IntList> tags = new HashMap<>();
        for (int i = 0; i < numTags; i++) {
            Identifier tagId = buf.readIdentifier();
            int numEntries = buf.readVarInt();
            IntList entries = new IntArrayList(numEntries);
            for (int j = 0; j < numEntries; j++) {
                entries.add(buf.readVarInt());
            }
            tags.put(tagId, entries);
        }
        return TagGroupSerializedAccessor.createTagGroupSerialized(tags);
    }

    private static void translateDimensionType(TransformerByteBuf buf) {
        Codecked<Supplier<DimensionType>> dimensionTypeCodecked = Utils.translateDimensionType(buf);
        if (dimensionTypeCodecked != null) {
            Supplier<DimensionType> oldSupplier = dimensionTypeCodecked.getValue();
            dimensionTypeCodecked.setValue(() -> {
                DimensionType oldDimensionType = oldSupplier.get();
                if (oldDimensionType.getMinimumY() == 0 && oldDimensionType.getHeight() == 256) {
                    // nothing to change
                    return oldDimensionType;
                }
                DimensionType newDimensionType = Utils.clone(DimensionType.CODEC, oldDimensionType);
                DimensionTypeAccessor accessor = (DimensionTypeAccessor) newDimensionType;
                accessor.setMinimumY(0);
                accessor.setLogicalHeight(256);
                accessor.setHeight(256);
                return newDimensionType;
            });
        }
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        insertAfter(packets, MapUpdateS2CPacket.class, PacketInfo.of(MapUpdateS2CPacket_1_16_4.class, MapUpdateS2CPacket_1_16_4::new));
        remove(packets, MapUpdateS2CPacket.class);
        remove(packets, VibrationS2CPacket.class);
        remove(packets, ClearTitleS2CPacket.class);
        remove(packets, class_5889.class);
        insertAfter(packets, EntityS2CPacket.Rotate.class, PacketInfo.of(EntityS2CPacket_1_16_4.class, EntityS2CPacket_1_16_4::new));
        insertAfter(packets, PlayerAbilitiesS2CPacket.class, PacketInfo.of(CombatEventS2CPacket_1_16_4.class, CombatEventS2CPacket_1_16_4::new));
        remove(packets, class_5890.class);
        remove(packets, class_5891.class);
        remove(packets, class_5892.class);
        insertAfter(packets, SelectAdvancementTabS2CPacket.class, PacketInfo.of(WorldBorderS2CPacket_1_16_4.class, WorldBorderS2CPacket_1_16_4::new));
        remove(packets, OverlayMessageS2CPacket.class);
        remove(packets, class_5895.class);
        remove(packets, class_5896.class);
        remove(packets, class_5897.class);
        remove(packets, class_5898.class);
        remove(packets, class_5899.class);
        remove(packets, SubtitleS2CPacket.class);
        insertAfter(packets, WorldTimeUpdateS2CPacket.class, PacketInfo.of(TitleS2CPacket_1_16_4.class, TitleS2CPacket_1_16_4::new));
        remove(packets, TitleS2CPacket.class);
        remove(packets, TitleFadeS2CPacket.class);
        return packets;
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_16_4, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.ENTITY_TYPE, this::mutateEntityRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.BLOCK_ENTITY_TYPE, this::mutateBlockEntityRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.PARTICLE_TYPE, this::mutateParticleTypeRegistry);
        mutator.mutate(Protocols.V1_16_4, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    @Override
    public void mutateDynamicRegistries(RegistryMutator mutator, DynamicRegistryManager.Impl registries) {
        super.mutateDynamicRegistries(mutator, registries);
        addRegistry(registries, Registry.DIMENSION_TYPE_KEY);
        addRegistry(registries, Registry.BIOME_KEY);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.WATER_CAULDRON);
        registry.unregister(Blocks.LAVA_CAULDRON);
        registry.unregister(Blocks.POWDER_SNOW_CAULDRON);
        rename(registry, Blocks.DIRT_PATH, "grass_path");
        registry.unregister(Blocks.CANDLE);
        registry.unregister(Blocks.WHITE_CANDLE);
        registry.unregister(Blocks.ORANGE_CANDLE);
        registry.unregister(Blocks.MAGENTA_CANDLE);
        registry.unregister(Blocks.LIGHT_BLUE_CANDLE);
        registry.unregister(Blocks.YELLOW_CANDLE);
        registry.unregister(Blocks.LIME_CANDLE);
        registry.unregister(Blocks.PINK_CANDLE);
        registry.unregister(Blocks.GRAY_CANDLE);
        registry.unregister(Blocks.LIGHT_GRAY_CANDLE);
        registry.unregister(Blocks.CYAN_CANDLE);
        registry.unregister(Blocks.PURPLE_CANDLE);
        registry.unregister(Blocks.BLUE_CANDLE);
        registry.unregister(Blocks.BROWN_CANDLE);
        registry.unregister(Blocks.GREEN_CANDLE);
        registry.unregister(Blocks.RED_CANDLE);
        registry.unregister(Blocks.BLACK_CANDLE);
        registry.unregister(Blocks.CANDLE_CAKE);
        registry.unregister(Blocks.WHITE_CANDLE_CAKE);
        registry.unregister(Blocks.ORANGE_CANDLE_CAKE);
        registry.unregister(Blocks.MAGENTA_CANDLE_CAKE);
        registry.unregister(Blocks.LIGHT_BLUE_CANDLE_CAKE);
        registry.unregister(Blocks.YELLOW_CANDLE_CAKE);
        registry.unregister(Blocks.LIME_CANDLE_CAKE);
        registry.unregister(Blocks.PINK_CANDLE_CAKE);
        registry.unregister(Blocks.GRAY_CANDLE_CAKE);
        registry.unregister(Blocks.LIGHT_GRAY_CANDLE_CAKE);
        registry.unregister(Blocks.CYAN_CANDLE_CAKE);
        registry.unregister(Blocks.PURPLE_CANDLE_CAKE);
        registry.unregister(Blocks.BLUE_CANDLE_CAKE);
        registry.unregister(Blocks.BROWN_CANDLE_CAKE);
        registry.unregister(Blocks.GREEN_CANDLE_CAKE);
        registry.unregister(Blocks.RED_CANDLE_CAKE);
        registry.unregister(Blocks.BLACK_CANDLE_CAKE);
        registry.unregister(Blocks.AMETHYST_BLOCK);
        registry.unregister(Blocks.BUDDING_AMETHYST);
        registry.unregister(Blocks.AMETHYST_CLUSTER);
        registry.unregister(Blocks.LARGE_AMETHYST_BUD);
        registry.unregister(Blocks.MEDIUM_AMETHYST_BUD);
        registry.unregister(Blocks.SMALL_AMETHYST_BUD);
        registry.unregister(Blocks.TUFF);
        registry.unregister(Blocks.CALCITE);
        registry.unregister(Blocks.TINTED_GLASS);
        registry.unregister(Blocks.POWDER_SNOW);
        registry.unregister(Blocks.SCULK_SENSOR);
        registry.unregister(Blocks.WEATHERED_COPPER);
        registry.unregister(Blocks.OXIDIZED_COPPER);
        registry.unregister(Blocks.EXPOSED_COPPER);
        registry.unregister(Blocks.COPPER_BLOCK);
        registry.unregister(Blocks.COPPER_ORE);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.OXIDIZED_CUT_COPPER);
        registry.unregister(Blocks.EXPOSED_CUT_COPPER);
        registry.unregister(Blocks.CUT_COPPER);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.OXIDIZED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.EXPOSED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.OXIDIZED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.EXPOSED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_COPPER_BLOCK);
        registry.unregister(Blocks.WAXED_WEATHERED_COPPER);
        registry.unregister(Blocks.WAXED_EXPOSED_COPPER);
        registry.unregister(Blocks.WAXED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_WEATHERED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_EXPOSED_CUT_COPPER);
        registry.unregister(Blocks.WAXED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS);
        registry.unregister(Blocks.WAXED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB);
        registry.unregister(Blocks.LIGHTNING_ROD);
        registry.unregister(Blocks.POINTED_DRIPSTONE);
        registry.unregister(Blocks.DRIPSTONE_BLOCK);
        registry.unregister(Blocks.GLOW_LICHEN);
        registry.unregister(Blocks.AZALEA_LEAVES);
        registry.unregister(Blocks.AZALEA_LEAVES_FLOWERS);
        registry.unregister(Blocks.CAVE_VINES_HEAD);
        registry.unregister(Blocks.CAVE_VINES_BODY);
        registry.unregister(Blocks.SPORE_BLOSSOM);
        registry.unregister(Blocks.AZALEA);
        registry.unregister(Blocks.FLOWERING_AZALEA);
        registry.unregister(Blocks.MOSS_CARPET);
        registry.unregister(Blocks.MOSS_BLOCK);
        registry.unregister(Blocks.BIG_DRIPLEAF);
        registry.unregister(Blocks.BIG_DRIPLEAF_STEM);
        registry.unregister(Blocks.SMALL_DRIPLEAF);
        registry.unregister(Blocks.ROOTED_DIRT);
        registry.unregister(Blocks.HANGING_ROOTS);
        registry.unregister(Blocks.DEEPSLATE_GOLD_ORE);
        registry.unregister(Blocks.DEEPSLATE_IRON_ORE);
        registry.unregister(Blocks.DEEPSLATE_LAPIS_ORE);
        registry.unregister(Blocks.DEEPSLATE_DIAMOND_ORE);
        registry.unregister(Blocks.DEEPSLATE_REDSTONE_ORE);
        registry.unregister(Blocks.CHISELED_DEEPSLATE);
        registry.unregister(Blocks.DEEPSLATE);
        registry.unregister(Blocks.COBBLED_DEEPSLATE);
        registry.unregister(Blocks.COBBLED_DEEPSLATE_STAIRS);
        registry.unregister(Blocks.COBBLED_DEEPSLATE_SLAB);
        registry.unregister(Blocks.COBBLED_DEEPSLATE_WALL);
        registry.unregister(Blocks.DEEPSLATE_BRICK_SLAB);
        registry.unregister(Blocks.DEEPSLATE_BRICK_STAIRS);
        registry.unregister(Blocks.DEEPSLATE_BRICK_WALL);
        registry.unregister(Blocks.DEEPSLATE_BRICKS);
        registry.unregister(Blocks.DEEPSLATE_TILE_SLAB);
        registry.unregister(Blocks.DEEPSLATE_TILE_STAIRS);
        registry.unregister(Blocks.DEEPSLATE_TILE_WALL);
        registry.unregister(Blocks.DEEPSLATE_TILES);
        registry.unregister(Blocks.POLISHED_DEEPSLATE);
        registry.unregister(Blocks.POLISHED_DEEPSLATE_SLAB);
        registry.unregister(Blocks.POLISHED_DEEPSLATE_STAIRS);
        registry.unregister(Blocks.POLISHED_DEEPSLATE_WALL);
        registry.unregister(Blocks.SMOOTH_BASALT);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        rename(registry, Items.DIRT_PATH, "grass_path");
        registry.unregister(Items.JACK_O_LANTERN);
        insertAfter(registry, Items.GLOWSTONE, Items.JACK_O_LANTERN, "jack_o_lantern");
        registry.unregister(Items.COPPER_INGOT);
        registry.unregister(Items.BUNDLE);
        registry.unregister(Items.AMETHYST_SHARD);
        registry.unregister(Items.SPYGLASS);
        registry.unregister(Items.POWDER_SNOW_BUCKET);
        registry.unregister(Items.AXOLOTL_BUCKET);
        registry.unregister(Items.GLOW_ITEM_FRAME);
        registry.unregister(Items.GLOW_INK_SAC);
        registry.unregister(Items.GLOW_BERRIES);
        registry.unregister(Items.DISPENSER);
        registry.unregister(Items.NOTE_BLOCK);
        registry.unregister(Items.STICKY_PISTON);
        registry.unregister(Items.PISTON);
        registry.unregister(Items.TNT);
        registry.unregister(Items.LEVER);
        registry.unregister(Items.STONE_PRESSURE_PLATE);
        registry.unregister(Items.OAK_PRESSURE_PLATE);
        registry.unregister(Items.SPRUCE_PRESSURE_PLATE);
        registry.unregister(Items.BIRCH_PRESSURE_PLATE);
        registry.unregister(Items.JUNGLE_PRESSURE_PLATE);
        registry.unregister(Items.ACACIA_PRESSURE_PLATE);
        registry.unregister(Items.DARK_OAK_PRESSURE_PLATE);
        registry.unregister(Items.CRIMSON_PRESSURE_PLATE);
        registry.unregister(Items.WARPED_PRESSURE_PLATE);
        registry.unregister(Items.POLISHED_BLACKSTONE_PRESSURE_PLATE);
        registry.unregister(Items.REDSTONE_TORCH);
        registry.unregister(Items.OAK_TRAPDOOR);
        registry.unregister(Items.SPRUCE_TRAPDOOR);
        registry.unregister(Items.BIRCH_TRAPDOOR);
        registry.unregister(Items.JUNGLE_TRAPDOOR);
        registry.unregister(Items.ACACIA_TRAPDOOR);
        registry.unregister(Items.DARK_OAK_TRAPDOOR);
        registry.unregister(Items.CRIMSON_TRAPDOOR);
        registry.unregister(Items.WARPED_TRAPDOOR);
        registry.unregister(Items.OAK_FENCE_GATE);
        registry.unregister(Items.SPRUCE_FENCE_GATE);
        registry.unregister(Items.BIRCH_FENCE_GATE);
        registry.unregister(Items.JUNGLE_FENCE_GATE);
        registry.unregister(Items.ACACIA_FENCE_GATE);
        registry.unregister(Items.DARK_OAK_FENCE_GATE);
        registry.unregister(Items.CRIMSON_FENCE_GATE);
        registry.unregister(Items.WARPED_FENCE_GATE);
        registry.unregister(Items.REDSTONE_LAMP);
        registry.unregister(Items.TRIPWIRE_HOOK);
        registry.unregister(Items.STONE_BUTTON);
        registry.unregister(Items.OAK_BUTTON);
        registry.unregister(Items.SPRUCE_BUTTON);
        registry.unregister(Items.BIRCH_BUTTON);
        registry.unregister(Items.JUNGLE_BUTTON);
        registry.unregister(Items.ACACIA_BUTTON);
        registry.unregister(Items.DARK_OAK_BUTTON);
        registry.unregister(Items.CRIMSON_BUTTON);
        registry.unregister(Items.WARPED_BUTTON);
        registry.unregister(Items.POLISHED_BLACKSTONE_BUTTON);
        registry.unregister(Items.TRAPPED_CHEST);
        registry.unregister(Items.LIGHT_WEIGHTED_PRESSURE_PLATE);
        registry.unregister(Items.HEAVY_WEIGHTED_PRESSURE_PLATE);
        registry.unregister(Items.DAYLIGHT_DETECTOR);
        registry.unregister(Items.REDSTONE_BLOCK);
        registry.unregister(Items.HOPPER);
        registry.unregister(Items.DROPPER);
        registry.unregister(Items.IRON_TRAPDOOR);
        registry.unregister(Items.OBSERVER);
        registry.unregister(Items.REPEATER);
        registry.unregister(Items.COMPARATOR);
        registry.unregister(Items.REDSTONE);
        registry.unregister(Items.LECTERN);
        registry.unregister(Items.TARGET);
        registry.unregister(Items.SLIME_BLOCK);
        registry.unregister(Items.HONEY_BLOCK);
        insertAfter(registry, Items.LAPIS_BLOCK, Items.DISPENSER, "dispenser");
        insertAfter(registry, Items.CUT_SANDSTONE, Items.NOTE_BLOCK, "note_block");
        insertAfter(registry, Items.DETECTOR_RAIL, Items.STICKY_PISTON, "sticky_piston");
        insertAfter(registry, Items.SEA_PICKLE, Items.PISTON, "piston");
        insertAfter(registry, Items.BRICKS, Items.TNT, "tnt");
        insertAfter(registry, Items.COBBLESTONE_STAIRS, Items.LEVER, "lever");
        insertAfter(registry, Items.LEVER, Items.STONE_PRESSURE_PLATE, "stone_pressure_plate");
        insertAfter(registry, Items.STONE_PRESSURE_PLATE, Items.OAK_PRESSURE_PLATE, "oak_pressure_plate");
        insertAfter(registry, Items.OAK_PRESSURE_PLATE, Items.SPRUCE_PRESSURE_PLATE, "spruce_pressure_plate");
        insertAfter(registry, Items.SPRUCE_PRESSURE_PLATE, Items.BIRCH_PRESSURE_PLATE, "birch_pressure_plate");
        insertAfter(registry, Items.BIRCH_PRESSURE_PLATE, Items.JUNGLE_PRESSURE_PLATE, "jungle_pressure_plate");
        insertAfter(registry, Items.JUNGLE_PRESSURE_PLATE, Items.ACACIA_PRESSURE_PLATE, "acacia_pressure_plate");
        insertAfter(registry, Items.ACACIA_PRESSURE_PLATE, Items.DARK_OAK_PRESSURE_PLATE, "dark_oak_pressure_plate");
        insertAfter(registry, Items.DARK_OAK_PRESSURE_PLATE, Items.CRIMSON_PRESSURE_PLATE, "crimson_pressure_plate");
        insertAfter(registry, Items.CRIMSON_PRESSURE_PLATE, Items.WARPED_PRESSURE_PLATE, "warped_pressure_plate");
        insertAfter(registry, Items.WARPED_PRESSURE_PLATE, Items.POLISHED_BLACKSTONE_PRESSURE_PLATE, "polished_blackstone_pressure_plate");
        insertAfter(registry, Items.REDSTONE_ORE, Items.REDSTONE_TORCH, "redstone_torch");
        insertAfter(registry, Items.JACK_O_LANTERN, Items.OAK_TRAPDOOR, "oak_trapdoor");
        insertAfter(registry, Items.OAK_TRAPDOOR, Items.SPRUCE_TRAPDOOR, "spruce_trapdoor");
        insertAfter(registry, Items.SPRUCE_TRAPDOOR, Items.BIRCH_TRAPDOOR, "birch_trapdoor");
        insertAfter(registry, Items.BIRCH_TRAPDOOR, Items.JUNGLE_TRAPDOOR, "jungle_trapdoor");
        insertAfter(registry, Items.JUNGLE_TRAPDOOR, Items.ACACIA_TRAPDOOR, "acacia_trapdoor");
        insertAfter(registry, Items.ACACIA_TRAPDOOR, Items.DARK_OAK_TRAPDOOR, "dark_oak_trapdoor");
        insertAfter(registry, Items.DARK_OAK_TRAPDOOR, Items.CRIMSON_TRAPDOOR, "crimson_trapdoor");
        insertAfter(registry, Items.CRIMSON_TRAPDOOR, Items.WARPED_TRAPDOOR, "warped_trapdoor");
        insertAfter(registry, Items.VINE, Items.OAK_FENCE_GATE, "oak_fence_gate");
        insertAfter(registry, Items.OAK_FENCE_GATE, Items.SPRUCE_FENCE_GATE, "spruce_fence_gate");
        insertAfter(registry, Items.SPRUCE_FENCE_GATE, Items.BIRCH_FENCE_GATE, "birch_fence_gate");
        insertAfter(registry, Items.BIRCH_FENCE_GATE, Items.JUNGLE_FENCE_GATE, "jungle_fence_gate");
        insertAfter(registry, Items.JUNGLE_FENCE_GATE, Items.ACACIA_FENCE_GATE, "acacia_fence_gate");
        insertAfter(registry, Items.ACACIA_FENCE_GATE, Items.DARK_OAK_FENCE_GATE, "dark_oak_fence_gate");
        insertAfter(registry, Items.DARK_OAK_FENCE_GATE, Items.CRIMSON_FENCE_GATE, "crimson_fence_gate");
        insertAfter(registry, Items.CRIMSON_FENCE_GATE, Items.WARPED_FENCE_GATE, "warped_fence_gate");
        insertAfter(registry, Items.DRAGON_EGG, Items.REDSTONE_LAMP, "redstone_lamp");
        insertAfter(registry, Items.ENDER_CHEST, Items.TRIPWIRE_HOOK, "tripwire_hook");
        insertAfter(registry, Items.POLISHED_BLACKSTONE_BRICK_WALL, Items.STONE_BUTTON, "stone_button");
        insertAfter(registry, Items.STONE_BUTTON, Items.OAK_BUTTON, "oak_button");
        insertAfter(registry, Items.OAK_BUTTON, Items.SPRUCE_BUTTON, "spruce_button");
        insertAfter(registry, Items.SPRUCE_BUTTON, Items.BIRCH_BUTTON, "birch_button");
        insertAfter(registry, Items.BIRCH_BUTTON, Items.JUNGLE_BUTTON, "jungle_button");
        insertAfter(registry, Items.JUNGLE_BUTTON, Items.ACACIA_BUTTON, "acacia_button");
        insertAfter(registry, Items.ACACIA_BUTTON, Items.DARK_OAK_BUTTON, "dark_oak_button");
        insertAfter(registry, Items.DARK_OAK_BUTTON, Items.CRIMSON_BUTTON, "crimson_button");
        insertAfter(registry, Items.CRIMSON_BUTTON, Items.WARPED_BUTTON, "warped_button");
        insertAfter(registry, Items.WARPED_BUTTON, Items.POLISHED_BLACKSTONE_BUTTON, "polished_blackstone_button");
        insertAfter(registry, Items.DAMAGED_ANVIL, Items.TRAPPED_CHEST, "trapped_chest");
        insertAfter(registry, Items.TRAPPED_CHEST, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, "light_weighted_pressure_plate");
        insertAfter(registry, Items.LIGHT_WEIGHTED_PRESSURE_PLATE, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, "heavy_weighted_pressure_plate");
        insertAfter(registry, Items.HEAVY_WEIGHTED_PRESSURE_PLATE, Items.DAYLIGHT_DETECTOR, "daylight_detector");
        insertAfter(registry, Items.DAYLIGHT_DETECTOR, Items.REDSTONE_BLOCK, "redstone_block");
        insertAfter(registry, Items.NETHER_QUARTZ_ORE, Items.HOPPER, "hopper");
        insertAfter(registry, Items.ACTIVATOR_RAIL, Items.DROPPER, "dropper");
        insertAfter(registry, Items.BARRIER, Items.IRON_TRAPDOOR, "iron_trapdoor");
        insertAfter(registry, Items.STRUCTURE_VOID, Items.OBSERVER, "observer");
        insertAfter(registry, Items.WARPED_DOOR, Items.REPEATER, "repeater");
        insertAfter(registry, Items.REPEATER, Items.COMPARATOR, "comparator");
        insertAfter(registry, Items.SADDLE, Items.REDSTONE, "redstone");
        insertAfter(registry, Items.GRINDSTONE, Items.LECTERN, "lectern");
        insertAfter(registry, Items.ANCIENT_DEBRIS, Items.TARGET, "target");
        insertAfter(registry, Items.DARK_OAK_STAIRS, Items.SLIME_BLOCK, "slime_block");
        insertAfter(registry, Items.HONEY_BOTTLE, Items.HONEY_BLOCK, "honey_block");
    }

    private void mutateEntityRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.AXOLOTL);
        registry.unregister(EntityType.GLOW_ITEM_FRAME);
        registry.unregister(EntityType.GLOW_SQUID);
    }

    private void mutateBlockEntityRegistry(ISimpleRegistry<BlockEntityType<?>> registry) {
        registry.unregister(BlockEntityType.SCULK_SENSOR);
    }

    private void mutateParticleTypeRegistry(ISimpleRegistry<ParticleType<?>> registry) {
        registry.unregister(ParticleTypes.SMALL_FLAME);
        registry.unregister(ParticleTypes.SNOWFLAKE);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_LAVA);
        registry.unregister(ParticleTypes.DRIPPING_DRIPSTONE_WATER);
        registry.unregister(ParticleTypes.FALLING_DRIPSTONE_WATER);
        registry.unregister(ParticleTypes.DUST_COLOR_TRANSITION);
        registry.unregister(ParticleTypes.VIBRATION);
        registry.unregister(ParticleTypes.GLOW_SQUID_INK);
        registry.unregister(ParticleTypes.GLOW);
        registry.unregister(ParticleTypes.FALLING_SPORE_BLOSSOM);
        registry.unregister(ParticleTypes.SPORE_BLOSSOM_AIR);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_BREAK);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_FALL);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_HIT);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_PLACE);
        registry.unregister(SoundEvents.BLOCK_AMETHYST_CLUSTER_STEP);
        registry.unregister(SoundEvents.BLOCK_CAKE_ADD_CANDLE);
        registry.unregister(SoundEvents.BLOCK_CALCITE_BREAK);
        registry.unregister(SoundEvents.BLOCK_CALCITE_FALL);
        registry.unregister(SoundEvents.BLOCK_CALCITE_HIT);
        registry.unregister(SoundEvents.BLOCK_CALCITE_PLACE);
        registry.unregister(SoundEvents.BLOCK_CALCITE_STEP);
        registry.unregister(SoundEvents.BLOCK_CANDLE_AMBIENT);
        registry.unregister(SoundEvents.BLOCK_CANDLE_BREAK);
        registry.unregister(SoundEvents.BLOCK_CANDLE_EXTINGUISH);
        registry.unregister(SoundEvents.BLOCK_CANDLE_FALL);
        registry.unregister(SoundEvents.BLOCK_CANDLE_HIT);
        registry.unregister(SoundEvents.BLOCK_CANDLE_PLACE);
        registry.unregister(SoundEvents.BLOCK_CANDLE_STEP);
        registry.unregister(SoundEvents.BLOCK_COPPER_BREAK);
        registry.unregister(SoundEvents.BLOCK_COPPER_FALL);
        registry.unregister(SoundEvents.BLOCK_COPPER_HIT);
        registry.unregister(SoundEvents.BLOCK_COPPER_PLACE);
        registry.unregister(SoundEvents.BLOCK_COPPER_STEP);
        registry.unregister(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_LARGE_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.BLOCK_MEDIUM_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_MEDIUM_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.ENTITY_MINECART_INSIDE_UNDERWATER);
        registry.unregister(SoundEvents.BLOCK_SMALL_AMETHYST_BUD_BREAK);
        registry.unregister(SoundEvents.BLOCK_SMALL_AMETHYST_BUD_PLACE);
        registry.unregister(SoundEvents.ITEM_SPYGLASS_USE);
        registry.unregister(SoundEvents.ITEM_SPYGLASS_STOP_USING);
        registry.unregister(SoundEvents.BLOCK_TUFF_BREAK);
        registry.unregister(SoundEvents.BLOCK_TUFF_FALL);
        registry.unregister(SoundEvents.BLOCK_TUFF_HIT);
        registry.unregister(SoundEvents.BLOCK_TUFF_PLACE);
        registry.unregister(SoundEvents.BLOCK_TUFF_STEP);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW);
        registry.unregister(SoundEvents.ENTITY_PLAYER_HURT_FREEZE);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_BREAK);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_FALL);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_HIT);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_PLACE);
        registry.unregister(SoundEvents.BLOCK_POWDER_SNOW_STEP);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_BREAK);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_STEP);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_PLACE);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_HIT);
        registry.unregister(SoundEvents.BLOCK_DRIPSTONE_BLOCK_FALL);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_BREAK);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_STEP);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_PLACE);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_HIT);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_FALL);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_LAND);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_LAVA_INTO_CAULDRON);
        registry.unregister(SoundEvents.BLOCK_POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_CLICKING_STOP);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_BREAK);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_FALL);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_HIT);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_PLACE);
        registry.unregister(SoundEvents.BLOCK_SCULK_SENSOR_STEP);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_ATTACK);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_DEATH);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_HURT);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_IDLE_AIR);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_IDLE_WATER);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_SPLASH);
        registry.unregister(SoundEvents.ENTITY_AXOLOTL_SWIM);
        registry.unregister(SoundEvents.ITEM_BUCKET_EMPTY_AXOLOTL);
        registry.unregister(SoundEvents.ITEM_BUCKET_FILL_AXOLOTL);
        registry.unregister(SoundEvents.ITEM_DYE_USE);
        registry.unregister(SoundEvents.ITEM_GLOW_INK_SAC_USE);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_DEATH);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_HURT);
        registry.unregister(SoundEvents.ENTITY_GLOW_SQUID_SQUIRT);
        registry.unregister(SoundEvents.ITEM_INK_SAC_USE);
        registry.unregister(SoundEvents.BLOCK_AZALEA_BREAK);
        registry.unregister(SoundEvents.BLOCK_AZALEA_FALL);
        registry.unregister(SoundEvents.BLOCK_AZALEA_HIT);
        registry.unregister(SoundEvents.BLOCK_AZALEA_PLACE);
        registry.unregister(SoundEvents.BLOCK_AZALEA_STEP);
        registry.unregister(SoundEvents.BLOCK_AZALEA_LEAVES_BREAK);
        registry.unregister(SoundEvents.BLOCK_AZALEA_LEAVES_FALL);
        registry.unregister(SoundEvents.BLOCK_AZALEA_LEAVES_HIT);
        registry.unregister(SoundEvents.BLOCK_AZALEA_LEAVES_PLACE);
        registry.unregister(SoundEvents.BLOCK_AZALEA_LEAVES_STEP);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_BREAK);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_FALL);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_HIT);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_PLACE);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_STEP);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_BREAK);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_FALL);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_HIT);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_PLACE);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_STEP);
        registry.unregister(SoundEvents.BLOCK_CAVE_VINES_PICK_BERRIES);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_DOWN);
        registry.unregister(SoundEvents.BLOCK_BIG_DRIPLEAF_TILT_UP);
        registry.unregister(SoundEvents.BLOCK_FLOWERING_AZALEA_BREAK);
        registry.unregister(SoundEvents.BLOCK_FLOWERING_AZALEA_FALL);
        registry.unregister(SoundEvents.BLOCK_FLOWERING_AZALEA_HIT);
        registry.unregister(SoundEvents.BLOCK_FLOWERING_AZALEA_PLACE);
        registry.unregister(SoundEvents.BLOCK_FLOWERING_AZALEA_STEP);
        registry.unregister(SoundEvents.BLOCK_HANGING_ROOTS_BREAK);
        registry.unregister(SoundEvents.BLOCK_HANGING_ROOTS_FALL);
        registry.unregister(SoundEvents.BLOCK_HANGING_ROOTS_HIT);
        registry.unregister(SoundEvents.BLOCK_HANGING_ROOTS_PLACE);
        registry.unregister(SoundEvents.BLOCK_HANGING_ROOTS_STEP);
        registry.unregister(SoundEvents.BLOCK_MOSS_CARPET_BREAK);
        registry.unregister(SoundEvents.BLOCK_MOSS_CARPET_FALL);
        registry.unregister(SoundEvents.BLOCK_MOSS_CARPET_HIT);
        registry.unregister(SoundEvents.BLOCK_MOSS_CARPET_PLACE);
        registry.unregister(SoundEvents.BLOCK_MOSS_CARPET_STEP);
        registry.unregister(SoundEvents.BLOCK_MOSS_BREAK);
        registry.unregister(SoundEvents.BLOCK_MOSS_FALL);
        registry.unregister(SoundEvents.BLOCK_MOSS_HIT);
        registry.unregister(SoundEvents.BLOCK_MOSS_PLACE);
        registry.unregister(SoundEvents.BLOCK_MOSS_STEP);
        registry.unregister(SoundEvents.BLOCK_ROOTED_DIRT_BREAK);
        registry.unregister(SoundEvents.BLOCK_ROOTED_DIRT_FALL);
        registry.unregister(SoundEvents.BLOCK_ROOTED_DIRT_HIT);
        registry.unregister(SoundEvents.BLOCK_ROOTED_DIRT_PLACE);
        registry.unregister(SoundEvents.BLOCK_ROOTED_DIRT_STEP);
        registry.unregister(SoundEvents.ENTITY_SKELETON_CONVERTED_TO_STRAY);
        registry.unregister(SoundEvents.BLOCK_SMALL_DRIPLEAF_BREAK);
        registry.unregister(SoundEvents.BLOCK_SMALL_DRIPLEAF_FALL);
        registry.unregister(SoundEvents.BLOCK_SMALL_DRIPLEAF_HIT);
        registry.unregister(SoundEvents.BLOCK_SMALL_DRIPLEAF_PLACE);
        registry.unregister(SoundEvents.BLOCK_SMALL_DRIPLEAF_STEP);
        registry.unregister(SoundEvents.BLOCK_SPORE_BLOSSOM_BREAK);
        registry.unregister(SoundEvents.BLOCK_SPORE_BLOSSOM_FALL);
        registry.unregister(SoundEvents.BLOCK_SPORE_BLOSSOM_HIT);
        registry.unregister(SoundEvents.BLOCK_SPORE_BLOSSOM_PLACE);
        registry.unregister(SoundEvents.BLOCK_SPORE_BLOSSOM_STEP);
        registry.unregister(SoundEvents.BLOCK_VINE_BREAK);
        registry.unregister(SoundEvents.BLOCK_VINE_FALL);
        registry.unregister(SoundEvents.BLOCK_VINE_HIT);
        registry.unregister(SoundEvents.BLOCK_VINE_PLACE);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BRICKS_BREAK);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BRICKS_FALL);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BRICKS_HIT);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BRICKS_PLACE);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BRICKS_STEP);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_BREAK);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_FALL);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_HIT);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_PLACE);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_STEP);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_TILES_BREAK);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_TILES_FALL);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_TILES_HIT);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_TILES_PLACE);
        registry.unregister(SoundEvents.BLOCK_DEEPSLATE_TILES_STEP);
        registry.unregister(SoundEvents.BLOCK_POLISHED_DEEPSLATE_BREAK);
        registry.unregister(SoundEvents.BLOCK_POLISHED_DEEPSLATE_FALL);
        registry.unregister(SoundEvents.BLOCK_POLISHED_DEEPSLATE_HIT);
        registry.unregister(SoundEvents.BLOCK_POLISHED_DEEPSLATE_PLACE);
        registry.unregister(SoundEvents.BLOCK_POLISHED_DEEPSLATE_STEP);
    }

    @Override
    protected Stream<BlockState> getStatesForBlock(Block block) {
        if (block == Blocks.CAULDRON) {
            return Stream.of(Blocks.CAULDRON.getDefaultState(),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 1),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 2),
                    Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3));
        }
        return super.getStatesForBlock(block);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() instanceof AbstractRailBlock && state.get(AbstractRailBlock.WATERLOGGED)) {
            return false;
        }
        if (state.getBlock() instanceof AbstractSignBlock && state.get(SignBlock.LIT)) {
            return false;
        }
        return super.acceptBlockState(state);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.CANDLES);
        tags.add(BlockTags.CANDLE_CAKES);
        tags.add(BlockTags.CAULDRONS, Blocks.CAULDRON, Blocks.WATER_CAULDRON);
        tags.add(BlockTags.CRYSTAL_SOUND_BLOCKS);
        tags.add(BlockTags.INSIDE_STEP_SOUND_BLOCKS, Blocks.SNOW);
        tags.addTag(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, BlockTags.BASE_STONE_OVERWORLD);
        tags.add(BlockTags.DRIPSTONE_REPLACEABLE_BLOCKS, Blocks.DIRT);
        tags.addTag(BlockTags.OCCLUDES_VIBRATION_SIGNALS, BlockTags.WOOL);
        tags.add(BlockTags.CAVE_VINES);
        tags.addTag(BlockTags.LUSH_PLANTS_REPLACEABLE, BlockTags.BASE_STONE_OVERWORLD);
        tags.addTag(BlockTags.LUSH_PLANTS_REPLACEABLE, BlockTags.CAVE_VINES);
        tags.addTag(BlockTags.LUSH_PLANTS_REPLACEABLE, BlockTags.FLOWERS);
        tags.add(BlockTags.LUSH_PLANTS_REPLACEABLE, Blocks.DIRT, Blocks.GRAVEL, Blocks.SAND, Blocks.GRASS, Blocks.TALL_GRASS, Blocks.VINE);
        tags.addTag(BlockTags.AZALEA_LOG_REPLACEABLE, BlockTags.FLOWERS);
        tags.addTag(BlockTags.AZALEA_LOG_REPLACEABLE, BlockTags.LEAVES);
        tags.add(BlockTags.AZALEA_LOG_REPLACEABLE, Blocks.GRASS, Blocks.FERN, Blocks.SWEET_BERRY_BUSH);
        tags.add(BlockTags.IRON_ORES, Blocks.IRON_ORE);
        tags.add(BlockTags.DIAMOND_ORES, Blocks.DIAMOND_ORE);
        tags.add(BlockTags.REDSTONE_ORES, Blocks.REDSTONE_ORE);
        tags.add(BlockTags.LAPIS_ORES, Blocks.LAPIS_ORE);
        tags.add(BlockTags.STONE_ORE_REPLACEABLES, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
        tags.add(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.IGNORED_BY_PIGLIN_BABIES, Items.LEATHER);
        tags.add(ItemTags.PIGLIN_FOOD, Items.PORKCHOP, Items.COOKED_PORKCHOP);
        copyBlocks(tags, blockTags, ItemTags.CANDLES, BlockTags.CANDLES);
        tags.add(ItemTags.FREEZE_IMMUNE_WEARABLES, Items.LEATHER_BOOTS, Items.LEATHER_LEGGINGS, Items.LEATHER_CHESTPLATE, Items.LEATHER_HELMET);
        tags.add(ItemTags.AXOLOTL_TEMPT_ITEMS, Items.TROPICAL_FISH, Items.TROPICAL_FISH_BUCKET);
        copyBlocks(tags, blockTags, ItemTags.OCCLUDES_VIBRATION_SIGNALS, BlockTags.OCCLUDES_VIBRATION_SIGNALS);
        tags.add(ItemTags.FOX_FOOD, Items.SWEET_BERRIES);
        copyBlocks(tags, blockTags, ItemTags.IRON_ORES, BlockTags.IRON_ORES);
        copyBlocks(tags, blockTags, ItemTags.DIAMOND_ORES, BlockTags.DIAMOND_ORES);
        copyBlocks(tags, blockTags, ItemTags.REDSTONE_ORES, BlockTags.REDSTONE_ORES);
        copyBlocks(tags, blockTags, ItemTags.LAPIS_ORES, BlockTags.LAPIS_ORES);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public void addExtraEntityTags(TagRegistry<EntityType<?>> tags) {
        tags.add(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS, EntityType.RABBIT, EntityType.ENDERMITE, EntityType.SILVERFISH);
        tags.add(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES, EntityType.TROPICAL_FISH, EntityType.PUFFERFISH, EntityType.SALMON, EntityType.COD, EntityType.SQUID, EntityType.GLOW_SQUID);
        tags.add(EntityTypeTags.AXOLOTL_TEMPTED_HOSTILES, EntityType.DROWNED, EntityType.GUARDIAN);
        super.addExtraEntityTags(tags);
    }

    @Override
    public void addExtraGameEventTags(TagRegistry<GameEvent> tags) {
        tags.add(GameEventTags.VIBRATIONS);
        tags.add(GameEventTags.IGNORE_VIBRATIONS_SNEAKING);
        super.addExtraGameEventTags(tags);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getFrozenTicks()) {
            return false;
        }
        if (clazz == ShulkerEntity.class && data == ShulkerEntityAccessor.getPeekAmount()) {
            DataTrackerManager.registerOldTrackedData(ShulkerEntity.class, OLD_SHULKER_ATTACHED_POSITION, Optional.empty(), (entity, pos) -> {});
        }
        return super.acceptEntityData(clazz, data);
    }
}
