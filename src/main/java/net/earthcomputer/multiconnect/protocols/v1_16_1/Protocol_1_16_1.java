package net.earthcomputer.multiconnect.protocols.v1_16_1;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.Utils;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.AbstractPiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.PiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_2.Protocol_1_16_2;
import net.earthcomputer.multiconnect.protocols.v1_16_4.Protocol_1_16_4;
import net.earthcomputer.multiconnect.transformer.Codecked;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.PiglinEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeCategoryOptionsC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.dimension.DimensionType;

import java.util.*;

public class Protocol_1_16_1 extends Protocol_1_16_2 {
    private static final TrackedData<Boolean> OLD_IMMUNE_TO_ZOMBIFICATION = DataTrackerManager.createOldTrackedData(TrackedDataHandlerRegistry.BOOLEAN);

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // player id
            buf.disablePassthroughMode();
            int gameTypeAndHardcore = buf.readUnsignedByte();
            int prevGameType = buf.readUnsignedByte();
            buf.pendingRead(Boolean.class, (gameTypeAndHardcore & 8) == 8); // hardcore
            buf.pendingRead(Byte.class, (byte) (gameTypeAndHardcore & ~8)); // game type
            buf.pendingRead(Byte.class, (byte) (prevGameType)); // prev game type
            buf.enablePassthroughMode();
            int dimensionCount = buf.readVarInt();
            for (int i = 0; i < dimensionCount; i++) {
                buf.readIdentifier(); // dimension id
            }
            buf.disablePassthroughMode();
            Codec<Set<Identifier>> dimensionSetCodec = Codec.list(Utils.singletonKeyCodec("name", Identifier.CODEC))
                    .xmap(ImmutableSet::copyOf, ImmutableList::copyOf);
            Utils.translateDynamicRegistries(
                    buf,
                    Utils.singletonKeyCodec("dimension", dimensionSetCodec),
                    ImmutableSet.of(new Identifier("overworld"), new Identifier("the_nether"), new Identifier("the_end"), new Identifier("overworld_caves"))::equals
            );
            Identifier dimTypeId = buf.readIdentifier();
            DynamicRegistryManager defaultRegistryManager = DynamicRegistryManager.create();
            DimensionType dimType = defaultRegistryManager.get(Registry.DIMENSION_TYPE_KEY).get(dimTypeId);
            if (dimType == null) dimType = defaultRegistryManager.get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.OVERWORLD_REGISTRY_KEY);
            DimensionType dimType_f = dimType;
            buf.pendingRead(Codecked.class, new Codecked<>(DimensionType.REGISTRY_CODEC, () -> dimType_f));
            buf.enablePassthroughMode();
            buf.readIdentifier(); // dimension
            buf.readLong(); // seed
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readUnsignedByte())); // max players
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlayerRespawnS2CPacket.class, buf -> {
            DynamicRegistryManager defaultRegistryManager = DynamicRegistryManager.create();
            Identifier dimTypeId = buf.readIdentifier();
            DimensionType dimType = defaultRegistryManager.get(Registry.DIMENSION_TYPE_KEY).get(dimTypeId);
            if (dimType == null) dimType = defaultRegistryManager.get(Registry.DIMENSION_TYPE_KEY).get(DimensionType.OVERWORLD_REGISTRY_KEY);
            DimensionType dimType_f = dimType;
            buf.pendingRead(Codecked.class, new Codecked<>(DimensionType.REGISTRY_CODEC, () -> dimType_f));
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // chunk x
            buf.readInt(); // chunk z
            boolean fullChunk = buf.readBoolean();
            buf.disablePassthroughMode();
            buf.readBoolean(); // forget old data
            if (!fullChunk) {
                buf.applyPendingReads();
                return;
            }
            buf.enablePassthroughMode();
            buf.readVarInt(); // vertical strip bitmask
            buf.readCompoundTag(); // heightmaps
            buf.disablePassthroughMode();
            int[] biomes = new int[Protocol_1_16_4.BIOME_ARRAY_LENGTH];
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = buf.readInt();
            }
            buf.pendingRead(int[].class, biomes);
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(UnlockRecipesS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readEnumConstant(UnlockRecipesS2CPacket.Action.class); // action
            buf.readBoolean(); // gui open
            buf.readBoolean(); // filtering craftable
            buf.readBoolean(); // furnace gui open
            buf.readBoolean(); // furnace filtering craftable
            buf.disablePassthroughMode();
            // These will be fixed by an on-thread mixin, to ensure a race condition doesn't happen with recipe book access
            buf.pendingRead(Boolean.class, false); // blast furnace gui open
            buf.pendingRead(Boolean.class, false); // blast furnace filtering craftable
            buf.pendingRead(Boolean.class, false); // smoker gui open
            buf.pendingRead(Boolean.class, false); // smoker filtering craftable
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, ChunkDeltaUpdateS2CPacket.class);
        insertAfter(packets, GameMessageS2CPacket.class, PacketInfo.of(ChunkDeltaUpdateS2CPacket_1_16_1.class, ChunkDeltaUpdateS2CPacket_1_16_1::new));
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        insertAfter(packets, RecipeBookDataC2SPacket.class, PacketInfo.of(RecipeBookDataC2SPacket_1_16_1.class, RecipeBookDataC2SPacket_1_16_1::new));
        remove(packets, RecipeBookDataC2SPacket.class);
        remove(packets, RecipeCategoryOptionsC2SPacket.class);
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet.getClass() == RecipeBookDataC2SPacket.class) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            assert networkHandler != null;
            networkHandler.sendPacket(new RecipeBookDataC2SPacket_1_16_1((RecipeBookDataC2SPacket) packet));
            return false;
        }
        if (packet.getClass() == RecipeCategoryOptionsC2SPacket.class) {
            ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
            assert networkHandler != null;
            networkHandler.sendPacket(new RecipeBookDataC2SPacket_1_16_1((RecipeCategoryOptionsC2SPacket) packet));
            return false;
        }
        return super.onSendPacket(packet);
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_16_1, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_16_1, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_16_1, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    @Override
    public void mutateDynamicRegistries(RegistryMutator mutator, DynamicRegistryManager.Impl registries) {
        super.mutateDynamicRegistries(mutator, registries);
        addRegistry(registries, Registry.DIMENSION_TYPE_KEY);
        addRegistry(registries, Registry.BIOME_KEY);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.unregister(Items.IRON_SHOVEL);
        registry.unregister(Items.IRON_PICKAXE);
        registry.unregister(Items.IRON_AXE);
        insertAfter(registry, Items.SCUTE, Items.IRON_SHOVEL, "iron_shovel");
        insertAfter(registry, Items.IRON_SHOVEL, Items.IRON_PICKAXE, "iron_pickaxe");
        insertAfter(registry, Items.IRON_PICKAXE, Items.IRON_AXE, "iron_axe");
        registry.unregister(Items.IRON_SWORD);
        insertAfter(registry, Items.NETHERITE_SCRAP, Items.IRON_SWORD, "iron_sword");
        registry.unregister(Items.DIAMOND_SWORD);
        registry.unregister(Items.DIAMOND_SHOVEL);
        registry.unregister(Items.DIAMOND_PICKAXE);
        registry.unregister(Items.DIAMOND_AXE);
        registry.unregister(Items.STICK);
        registry.unregister(Items.BOWL);
        registry.unregister(Items.MUSHROOM_STEW);
        insertAfter(registry, Items.STONE_AXE, Items.DIAMOND_SWORD, "diamond_sword");
        insertAfter(registry, Items.DIAMOND_SWORD, Items.DIAMOND_SHOVEL, "diamond_shovel");
        insertAfter(registry, Items.DIAMOND_SHOVEL, Items.DIAMOND_PICKAXE, "diamond_pickaxe");
        insertAfter(registry, Items.DIAMOND_PICKAXE, Items.DIAMOND_AXE, "diamond_axe");
        insertAfter(registry, Items.DIAMOND_AXE, Items.STICK, "stick");
        insertAfter(registry, Items.STICK, Items.BOWL, "bowl");
        insertAfter(registry, Items.BOWL, Items.MUSHROOM_STEW, "mushroom_stew");
        registry.unregister(Items.WOODEN_HOE);
        registry.unregister(Items.STONE_HOE);
        registry.unregister(Items.IRON_HOE);
        registry.unregister(Items.DIAMOND_HOE);
        registry.unregister(Items.GOLDEN_HOE);
        registry.unregister(Items.NETHERITE_HOE);
        insertAfter(registry, Items.GUNPOWDER, Items.WOODEN_HOE, "wooden_hoe");
        insertAfter(registry, Items.WOODEN_HOE, Items.STONE_HOE, "stone_hoe");
        insertAfter(registry, Items.STONE_HOE, Items.IRON_HOE, "iron_hoe");
        insertAfter(registry, Items.IRON_HOE, Items.DIAMOND_HOE, "diamond_hoe");
        insertAfter(registry, Items.DIAMOND_HOE, Items.GOLDEN_HOE, "golden_hoe");
        insertAfter(registry, Items.GOLDEN_HOE, Items.NETHERITE_HOE, "netherite_hoe");
        registry.unregister(Items.RED_DYE);
        registry.unregister(Items.GREEN_DYE);
        insertAfter(registry, Items.INK_SAC, Items.RED_DYE, "red_dye");
        insertAfter(registry, Items.RED_DYE, Items.GREEN_DYE, "green_dye");
        registry.unregister(Items.PURPLE_DYE);
        registry.unregister(Items.CYAN_DYE);
        registry.unregister(Items.LIGHT_GRAY_DYE);
        registry.unregister(Items.GRAY_DYE);
        registry.unregister(Items.PINK_DYE);
        registry.unregister(Items.LIME_DYE);
        registry.unregister(Items.YELLOW_DYE);
        registry.unregister(Items.LIGHT_BLUE_DYE);
        registry.unregister(Items.MAGENTA_DYE);
        insertAfter(registry, Items.LAPIS_LAZULI, Items.PURPLE_DYE, "purple_dye");
        insertAfter(registry, Items.PURPLE_DYE, Items.CYAN_DYE, "cyan_dye");
        insertAfter(registry, Items.CYAN_DYE, Items.LIGHT_GRAY_DYE, "light_gray_dye");
        insertAfter(registry, Items.LIGHT_GRAY_DYE, Items.GRAY_DYE, "gray_dye");
        insertAfter(registry, Items.GRAY_DYE, Items.PINK_DYE, "pink_dye");
        insertAfter(registry, Items.PINK_DYE, Items.LIME_DYE, "lime_dye");
        insertAfter(registry, Items.LIME_DYE, Items.YELLOW_DYE, "yellow_dye");
        insertAfter(registry, Items.YELLOW_DYE, Items.LIGHT_BLUE_DYE, "light_blue_dye");
        insertAfter(registry, Items.LIGHT_BLUE_DYE, Items.MAGENTA_DYE, "magenta_dye");
        registry.unregister(Items.BONE_MEAL);
        insertAfter(registry, Items.ORANGE_DYE, Items.BONE_MEAL, "bone_meal");
        registry.unregister(Items.WHITE_DYE);
        insertAfter(registry, Items.BLACK_DYE, Items.WHITE_DYE, "white_dye");
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.unregister(EntityType.PIGLIN_BRUTE);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_PIGLIN_BRUTE);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_ANGRY);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_CONVERTED_TO_ZOMBIFIED);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_DEATH);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_HURT);
        registry.unregister(SoundEvents.ENTITY_PIGLIN_BRUTE_STEP);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.MUSHROOM_GROW_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM);
        tags.add(BlockTags.BASE_STONE_OVERWORLD, Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE, Blocks.ANDESITE);
        tags.add(BlockTags.BASE_STONE_NETHER, Blocks.NETHERRACK, Blocks.BASALT, Blocks.BLACKSTONE);
        super.addExtraBlockTags(tags);
    }

    @Override
    public void addExtraItemTags(TagRegistry<Item> tags, TagRegistry<Block> blockTags) {
        tags.add(ItemTags.STONE_CRAFTING_MATERIALS, Items.COBBLESTONE, Items.BLACKSTONE);
        super.addExtraItemTags(tags, blockTags);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() instanceof LanternBlock && state.get(LanternBlock.WATERLOGGED)) {
            return false;
        }
        if (state.getBlock() == Blocks.CHAIN && state.get(ChainBlock.AXIS) != Direction.Axis.Y) {
            return false;
        }
        return super.acceptBlockState(state);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PiglinEntity.class && data == PiglinEntityAccessor.getCharging()) {
            DataTrackerManager.registerOldTrackedData(PiglinEntity.class, OLD_IMMUNE_TO_ZOMBIFICATION, false, AbstractPiglinEntity::setImmuneToZombification);
        }
        if (clazz == AbstractPiglinEntity.class && data == AbstractPiglinEntityAccessor.getImmuneToZombification()) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
