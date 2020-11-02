package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.UnionArgumentType.*;

public class DifficultyCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("difficulty")
            .then(argument("newDifficulty", union(enumArg("peaceful", "easy", "normal", "hard", "p", "e", "n", "h").caseInsensitive(), integer(0, 3)))
                .executes(ctx -> 0)));
    }

}
