package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.argument;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.literal;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.enumArg;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.UnionArgumentType.union;

public class DefaultGamemodeCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("gamemode")
            .then(argument("mode", union(enumArg("survival", "creative", "adventure", "spectator", "s", "c", "a", "sp"), integer(0, 3)))
                .executes(ctx -> 0)));
    }

}
