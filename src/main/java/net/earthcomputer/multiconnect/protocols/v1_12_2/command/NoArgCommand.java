package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;

public class NoArgCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .executes(ctx -> 0));
    }

}
