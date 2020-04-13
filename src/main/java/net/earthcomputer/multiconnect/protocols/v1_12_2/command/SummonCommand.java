package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.minecraft.command.arguments.EntitySummonArgument.entitySummon;
import static net.minecraft.command.arguments.NBTCompoundTagArgument.nbt;
import static net.minecraft.command.arguments.Vec3Argument.vec3;

public class SummonCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        //noinspection unchecked
        dispatcher.register(literal("summon")
            .then(argument("entity", entitySummon())
                .suggests((SuggestionProvider<ISuggestionProvider>) (SuggestionProvider<?>) SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(ctx -> 0)
                .then(argument("pos", vec3())
                    .executes(ctx -> 0)
                    .then(argument("nbt", nbt())
                        .executes(ctx -> 0)))));
    }

}
