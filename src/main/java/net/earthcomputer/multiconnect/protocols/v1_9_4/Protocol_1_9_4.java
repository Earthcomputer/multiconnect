package net.earthcomputer.multiconnect.protocols.v1_9_4;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.ISimpleRegistry;
import net.earthcomputer.multiconnect.protocols.generic.PacketInfo;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_12_2.RecipeInfo;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.BrigadierRemover;
import net.earthcomputer.multiconnect.protocols.v1_9_4.mixin.EntityAccessor;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.item.Items;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ResourcePackStatusC2SPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundIdS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import java.util.List;
import java.util.Set;

public class Protocol_1_9_4 extends Protocol_1_10 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(PlaySoundIdS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readString(256); // id
            buf.readEnumConstant(SoundCategory.class); // category
            buf.readInt(); // x
            buf.readInt(); // y
            buf.readInt(); // z
            buf.readFloat(); // volume
            buf.disablePassthroughMode();
            buf.pendingRead(Float.class, buf.readUnsignedByte() / 63f); // pitch
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(PlaySoundS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // sound id
            buf.readEnumConstant(SoundCategory.class); // category
            buf.readInt(); // x
            buf.readInt(); // y
            buf.readInt(); // z
            buf.readFloat(); // volume
            buf.disablePassthroughMode();
            buf.pendingRead(Float.class, buf.readUnsignedByte() / (ConnectionInfo.protocolVersion <= Protocols.V1_9_2 ? 63.5f : 63f)); // pitch
            buf.applyPendingReads();
        });
    }

    @Override
    public List<PacketInfo<?>> getServerboundPackets() {
        List<PacketInfo<?>> packets = super.getServerboundPackets();
        insertAfter(packets, ResourcePackStatusC2SPacket.class, PacketInfo.of(ResourcePackStatusC2SPacket_1_9_4.class, ResourcePackStatusC2SPacket_1_9_4::new));
        remove(packets, ResourcePackStatusC2SPacket.class);
        return packets;
    }

    @Override
    public boolean onSendPacket(Packet<?> packet) {
        if (packet instanceof ResourcePackStatusC2SPacket) {
            return false;
        }
        return super.onSendPacket(packet);
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_9_4, Registry.BLOCK, this::mutateBlockRegistry);
        mutator.mutate(Protocols.V1_9_4, Registry.ENTITY_TYPE, this::mutateEntityTypeRegistry);
        mutator.mutate(Protocols.V1_9_4, Registry.SOUND_EVENT, this::mutateSoundEventRegistry);
    }

    private void mutateBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.purge(Blocks.MAGMA_BLOCK);
        registry.purge(Blocks.NETHER_WART_BLOCK);
        registry.purge(Blocks.RED_NETHER_BRICKS);
        registry.purge(Blocks.BONE_BLOCK);
        registry.purge(Blocks.STRUCTURE_VOID);
    }

    private void mutateEntityTypeRegistry(ISimpleRegistry<EntityType<?>> registry) {
        registry.purge(EntityType.POLAR_BEAR);
    }

    private void mutateSoundEventRegistry(ISimpleRegistry<SoundEvent> registry) {
        registry.unregister(SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE);
        registry.unregister(SoundEvents.ENTITY_HUSK_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_HUSK_DEATH);
        registry.unregister(SoundEvents.ENTITY_HUSK_HURT);
        registry.unregister(SoundEvents.ENTITY_HUSK_STEP);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_AMBIENT_BABY);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_DEATH);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_HURT);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_STEP);
        registry.unregister(SoundEvents.ENTITY_POLAR_BEAR_WARNING);
        registry.unregister(SoundEvents.ENTITY_STRAY_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_STRAY_DEATH);
        registry.unregister(SoundEvents.ENTITY_STRAY_HURT);
        registry.unregister(SoundEvents.ENTITY_STRAY_STEP);
        registry.unregister(SoundEvents.ENTITY_WITHER_SKELETON_AMBIENT);
        registry.unregister(SoundEvents.ENTITY_WITHER_SKELETON_DEATH);
        registry.unregister(SoundEvents.ENTITY_WITHER_SKELETON_HURT);
        registry.unregister(SoundEvents.ENTITY_WITHER_SKELETON_STEP);
    }

    @Override
    public List<RecipeInfo<?>> getRecipes() {
        List<RecipeInfo<?>> recipes = super.getRecipes();
        recipes.removeIf(recipe -> recipe.getOutput().getItem() == Items.BONE_BLOCK);
        return recipes;
    }

    @Override
    public void registerCommands(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        super.registerCommands(dispatcher, serverCommands);
        BrigadierRemover.of(dispatcher).get("teleport").remove();
    }

    @Override
    public boolean acceptEntityData(Class<? extends Entity> clazz, TrackedData<?> data) {
        if (clazz == Entity.class && data == EntityAccessor.getNoGravity()) {
            return false;
        }
        if (clazz == AreaEffectCloudEntity.class) {
            if (data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM1 || data == Protocol_1_12_2.OLD_AREA_EFFECT_CLOUD_PARTICLE_PARAM2) {
                return false;
            }
        }
        return super.acceptEntityData(clazz, data);
    }
}
