package net.earthcomputer.multiconnect.protocols.v1_12.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;

public class ParticleEffectArgumentType_1_12_2 implements ArgumentType<ParticleType<?>> {

    private static final Collection<String> EXAMPLES = Arrays.asList("foo", "foo:bar");

    private ParticleEffectArgumentType_1_12_2() {}

    public static ParticleEffectArgumentType_1_12_2 particle() {
        return new ParticleEffectArgumentType_1_12_2();
    }

    @Override
    public ParticleType<?> parse(StringReader reader) throws CommandSyntaxException {
        int start = reader.getCursor();
        ResourceLocation id = ResourceLocation.read(reader);
        if (!Registry.PARTICLE_TYPE.containsKey(id)) {
            reader.setCursor(start);
            throw ParticleArgument.ERROR_UNKNOWN_PARTICLE.createWithContext(reader, id);
        }
        return Registry.PARTICLE_TYPE.get(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        SharedSuggestionProvider.suggestResource(Registry.PARTICLE_TYPE.keySet(), builder);
        return builder.buildFuture();
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

}
