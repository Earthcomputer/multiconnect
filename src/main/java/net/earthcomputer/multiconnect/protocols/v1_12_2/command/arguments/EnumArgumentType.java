package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandSource;
import net.minecraft.text.LiteralText;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public final class EnumArgumentType implements ArgumentType<String> {

    private static final Dynamic2CommandExceptionType INVALID_ENUM_EXCEPTION =
            new Dynamic2CommandExceptionType((actual, expected) -> new LiteralText("Invalid enum \"" + actual + "\", expected one of " + String.join(", ", (String[]) expected)));

    private final String[] values;
    private boolean caseSensitive = true;

    private EnumArgumentType(String[] values) {
        this.values = values;
    }

    public static EnumArgumentType enumArg(String... values) {
        return new EnumArgumentType(values);
    }

    public EnumArgumentType caseInsensitive() {
        caseSensitive = false;
        return this;
    }

    @Override
    public String parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        String val = reader.readUnquotedString();
        boolean found = false;
        if (caseSensitive) {
            for (String allowed : values) {
                if (val.equals(allowed)) {
                    found = true;
                    break;
                }
            }
        } else {
            for (String allowed : values) {
                if (val.equalsIgnoreCase(allowed)) {
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            reader.setCursor(start);
            throw INVALID_ENUM_EXCEPTION.createWithContext(reader, val, values);
        }
        return val;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandSource.suggestMatching(values, builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return Arrays.asList(values);
    }
}
