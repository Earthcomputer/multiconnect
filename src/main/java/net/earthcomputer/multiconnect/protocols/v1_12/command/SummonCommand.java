package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityTypeArgumentType_1_12.*;
import static net.minecraft.commands.arguments.CompoundTagArgument.*;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.*;

public class SummonCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("summon")
            .then(argument("entity", entityType())
                .suggests(SUMMONABLE_ENTITIES)
                .executes(ctx -> 0)
                .then(argument("pos", vec3())
                    .executes(ctx -> 0)
                    .then(argument("nbt", compoundTag())
                        .executes(ctx -> 0)))));
    }

}
