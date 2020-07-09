package net.earthcomputer.multiconnect.protocols.v1_16_1;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.AbstractPiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_1.mixin.PiglinEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_16_2.Protocol_1_16_2;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.class_5455;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
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
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.recipe.book.RecipeBookOptions;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.source.BiomeArray;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

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
            try {
                buf.decode(class_5455.class_5457.field_25923);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            buf.readIdentifier(); // dimension type
            buf.readIdentifier(); // dimension
            buf.readLong(); // seed
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readUnsignedByte())); // max players
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(ChunkDataS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // chunk x
            buf.readInt(); // chunk z
            boolean fullChunk = buf.readBoolean();
            if (!fullChunk) {
                buf.disablePassthroughMode();
                buf.applyPendingReads();
                return;
            }
            buf.readBoolean(); // forget old data
            buf.readVarInt(); // vertical strip bitmask
            buf.readCompoundTag(); // heightmaps
            buf.disablePassthroughMode();
            int[] biomes = new int[BiomeArray.DEFAULT_LENGTH];
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
            boolean blastFurnaceGuiOpen, blastFurnaceFilteringCraftable, smokerGuiOpen, smokerFilteringCraftable;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                RecipeBookOptions bookOptions = player.getRecipeBook().getOptions();
                blastFurnaceGuiOpen = bookOptions.isGuiOpen(RecipeBookCategory.BLAST_FURNACE);
                blastFurnaceFilteringCraftable = bookOptions.isFilteringCraftable(RecipeBookCategory.BLAST_FURNACE);
                smokerGuiOpen = bookOptions.isGuiOpen(RecipeBookCategory.SMOKER);
                smokerFilteringCraftable = bookOptions.isFilteringCraftable(RecipeBookCategory.SMOKER);
            } else {
                blastFurnaceGuiOpen = blastFurnaceFilteringCraftable = smokerGuiOpen = smokerFilteringCraftable = false;
            }
            buf.pendingRead(Boolean.class, blastFurnaceGuiOpen);
            buf.pendingRead(Boolean.class, blastFurnaceFilteringCraftable);
            buf.pendingRead(Boolean.class, smokerGuiOpen);
            buf.pendingRead(Boolean.class, smokerFilteringCraftable);
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
    public ChunkData createChunkData() {
        return new ChunkData_1_16_1();
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_16_1, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_16_1, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
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
