package net.earthcomputer.multiconnect.bridge;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings({"MethodCallSideOnly", "VariableUseSideOnly"})
public class RegistryTest {
    @BeforeAll
    public static void beforeAll() {
        Bootstrap.initialize();
        Bootstrap.getMissingTranslations(); // forces initialization of translation keys
        DefaultRegistries.initialize();
        ProtocolRegistry.get(Protocols.V1_13).doRegistryMutation(false);
    }

    @Test
    public void testBlockRegistry() {
        testRegistryMatches(Registry.BLOCK, "1_13_blocks.txt");
    }

    @Test
    public void testItemRegistry() {
        testRegistryMatches(Registry.ITEM, "1_13_items.txt");
    }

    @Test
    public void testEnchantmentRegistry() {
        testRegistryMatches(Registry.ENCHANTMENT, "1_13_enchantments.txt");
    }

    @Test
    public void testEntityRegistry() {
        testRegistryMatches(Registry.ENTITY_TYPE, "1_13_entities.txt");
    }

    @Test
    public void testSoundRegistry() {
        testRegistryMatches(Registry.SOUND_EVENT, "1_13_sounds.txt");
    }

    @Test
    public void testPotionRegistry() {
        testRegistryMatches(Registry.POTION, "1_13_potions.txt");
    }

    @Test
    public void testParticleRegistry() {
        testRegistryMatches(Registry.PARTICLE_TYPE, "1_13_particles.txt");
    }

    @Test
    public void testBlockEntityRegistry() {
        testRegistryMatches(Registry.BLOCK_ENTITY_TYPE, "1_13_block_entities.txt");
    }

    @Test
    public void testScreenHandlerRegistry() {
        testRegistryMatches(Registry.SCREEN_HANDLER, "1_13_screen_handlers.txt");
    }

    @Test
    public void testRecipeSerializerRegistry() {
        testRegistryMatches(Registry.RECIPE_SERIALIZER, "1_13_recipe_serializers.txt");
    }

    @Test
    public void testCustomStatRegistry() {
        testRegistryMatches(Registry.CUSTOM_STAT, "1_13_custom_stats.txt");
    }

    @Test
    public void testBlockStateIds() {
        List<String> blocks;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RegistryTest.class.getResourceAsStream("1_13_blockstates.txt")))) {
            blocks = reader.lines()
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(" "))
                    .flatMap(line -> Collections.nCopies(Integer.parseInt(line[0]), line[1]).stream())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read 1_13_blockstates.txt", e);
        }
        for (int id = 0; id < blocks.size(); id++) {
            int id_f = id;
            BlockState state = Block.STATE_IDS.get(id);
            assertNotNull(state, () -> "Missing block state at id " + id_f);
            Block block = state.getBlock();
            assertEquals(blocks.get(id), Registry.BLOCK.getId(block).getPath(), () -> "Block state at id " + id_f + " is wrong value of block " + block.getTranslationKey().replaceFirst("block.minecraft.", ""));
        }
    }

    private void testRegistryMatches(Registry<?> registry, String file) {
        List<String> ids;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RegistryTest.class.getResourceAsStream(file)))) {
            ids = reader.lines().filter(line -> !line.isEmpty()).collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + file, e);
        }
        int registrySize = registry.getIds().size();
        for (int id = 0; id < ids.size() && id < registrySize; id++) {
            Identifier identifier = getId(registry, id);
            int id_f = id;
            assertNotNull(identifier, () -> "Registry does not contain value at id " + id_f);
            assertEquals(ids.get(id), identifier.getPath(), () -> "Registry contains wrong value at id " + id_f);
        }
        assertTrue(registrySize >= ids.size(), () -> "Registry does not contain value " + ids.get(registrySize));
        assertEquals(ids.size(), registry.getIds().size(), "Registry contains excess values");
    }

    private static <T> Identifier getId(Registry<T> registry, int id) {
        return registry.getId(registry.get(id));
    }
}
