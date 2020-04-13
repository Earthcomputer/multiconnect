package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;

import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;

public class Simple1ArgCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher, String name) {
        dispatcher.register(literal(name)
            .then(argument("value", word())
                .suggests(SuggestionProviders.ASK_SERVER)
                .executes(ctx -> 0)));
    }

}
