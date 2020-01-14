package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.earthcomputer.multiconnect.protocols.v1_12_2.Items_1_12_2;
import net.minecraft.command.arguments.ItemStringReader;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandSource;
import net.minecraft.util.Identifier;
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
        Identifier id = Identifier.fromCommandInput(reader);
        if (!Registry.ITEM.containsId(id)) {
            reader.setCursor(start);
            throw ItemStringReader.ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        Item item = Registry.ITEM.get(id);
        if (!isValidItem(item)) {
            reader.setCursor(start);
            throw ItemStringReader.ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        return item;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandSource.suggestIdentifiers(Registry.ITEM.getIds().stream().filter(id -> isValidItem(Registry.ITEM.get(id))), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isValidItem(Item item) {
        return Items_1_12_2.newItemStackToOld(new ItemStack(item)).getLeft().getItem() == item;
    }

}
