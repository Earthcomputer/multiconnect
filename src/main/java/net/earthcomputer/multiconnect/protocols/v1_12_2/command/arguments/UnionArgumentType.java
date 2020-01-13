package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.datafixers.util.Either;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public final class UnionArgumentType<L, R> implements ArgumentType<Either<L, R>> {

    private final ArgumentType<L> left;
    private final ArgumentType<R> right;

    private UnionArgumentType(ArgumentType<L> left, ArgumentType<R> right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> UnionArgumentType<L, R> union(ArgumentType<L> left, ArgumentType<R> right) {
        return new UnionArgumentType<>(left, right);
    }

    @Override
    public Either<L, R> parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        try {
            return Either.left(left.parse(reader));
        } catch (CommandSyntaxException leftError) {
            reader.setCursor(start);
            try {
                return Either.right(right.parse(reader));
            } catch (CommandSyntaxException rightError) {
                reader.setCursor(start);
                throw leftError;
            }
        }
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        CompletableFuture<Suggestions> leftFuture = left.listSuggestions(context, builder.restart());
        CompletableFuture<Suggestions> rightFuture = right.listSuggestions(context, builder.restart());
        return CompletableFuture.allOf(leftFuture, rightFuture)
                .thenCompose(v -> mergeSuggestions(leftFuture.join(), rightFuture.join()));
    }

    static CompletableFuture<Suggestions> mergeSuggestions(Suggestions a, Suggestions b) {
        if (a.isEmpty() && b.isEmpty())
            return Suggestions.empty();
        if (a.isEmpty())
            return CompletableFuture.completedFuture(b);
        if (b.isEmpty())
            return CompletableFuture.completedFuture(a);

        Set<Suggestion> suggestions = new LinkedHashSet<>();
        suggestions.addAll(a.getList());
        suggestions.addAll(b.getList());
        return CompletableFuture.completedFuture(new Suggestions(StringRange.encompassing(a.getRange(), b.getRange()), new ArrayList<>(suggestions)));
    }

    @Override
    public Collection<String> getExamples() {
        Set<String> examples = new HashSet<>();
        examples.addAll(left.getExamples());
        examples.addAll(right.getExamples());
        return examples;
    }

    @Override
    public String toString() {
        return "union(" + left + ", " + right + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(left, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != UnionArgumentType.class) return false;
        UnionArgumentType<?, ?> that = (UnionArgumentType<?, ?>) obj;
        return left.equals(that.left) && right.equals(that.right);
    }
}
