package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.SharedSuggestionProvider;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.UnionArgumentType.*;

public class DifficultyCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("difficulty")
            .then(argument("newDifficulty", union(enumArg("peaceful", "easy", "normal", "hard", "p", "e", "n", "h").caseInsensitive(), integer(0, 3)))
                .executes(ctx -> 0)));
    }

}
