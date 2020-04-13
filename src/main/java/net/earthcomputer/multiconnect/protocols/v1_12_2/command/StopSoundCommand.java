package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.registry.Registry;

import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EnumArgumentType.*;
import static net.minecraft.command.arguments.ResourceLocationArgument.resourceLocation;

public class StopSoundCommand {

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher) {
        dispatcher.register(literal("stopsound")
            .then(argument("player", players())
                .executes(ctx -> 0)
                .then(argument("category", enumArg("master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"))
                    .executes(ctx -> 0)
                    .then(argument("sound", resourceLocation())
                        .suggests((ctx, builder) -> ISuggestionProvider.suggestIterable(Registry.SOUND_EVENT.keySet(), builder))
                        .executes(ctx -> 0)))));
    }

}
