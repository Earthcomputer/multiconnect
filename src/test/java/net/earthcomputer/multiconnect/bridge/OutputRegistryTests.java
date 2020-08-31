package net.earthcomputer.multiconnect.bridge;

import net.minecraft.Bootstrap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class OutputRegistryTests {
    private static final Registry<?>[] REGISTRIES = {
            Registry.BLOCK,
            Registry.ITEM,
            Registry.ENCHANTMENT,
            Registry.ENTITY_TYPE,
            Registry.SOUND_EVENT,
            Registry.POTION,
            Registry.PARTICLE_TYPE,
            Registry.BLOCK_ENTITY_TYPE,
            Registry.SCREEN_HANDLER,
            Registry.RECIPE_SERIALIZER,
            Registry.CUSTOM_STAT
    };

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter the version prefix: ");
        String prefix = scan.nextLine();

        Bootstrap.initialize();

        for (Registry<?> registry : REGISTRIES) {
            String fileName = "src/test/resources/net/earthcomputer/multiconnect/bridge/" + prefix + "_" + getId(registry).getPath() + "s.txt";
            System.out.println("Writing " + fileName);
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName)))) {
                for (Object entry : registry) {
                    writer.println(getId(registry, entry).getPath());
                }
                writer.flush();
            }
        }

        String fileName = "src/test/resources/net/earthcomputer/multiconnect/bridge/" + prefix + "_blockstates.txt";
        System.out.println("Writing " + fileName);
        try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(Paths.get(fileName)))) {
            StreamSupport.stream(Block.STATE_IDS.spliterator(), false)
                    .collect(Collectors.groupingBy(BlockState::getBlock, LinkedHashMap::new, Collectors.counting()))
                    .forEach((block, count) -> writer.println(count + " " + Registry.BLOCK.getId(block).getPath()));
        }

        System.out.println("Done!");
    }

    @SuppressWarnings("unchecked")
    private static Identifier getId(Registry<?> registry) {
        return ((Registry<Registry<?>>) Registry.REGISTRIES).getId(registry);
    }

    @SuppressWarnings("unchecked")
    private static <T> Identifier getId(Registry<T> registry, Object entry) {
        return registry.getId((T) entry);
    }
}
