package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.UnionArgumentType.*;

public class GamemodeCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("gamemode")
            .then(argument("mode", union(enumArg("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp"), integer(0, 3)))
                .executes(ctx -> 0)
                .then(argument("target", players())
                    .executes(ctx -> 0))));
    }

}
