package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

// This allows what is effectively a redirect to the root but, like other argument types, allows literal siblings
public final class CommandArgumentType implements ArgumentType<Custom_1_12_Argument> {

    private final CommandDispatcher<?> dispatcher;

    private CommandArgumentType(CommandDispatcher<?> dispatcher) {
        this.dispatcher = dispatcher;
    }

    public static CommandArgumentType command(CommandDispatcher<?> dispatcher) {
        return new CommandArgumentType(dispatcher);
    }

    @Override
    public Custom_1_12_Argument parse(StringReader reader) throws CommandSyntaxException {
        ParseResults<?> results = dispatcher.parse(reader, null);
        reader.setCursor(results.getReader().getCursor());
        if (reader.canRead()) {
            if (results.getExceptions().size() == 1) {
                throw results.getExceptions().values().iterator().next();
            } else if (results.getContext().getRange().isEmpty()) {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand().createWithContext(reader);
            } else {
                throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().createWithContext(reader);
            }
        }
        return new Custom_1_12_Argument(new ArrayList<>(results.getContext().getArguments().values()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CommandDispatcher<S> dispatcher = ((CommandDispatcher<S>) this.dispatcher);
        StringReader reader = new StringReader(builder.getInput());
        reader.setCursor(builder.getStart());
        ParseResults<S> results = dispatcher.parse(reader, context.getSource());
        return dispatcher.getCompletionSuggestions(results);
    }

    @Override
    public Collection<String> getExamples() {
        return dispatcher.getRoot().getChildren().stream()
                .filter(it -> it instanceof LiteralCommandNode)
                .map(CommandNode::getName)
                .collect(Collectors.toList());
    }
}
