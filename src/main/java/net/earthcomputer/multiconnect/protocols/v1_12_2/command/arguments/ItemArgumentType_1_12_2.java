package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.Item;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ItemArgumentType_1_12_2 implements ArgumentType<Item> {
    private static final DynamicCommandExceptionType ID_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.translatable("argument.item.id.invalid", id));
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
            throw ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        Item item = Registry.ITEM.get(id);
        if (!isValidItem(item)) {
            reader.setCursor(start);
            throw ID_INVALID_EXCEPTION.createWithContext(reader, id);
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
        return Registry.ITEM.getRawId(item) < 4096;
    }

}
