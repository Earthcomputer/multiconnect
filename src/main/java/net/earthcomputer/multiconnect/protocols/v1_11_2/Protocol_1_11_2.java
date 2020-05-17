package net.earthcomputer.multiconnect.protocols.v1_11_2;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.impl.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.v1_12.PlaceRecipeC2SPacket_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_13_2.Protocol_1_13_2;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.AdvancementTabC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientStatusC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.RecipeBookDataC2SPacket;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityS2CPacket;
import net.minecraft.network.packet.s2c.play.SelectAdvancementTabS2CPacket;
import net.minecraft.network.packet.s2c.play.UnlockRecipesS2CPacket;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Protocol_1_11_2 extends Protocol_1_12 {

    public static void registerTranslators() {

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
        remove(packets, RecipeBookDataC2SPacket.class);
        remove(packets, AdvancementTabC2SPacket.class);
        insertAfter(packets, ClientStatusC2SPacket.class, PacketInfo.of(ClientStatusC2SPacket_1_11_2.class, ClientStatusC2SPacket_1_11_2::new));
        remove(packets, ClientStatusC2SPacket.class);
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof PlaceRecipeC2SPacket_1_12) {
            return false;
        }
        if (packet instanceof RecipeBookDataC2SPacket) {
            return false;
        }
        if (packet instanceof AdvancementTabC2SPacket) {
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

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == IllagerEntity.class && data == Protocol_1_13_2.OLD_ILLAGER_FLAGS) {
            return false;
        }
        return super.acceptEntityData(clazz, data);
    }
}
