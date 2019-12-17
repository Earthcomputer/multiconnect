package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.PacketInfo;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.minecraft.block.BellBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.network.packet.PlayerActionResponseS2CPacket;
import net.minecraft.client.network.packet.SynchronizeTagsS2CPacket;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

import java.util.List;

public class Protocol_1_14_4 extends Protocol_1_15 {

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
        } else if (registry == Registry.BLOCK_ENTITY) {
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
}
