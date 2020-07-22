package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandSource;

import static com.mojang.brigadier.arguments.BoolArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.minecraft.command.argument.MobEffectArgumentType.*;

public class EffectCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("effect")
            .then(argument("target", entities())
                .then(literal("clear")
                    .executes(ctx -> 0))
                .then(argument("effect", mobEffect())
                    .executes(ctx -> 0)
                    .then(argument("seconds", integer(0, 1000000))
                        .executes(ctx -> 0)
                        .then(argument("amplifier", integer(0, 255))
                            .executes(ctx -> 0)
                            .then(argument("hideParticles", bool())
                                .executes(ctx -> 0)))))));
    }

}
