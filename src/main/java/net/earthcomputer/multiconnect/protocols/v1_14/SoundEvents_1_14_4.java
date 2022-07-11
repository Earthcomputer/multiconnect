package net.earthcomputer.multiconnect.protocols.v1_14;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class SoundEvents_1_14_4 {
    public static final SoundEvent ENTITY_PARROT_IMITATE_ENDERMAN = new SoundEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.enderman"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_PANDA = new SoundEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.panda"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_POLAR_BEAR = new SoundEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.polar_bear"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_WOLF = new SoundEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.wolf"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN = new SoundEvent(new ResourceLocation("multiconnect", "entity.parrot.imitate.zombie_pigman"));

    public static void register() {
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_ENDERMAN.getLocation(), ENTITY_PARROT_IMITATE_ENDERMAN);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_PANDA.getLocation(), ENTITY_PARROT_IMITATE_PANDA);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_POLAR_BEAR.getLocation(), ENTITY_PARROT_IMITATE_POLAR_BEAR);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_WOLF.getLocation(), ENTITY_PARROT_IMITATE_WOLF);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN.getLocation(), ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN);
    }
}
