package net.earthcomputer.multiconnect.bridge;

import net.earthcomputer.multiconnect.api.Protocols;
import net.minecraft.util.registry.Registry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class RegistryTest13 extends AbstractRegistryTest {
    protected RegistryTest13() {
        super(Protocols.V1_13, "1_13");
    }

    @BeforeAll
    public static void beforeAll() {
        initialize(Protocols.V1_13);
    }

    @Test
    public void testBlockRegistry() {
        testRegistryMatches(Registry.BLOCK);
    }

    @Test
    public void testItemRegistry() {
        testRegistryMatches(Registry.ITEM);
    }

    @Test
    public void testEnchantmentRegistry() {
        testRegistryMatches(Registry.ENCHANTMENT);
    }

    @Test
    public void testEntityRegistry() {
        testRegistryMatches(Registry.ENTITY_TYPE);
    }

    @Test
    public void testSoundRegistry() {
        testRegistryMatches(Registry.SOUND_EVENT);
    }

    @Test
    public void testPotionRegistry() {
        testRegistryMatches(Registry.POTION);
    }

    @Test
    public void testParticleRegistry() {
        testRegistryMatches(Registry.PARTICLE_TYPE);
    }

    @Test
    public void testBlockEntityRegistry() {
        testRegistryMatches(Registry.BLOCK_ENTITY_TYPE);
    }

    @Test
    public void testScreenHandlerRegistry() {
        testRegistryMatches(Registry.SCREEN_HANDLER);
    }

    @Test
    public void testRecipeSerializerRegistry() {
        testRegistryMatches(Registry.RECIPE_SERIALIZER);
    }

    @Test
    public void testCustomStatRegistry() {
        testRegistryMatches(Registry.CUSTOM_STAT);
    }

    @Test
    public void testBlockStateIds() {
        checkBlockStateIds();
    }
}
