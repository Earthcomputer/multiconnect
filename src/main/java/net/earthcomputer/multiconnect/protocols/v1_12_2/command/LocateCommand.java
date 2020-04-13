package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;

public class LocateCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("locate")
            .then(argument("feature", enumArg("Stronghold", "Monument", "Village", "Mansion", "EndCity", "Fortress", "Temple", "Mineshaft"))
                .executes(ctx -> 0)));
    }

}
