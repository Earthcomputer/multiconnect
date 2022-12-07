package net.earthcomputer.multiconnect.protocols.v1_12.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class ItemArgumentType_1_12_2 implements ArgumentType<Item> {
    private static final DynamicCommandExceptionType ID_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Component.translatable("argument.item.id.invalid", id));
    private static final Collection<String> EXAMPLES = Arrays.asList("stick", "minecraft:stick");

    private ItemArgumentType_1_12_2() {}

    public static ItemArgumentType_1_12_2 item() {
        return new ItemArgumentType_1_12_2();
    }

    @Override
    public Item parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        ResourceLocation id = ResourceLocation.read(reader);
        if (!BuiltInRegistries.ITEM.containsKey(id)) {
            reader.setCursor(start);
            throw ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        Item item = BuiltInRegistries.ITEM.get(id);
        if (!isValidItem(item)) {
            reader.setCursor(start);
            throw ID_INVALID_EXCEPTION.createWithContext(reader, id);
        }
        return item;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggestResource(BuiltInRegistries.ITEM.keySet().stream().filter(id -> isValidItem(BuiltInRegistries.ITEM.get(id))), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    private static boolean isValidItem(Item item) {
        return BuiltInRegistries.ITEM.getId(item) < 4096;
    }

}
