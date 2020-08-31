package net.earthcomputer.multiconnect.bridge;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.DefaultRegistries;
import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractRegistryTest {
    protected final String versionPrefix;

    protected AbstractRegistryTest(String versionPrefix) {
        this.versionPrefix = versionPrefix;
    }

    protected static void initialize(int protocol) {
        Bootstrap.initialize();
        Bootstrap.getMissingTranslations(); // forces initialization of translation keys
        DefaultRegistries.initialize();
        ProtocolRegistry.get(protocol).doRegistryMutation(false);
    }

    protected final void checkBlockStateIds() {
        List<String> blocks;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RegistryTest13.class.getResourceAsStream(versionPrefix + "_blockstates.txt")))) {
            blocks = reader.lines()
                    .filter(line -> !line.isEmpty())
                    .map(line -> line.split(" "))
                    .flatMap(line -> Collections.nCopies(Integer.parseInt(line[0]), line[1]).stream())
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read " + versionPrefix + "_blockstates.txt", e);
        }
        for (int id = 0; id < blocks.size(); id++) {
            int id_f = id;
            BlockState state = Block.STATE_IDS.get(id);
            assertNotNull(state, () -> "Missing block state at id " + id_f);
            Block block = state.getBlock();
            assertEquals(blocks.get(id), Registry.BLOCK.getId(block).getPath(), () -> "Block state at id " + id_f + " is wrong value of block " + block.getTranslationKey().replaceFirst("block.minecraft.", ""));
        }
    }

    protected final void testRegistryMatches(Registry<?> registry) {
        String file = versionPrefix + "_" + getId(registry).getPath() + "s.txt";
        List<String> ids;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(RegistryTest13.class.getResourceAsStream(file)))) {
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

    @SuppressWarnings("unchecked")
    private static Identifier getId(Registry<?> registry) {
        Identifier id = ((Registry<Registry<?>>) Registry.REGISTRIES).getId(registry);
        assert id != null;
        return id;
    }

    private static <T> Identifier getId(Registry<T> registry, int id) {
        return registry.getId(registry.get(id));
    }
}
