package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;

public class NoArgCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher, String name) {
        dispatcher.register(literal(name)
            .executes(ctx -> 0));
    }

}
