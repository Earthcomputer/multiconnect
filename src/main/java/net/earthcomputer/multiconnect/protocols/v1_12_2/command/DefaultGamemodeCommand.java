package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.argument;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.literal;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.enumArg;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.UnionArgumentType.union;

public class DefaultGamemodeCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("gamemode")
            .then(argument("mode", union(enumArg("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp"), integer(0, 3)))
                .executes(ctx -> 0)));
    }

}
