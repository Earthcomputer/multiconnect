package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityTypeArgumentType_1_12.*;
import static net.minecraft.command.argument.NbtCompoundTagArgumentType.*;
import static net.minecraft.command.argument.Vec3ArgumentType.*;

public class SummonCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("summon")
            .then(argument("entity", entityType())
                .suggests(SUMMONABLE_ENTITIES)
                .executes(ctx -> 0)
                .then(argument("pos", vec3())
                    .executes(ctx -> 0)
                    .then(argument("nbt", nbtCompound())
                        .executes(ctx -> 0)))));
    }

}
