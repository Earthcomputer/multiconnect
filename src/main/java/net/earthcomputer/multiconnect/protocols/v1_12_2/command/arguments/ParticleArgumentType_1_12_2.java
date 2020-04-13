package net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.ParticleArgument;
import net.minecraft.particles.ParticleType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class ParticleArgumentType_1_12_2 implements ArgumentType<ParticleType<?>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar");

    private ParticleArgumentType_1_12_2() {}

    public static ParticleArgumentType_1_12_2 particle() {
        return new ParticleArgumentType_1_12_2();
    }

    @Override
    public ParticleType<?> parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        ResourceLocation id = ResourceLocation.read(reader);
        if (!Registry.PARTICLE_TYPE.containsKey(id)) {
            reader.setCursor(start);
            throw ParticleArgument.PARTICLE_NOT_FOUND.createWithContext(reader, id);
        }
        return Registry.PARTICLE_TYPE.getOrDefault(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        ISuggestionProvider.suggestIterable(Registry.PARTICLE_TYPE.keySet(), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
