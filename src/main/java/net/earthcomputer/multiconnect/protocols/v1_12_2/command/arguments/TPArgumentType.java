package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.server.command.CommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class TPArgumentType implements ArgumentType<Custom_1_12_Argument> {

    private EntityArgumentType_1_12_2 victim = EntityArgumentType_1_12_2.entities().noSuggestPlayerNames();
    private EntityArgumentType_1_12_2 target = EntityArgumentType_1_12_2.oneEntity().noSuggestPlayerNames();
    private BlockPosArgumentType blockPos = BlockPosArgumentType.blockPos();
    private RotationArgumentType rotation = RotationArgumentType.rotation();

    private TPArgumentType() {
    }

    public static TPArgumentType tp() {
        return new TPArgumentType();
    }

    @Override
    public Custom_1_12_Argument parse(StringReader reader) throws CommandSyntaxException {
        List<ParsedArgument<?, ?>> result = new ArrayList<>();

        String[] args = reader.getRemaining().split(" ", -1);
        if (args.length == 2 || args.length == 4 || args.length == 6) {
            int start = reader.getCursor();
            victim.parse(reader);
            result.add(new ParsedArgument<>(start, reader.getCursor(), null));
            reader.expect(' ');
        }

        if (args.length != 1 && args.length != 2) {
            int start = reader.getCursor();
            blockPos.parse(reader);
            result.add(new ParsedArgument<>(start, reader.getCursor(), null));
            if (reader.canRead()) {
                reader.expect(' ');
                start = reader.getCursor();
                rotation.parse(reader);
                result.add(new ParsedArgument<>(start, reader.getCursor(), null));
            }
        } else {
            int start = reader.getCursor();
            target.parse(reader);
            result.add(new ParsedArgument<>(start, reader.getCursor(), null));
        }

        return new Custom_1_12_Argument(result);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<CompletableFuture<Suggestions>> suggestions = new ArrayList<>();

        String[] args = builder.getRemaining().split(" ", -1);
        if (args.length == 1) {
            suggestions.add(blockPos.listSuggestions(context, builder.restart()));
            suggestions.add(victim.listSuggestions(context, builder.restart()));
            suggestions.add(target.listSuggestions(context, builder.restart()));
        } else {
            StringReader reader = new StringReader(builder.getInput());
            reader.setCursor(builder.getStart());

            boolean victimPresent = !isCoordinateArg(reader);
            if (victimPresent) {
                reader.setCursor(builder.getStart());
                try {
                    victim.parse(reader);
                } catch (CommandSyntaxException e) {
                    return builder.buildFuture();
                }
                if (reader.peek() != ' ')
                    return builder.buildFuture();
            }
            reader.skip();
            if (args.length == 2) {
                if (victimPresent) {
                    suggestions.add(blockPos.listSuggestions(context, builder.createOffset(reader.getCursor())));
                    suggestions.add(target.listSuggestions(context, builder.createOffset(reader.getCursor())));
                } else {
                    suggestions.add(blockPos.listSuggestions(context, builder.restart()));
                }
            } else {
                int argStart = victimPresent ? reader.getCursor() : builder.getStart();
                if (!isCoordinateArg(reader))
                    return builder.buildFuture();
                if (args.length <= (victimPresent ? 4 : 3)) {
                    suggestions.add(blockPos.listSuggestions(context, builder.createOffset(argStart)));
                } else {
                    if (victimPresent) {
                        reader.skip();
                        isCoordinateArg(reader);
                    }
                    reader.skip();
                    isCoordinateArg(reader);
                    reader.skip();
                    suggestions.add(CommandSource.suggestMatching(new String[] {"~", "~ ~"}, builder.createOffset(reader.getCursor())));
                }
            }
        }

        return CompletableFuture.allOf(suggestions.toArray(new CompletableFuture[0]))
                .thenApply(v -> Suggestions.merge(builder.getInput(), suggestions.stream().map(CompletableFuture::join).collect(Collectors.toList())));
    }

    private static boolean isCoordinateArg(StringReader reader) {
        if (reader.peek() == '~') {
            reader.skip();
            if (reader.peek() == ' ')
                return true;
        }
        try {
            reader.readDouble();
        } catch (CommandSyntaxException e) {
            return false;
        }
        return reader.peek() == ' ';
    }
}
