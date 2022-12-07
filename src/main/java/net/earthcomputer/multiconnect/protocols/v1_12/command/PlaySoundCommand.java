package net.earthcomputer.multiconnect.protocols.v1_12.command;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.SoundData_1_8;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.registries.BuiltInRegistries;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.commands.arguments.ResourceLocationArgument.*;
import static net.minecraft.commands.arguments.coordinates.Vec3Argument.*;

public class PlaySoundCommand {

    public static void register(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        for (String category : new String[] {"master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"}) {
            dispatcher.register(literal("playsound")
                .then(argument("sound", id())
                    .suggests((ctx, builder) -> ConnectionInfo.protocolVersion <= Protocols.V1_8 ? SharedSuggestionProvider.suggest(SoundData_1_8.getInstance().getAllSounds(), builder) : SharedSuggestionProvider.suggestResource(BuiltInRegistries.SOUND_EVENT.keySet(), builder))
                    .then(literal(category)
                        .then(argument("player", players())
                            .executes(ctx -> 0)
                            .then(argument("pos", vec3())
                                .executes(ctx -> 0)
                                .then(argument("volume", floatArg(0))
                                    .executes(ctx -> 0)
                                    .then(argument("pitch", doubleArg(0, 2))
                                        .executes(ctx -> 0)
                                        .then(argument("minVolume", doubleArg(0, 1))
                                            .executes(ctx -> 0)))))))));
        }
    }

}
