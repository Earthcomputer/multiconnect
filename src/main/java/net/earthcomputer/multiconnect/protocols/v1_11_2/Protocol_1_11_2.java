package net.earthcomputer.multiconnect.protocols.v1_11_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_11_2.mixin.PlayerEntityAccessor;
import net.earthcomputer.multiconnect.protocols.v1_12.PlaceRecipeC2SPacket_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14_4.SoundEvents_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_16_1.RecipeBookDataC2SPacket_1_16_1;
import net.minecraft.advancement.Advancement;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Set;

public class Protocol_1_11_2 extends Protocol_1_12 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(StatisticsS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            int count = buf.readVarInt();
            for (int i = 0; i < count; i++) {
                String stat = buf.readString(32767);
                int value = buf.readVarInt();
                if (stat.startsWith("achievement.")) {
                    String achievementId = stat.substring("achievement.".length());
                    Advancement achievement = Achievements_1_11_2.ACHIEVEMENTS.get(achievementId);
                    if (achievement != null) {
                        if (value == 0) {
                            PendingAchievements.takeAchievement(achievement);
                        } else {
                            PendingAchievements.giveAchievement(achievement);
                        }
                    }
                    // invalid stat will be removed by 1.12.2 <-> 1.13 translator
                }
            }
            buf.disablePassthroughMode();
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getClientboundPackets() {
        List<PacketInfo<?>> packets = super.getClientboundPackets();
        remove(packets, EntityS2CPacket.class);
        insertAfter(packets, EntityS2CPacket.Rotate.class, PacketInfo.of(EntityS2CPacket.class, EntityS2CPacket::new));
        remove(packets, UnlockRecipesS2CPacket.class);
        remove(packets, SelectAdvancementTabS2CPacket.class);
        remove(packets, AdvancementUpdateS2CPacket.class);
        return packets;
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        remove(packets, PlaceRecipeC2SPacket_1_12.class);
        remove(packets, PlayerMoveC2SPacket.class);
        insertAfter(packets, PlayerMoveC2SPacket.LookOnly.class, PacketInfo.of(PlayerMoveC2SPacket.class, PlayerMoveC2SPacket::new));
        remove(packets, RecipeBookDataC2SPacket_1_16_1.class);
        remove(packets, AdvancementTabC2SPacket.class);
        insertAfter(packets, ClientStatusC2SPacket.class, PacketInfo.of(ClientStatusC2SPacket_1_11_2.class, ClientStatusC2SPacket_1_11_2::new));
        remove(packets, ClientStatusC2SPacket.class);
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof PlaceRecipeC2SPacket_1_12) {
            PlayerEntity player = MinecraftClient.getInstance().player;
            assert player != null;
            RecipeBookEmulator recipeBookEmulator = ((IScreenHandler) player.currentScreenHandler).multiconnect_getRecipeBookEmulator();
            recipeBookEmulator.emulateRecipePlacement((PlaceRecipeC2SPacket_1_12) packet);
            return false;
        }
        if (packet instanceof RecipeBookDataC2SPacket_1_16_1) {
            return false;
        }
        if (packet instanceof AdvancementTabC2SPacket) {
            AdvancementTabC2SPacket advancementTabPacket = (AdvancementTabC2SPacket) packet;
            if (advancementTabPacket.getAction() == AdvancementTabC2SPacket.Action.OPENED_TAB) {
                assert MinecraftClient.getInstance().getNetworkHandler() != null;
                MinecraftClient.getInstance().getNetworkHandler().onSelectAdvancementTab(new SelectAdvancementTabS2CPacket(advancementTabPacket.getTabToOpen()));
            }
            return false;
        }
        if (packet instanceof ClientStatusC2SPacket) {
            assert MinecraftClient.getInstance().getNetworkHandler() != null;
            MinecraftClient.getInstance().getNetworkHandler().sendPacket(new ClientStatusC2SPacket_1_11_2((ClientStatusC2SPacket) packet));
            return false;
        }
        return super.onSendPacket(packet);
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_11_2, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_11_2, Registry.ITEM, this::mutateItemRegistry);
        mutator.mutate(Protocols.V1_11_2, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_11_2, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.purge(Blocks.WHITE_GLAZED_TERRACOTTA);
        registry.purge(Blocks.ORANGE_GLAZED_TERRACOTTA);
        registry.purge(Blocks.MAGENTA_GLAZED_TERRACOTTA);
        registry.purge(Blocks.LIGHT_BLUE_GLAZED_TERRACOTTA);
        registry.purge(Blocks.YELLOW_GLAZED_TERRACOTTA);
        registry.purge(Blocks.LIME_GLAZED_TERRACOTTA);
        registry.purge(Blocks.PINK_GLAZED_TERRACOTTA);
        registry.purge(Blocks.GRAY_GLAZED_TERRACOTTA);
        registry.purge(Blocks.LIGHT_GRAY_GLAZED_TERRACOTTA);
        registry.purge(Blocks.CYAN_GLAZED_TERRACOTTA);
        registry.purge(Blocks.PURPLE_GLAZED_TERRACOTTA);
        registry.purge(Blocks.BLUE_GLAZED_TERRACOTTA);
        registry.purge(Blocks.BROWN_GLAZED_TERRACOTTA);
        registry.purge(Blocks.GREEN_GLAZED_TERRACOTTA);
        registry.purge(Blocks.RED_GLAZED_TERRACOTTA);
        registry.purge(Blocks.BLACK_GLAZED_TERRACOTTA);
        registry.purge(Blocks.WHITE_CONCRETE);
        registry.purge(Blocks.WHITE_CONCRETE_POWDER);
    }

    private void mutateItemRegistry(ISimpleRegistry<Item> registry) {
        registry.purge(Items.KNOWLEDGE_BOOK);
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.purge(EntityType.ILLUSIONER);
        registry.purge(EntityType.PARROT);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.ENTITY_BOAT_PADDLE_LAND);
        registry.unregister(SoundEvents.ENTITY_BOAT_PADDLE_WATER);
        registry.unregister(SoundEvents.ENTITY_FISHING_BOBBER_RETRIEVE);
        registry.unregister(SoundEvents.ENTITY_ENDER_EYE_DEATH);
        registry.unregister(SoundEvents.BLOCK_END_PORTAL_FRAME_FILL);
        registry.unregister(SoundEvents.BLOCK_END_PORTAL_SPAWN);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_CAST_SPELL);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_DEATH);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_HURT);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_MIRROR_MOVE);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_PREPARE_BLINDNESS);
        registry.unregister(SoundEvents.ENTITY_ILLUSIONER_PREPARE_MIRROR);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_BELL);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_CHIME);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_FLUTE);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_GUITAR);
        registry.unregister(SoundEvents.BLOCK_NOTE_BLOCK_XYLOPHONE);
        registry.unregister(SoundEvents.ENTITY_PARROT_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_PARROT_DEATH);
        registry.unregister(SoundEvents.ENTITY_PARROT_EAT);
        registry.unregister(SoundEvents.ENTITY_PARROT_FLY);
        registry.unregister(SoundEvents.ENTITY_PARROT_HURT);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_BLAZE);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_CREEPER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ELDER_GUARDIAN);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ENDER_DRAGON);
        registry.unregister(SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_ENDERMAN);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ENDERMITE);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_EVOKER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_GHAST);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_HUSK);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ILLUSIONER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_MAGMA_CUBE);
        registry.unregister(SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_POLAR_BEAR);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_SHULKER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_SILVERFISH);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_SKELETON);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_SLIME);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_SPIDER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_STRAY);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_VEX);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_VINDICATOR);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_WITCH);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_WITHER);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_WITHER_SKELETON);
        registry.unregister(SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_WOLF);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE);
        registry.unregister(SoundEvents_1_14_4.ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN);
        registry.unregister(SoundEvents.ENTITY_PARROT_IMITATE_ZOMBIE_VILLAGER);
        registry.unregister(SoundEvents.ENTITY_PARROT_STEP);
        registry.unregister(SoundEvents.ENTITY_PLAYER_HURT_DROWN);
        registry.unregister(SoundEvents.ENTITY_PLAYER_HURT_ON_FIRE);
        registry.unregister(SoundEvents.UI_TOAST_IN);
        registry.unregister(SoundEvents.UI_TOAST_OUT);
        registry.unregister(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == PlayerEntity.class && (data == PlayerEntityAccessor.getLeftShoulderEntity() || data == PlayerEntityAccessor.getRightShoulderEntity())) {
            return false;
        }
        if (clazz == IllagerEntity.class && data == Protocol_1_13_2.OLD_ILLAGER_FLAGS) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }

    @Override
    public List<RecipeInfo<?>> getCraftingRecipes() {
        List<RecipeInfo<?>> recipes = super.getCraftingRecipes();
        recipes.removeIf(recipe -> {
            if (recipe.getOutput().getItem() instanceof BlockItem && ((BlockItem) recipe.getOutput().getItem()).getBlock() instanceof ConcretePowderBlock) {
                return true;
            }
            return false;
        });
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);

        BrigadierRemover.of(dispatcher).get("advancement").remove();
        BrigadierRemover.of(dispatcher).get("function").remove();
        BrigadierRemover.of(dispatcher).get("recipe").remove();
        BrigadierRemover.of(dispatcher).get("reload").remove();

        Commands_1_12_2.registerVanilla(dispatcher, serverCommands, "achievement", AchievementCommand::register);
    }
}
