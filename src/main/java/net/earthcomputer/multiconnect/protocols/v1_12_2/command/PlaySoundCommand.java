package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;
import net.minecraft.util.registry.Registry;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static com.mojang.brigadier.arguments.FloatArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.arguments.IdentifierArgumentType.*;
import static net.minecraft.command.arguments.Vec3ArgumentType.*;

public class PlaySoundCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        for (String category : new String[] {"master", "music", "record", "weather", "block", "hostile", "neutral", "player", "ambient", "voice"}) {
            dispatcher.register(literal("playsound")
                .then(argument("sound", identifier())
                    .suggests((ctx, builder) -> {
                        CommandSource.suggestIdentifiers(Registry.SOUND_EVENT.getIds(), builder);
                        return builder.buildFuture();
                    })
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
