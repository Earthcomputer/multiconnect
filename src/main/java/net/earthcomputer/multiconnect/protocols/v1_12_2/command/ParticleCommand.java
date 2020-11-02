package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;

import static com.mojang.brigadier.arguments.DoubleArgumentType.*;
import static com.mojang.brigadier.arguments.IntegerArgumentType.*;
import static com.mojang.brigadier.arguments.StringArgumentType.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.EntityArgumentType_1_12_2.*;
import static net.earthcomputer.multiconnect.protocols.v1_12_2.command.arguments.ParticleArgumentType_1_12_2.*;
import static net.minecraft.command.argument.Vec3ArgumentType.*;

public class ParticleCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(literal("particle")
            .then(argument("type", particle()))
                .then(argument("pos", vec3())
                    .then(argument("vx", doubleArg())
                        .then(argument("vy", doubleArg())
                            .then(argument("vz", doubleArg())
                                .then(argument("speed", doubleArg())
                                    .executes(ctx -> 0)
                                    .then(argument("count", integer(0))
                                        .executes(ctx -> 0)
                                        .then(argument("mode", word())
                                            .suggests((ctx, builder) -> {
                                                CommandSource.suggestMatching(new String[] {"normal", "force"}, builder);
                                                return builder.buildFuture();
                                            })
                                            .executes(ctx -> 0)
                                            .then(argument("player", players())
                                                .executes(ctx -> 0)
                                                .then(argument("param1", integer())
                                                    .executes(ctx -> 0)
                                                    .then(argument("param2", integer()))))))))))));
    }

}
