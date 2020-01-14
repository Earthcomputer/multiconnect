package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.minecraft.command.arguments.EntitySummonArgumentType.*;
import static net.minecraft.command.arguments.NbtCompoundTagArgumentType.*;
import static net.minecraft.command.arguments.Vec3ArgumentType.*;

public class SummonCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        //noinspection unchecked
        dispatcher.register(literal("summon")
            .then(argument("entity", entitySummon())
                .suggests((SuggestionProvider<CommandSource>) (SuggestionProvider<?>) SuggestionProviders.SUMMONABLE_ENTITIES)
                .executes(ctx -> 0)
                .then(argument("pos", vec3())
                    .executes(ctx -> 0)
                    .then(argument("nbt", nbtCompound())
                        .executes(ctx -> 0)))));
    }

}
