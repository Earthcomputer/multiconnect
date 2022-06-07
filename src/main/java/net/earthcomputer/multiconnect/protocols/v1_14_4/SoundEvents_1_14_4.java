package net.earthcomputer.multiconnect.protocols.v1_14_4;

import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class SoundEvents_1_14_4 {
    public static final SoundEvent ENTITY_PARROT_IMITATE_ENDERMAN = new SoundEvent(new Identifier("multiconnect", "entity.parrot.imitate.enderman"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_PANDA = new SoundEvent(new Identifier("multiconnect", "entity.parrot.imitate.panda"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_POLAR_BEAR = new SoundEvent(new Identifier("multiconnect", "entity.parrot.imitate.polar_bear"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_WOLF = new SoundEvent(new Identifier("multiconnect", "entity.parrot.imitate.wolf"));
    public static final SoundEvent ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN = new SoundEvent(new Identifier("multiconnect", "entity.parrot.imitate.zombie_pigman"));

    public static void register() {
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_ENDERMAN.getId(), ENTITY_PARROT_IMITATE_ENDERMAN);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_PANDA.getId(), ENTITY_PARROT_IMITATE_PANDA);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_POLAR_BEAR.getId(), ENTITY_PARROT_IMITATE_POLAR_BEAR);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_WOLF.getId(), ENTITY_PARROT_IMITATE_WOLF);
        Registry.register(Registry.SOUND_EVENT, ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN.getId(), ENTITY_PARROT_IMITATE_ZOMBIE_PIGMAN);
    }
}
