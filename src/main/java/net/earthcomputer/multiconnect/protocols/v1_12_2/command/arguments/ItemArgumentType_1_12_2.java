package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ItemParser;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemArgumentType_1_12_2 implements ArgumentType<Item> {

    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick");

    private ItemArgumentType_1_12_2() {}

    public static ItemArgumentType_1_12_2 item() {
        return new ItemArgumentType_1_12_2();
    }

    @Override
    public Item parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        ResourceLocation id = ResourceLocation.read(reader);
        if (!Registry.ITEM.containsKey(id)) {
            reader.setCursor(start);
            throw ItemParser.ITEM_BAD_ID.createWithContext(reader, id);
        }
        Item item = Registry.ITEM.getOrDefault(id);
        if (!isValidItem(item)) {
            reader.setCursor(start);
            throw ItemParser.ITEM_BAD_ID.createWithContext(reader, id);
        }
        return item;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ISuggestionProvider.func_212476_a(Registry.ITEM.keySet().stream().filter(id -> isValidItem(Registry.ITEM.getOrDefault(id))), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isValidItem(Item item) {
        return Registry.ITEM.getId(item) < 4096;
    }

}
