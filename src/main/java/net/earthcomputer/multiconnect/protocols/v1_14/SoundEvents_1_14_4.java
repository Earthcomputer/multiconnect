package net.earthcomputer.multiconnect.protocols.v1_14;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

// TODO: apply these sound events with via
public class SoundEvents_1_14_4 {
    public static final SoundEvent ENTITY_PARROT_IMITATE_ENDERMAN = SoundEvent.createVariableRangeEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.enderman"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_PANDA = SoundEvent.createVariableRangeEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.panda"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_POLAR_BEAR = SoundEvent.createVariableRangeEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.polar_bear"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_WOLF = SoundEvent.createVariableRangeEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.wolf"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN = SoundEvent.createVariableRangeEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.zombie_pigman"));

    public static void register() {
        Registry.register(BuiltInRegistries.SOUND_EVENT, ENTITY_PARROT_IMITATE_ENDERMAN.getLocation(), ENTITY_PARROT_IMITATE_ENDERMAN);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ENTITY_PARROT_IMITATE_PANDA.getLocation(), ENTITY_PARROT_IMITATE_PANDA);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ENTITY_PARROT_IMITATE_POLAR_BEAR.getLocation(), ENTITY_PARROT_IMITATE_POLAR_BEAR);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ENTITY_PARROT_IMITATE_WOLF.getLocation(), ENTITY_PARROT_IMITATE_WOLF);
        Registry.register(BuiltInRegistries.SOUND_EVENT, ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN.getLocation(), ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN);
    }
}
