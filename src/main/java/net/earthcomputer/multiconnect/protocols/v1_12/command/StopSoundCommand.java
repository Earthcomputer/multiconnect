package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.SoundData_1_8;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.Registry;

import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EnumArgumentType.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;

public class StopSoundCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        dispatcher.register(literal("stopsound")
            .then(argument("player", players())
                .executes(ctx -> 0)
                .then(argument("category", enumArg("master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"))
                    .executes(ctx -> 0)
                    .then(argument("sound", id())
                        .suggests((ctx, builder) -> ConnectionInfo.protocolVersion <= Protocols.V1_8 ? SharedSuggestionProvider.suggest(SoundData_1_8.getInstance().getAllSounds(), builder) : SharedSuggestionProvider.suggestResource(Registry.SOUND_EVENT.keySet(), builder))
                        .executes(ctx -> 0)))));
    }

}
