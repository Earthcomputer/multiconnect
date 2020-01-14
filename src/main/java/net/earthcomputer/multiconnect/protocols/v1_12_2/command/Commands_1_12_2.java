package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.server.command.CommandSource;

import java.util.Set;
import java.util.function.Consumer;

public class Commands_1_12_2 {

    private static void registerVanilla(CommandDispatcher<CommandSource> dispatcher,
                                        Set<String> serverCommands,
                                        String name,
                                        Consumer<CommandDispatcher<CommandSource>> registerer) {
        if (serverCommands == null || serverCommands.contains(name)) {
            registerer.accept(dispatcher);
        }
    }

    public static void register(CommandDispatcher<CommandSource> dispatcher, Set<String> serverCommands) {
        registerVanilla(dispatcher, serverCommands, "time", TimeCommand::register);
        registerVanilla(dispatcher, serverCommands, "gamemode", GamemodeCommand::register);
        registerVanilla(dispatcher, serverCommands, "difficulty", DifficultyCommand::register);
        registerVanilla(dispatcher, serverCommands, "defaultgamemode", DefaultGamemodeCommand::register);
        registerVanilla(dispatcher, serverCommands, "kill", KillCommand::register);
        registerVanilla(dispatcher, serverCommands, "toggledownfall", d -> NoArgCommand.register(d, "toggledownfall"));
        registerVanilla(dispatcher, serverCommands, "weather", WeatherCommand::register);
        registerVanilla(dispatcher, serverCommands, "xp", XPCommand::register);
        registerVanilla(dispatcher, serverCommands, "tp", TPCommand::register);
        registerVanilla(dispatcher, serverCommands, "teleport", TeleportCommand::register);
        registerVanilla(dispatcher, serverCommands, "give", GiveCommand::register);
        registerVanilla(dispatcher, serverCommands, "replaceitem", ReplaceItemCommand::register);
        registerVanilla(dispatcher, serverCommands, "stats", StatsCommand::register);
        registerVanilla(dispatcher, serverCommands, "effect", EffectCommand::register);
        registerVanilla(dispatcher, serverCommands, "enchant", EnchantCommand::register);
        registerVanilla(dispatcher, serverCommands, "particle", ParticleCommand::register);
        registerVanilla(dispatcher, serverCommands, "me", d -> SayCommand.register(d, "me"));
        registerVanilla(dispatcher, serverCommands, "seed", d -> NoArgCommand.register(d, "seed"));
        registerVanilla(dispatcher, serverCommands, "help", d -> HelpCommand.register(d, "help"));
        registerVanilla(dispatcher, serverCommands, "?", d -> HelpCommand.register(d, "?"));
        registerVanilla(dispatcher, serverCommands, "debug", DebugCommand::register);
        registerVanilla(dispatcher, serverCommands, "tell", d -> TellCommand.register(d, "tell"));
        registerVanilla(dispatcher, serverCommands, "msg", d -> TellCommand.register(d, "msg"));
        registerVanilla(dispatcher, serverCommands, "w", d -> TellCommand.register(d, "w"));
        registerVanilla(dispatcher, serverCommands, "say", d -> SayCommand.register(d, "say"));
        registerVanilla(dispatcher, serverCommands, "spawnpoint", SpawnpointCommand::register);
        registerVanilla(dispatcher, serverCommands, "setworldspawn", SetWorldSpawnCommand::register);
        registerVanilla(dispatcher, serverCommands, "gamerule", GameruleCommand::register);
        registerVanilla(dispatcher, serverCommands, "clear", ClearCommand::register);
        registerVanilla(dispatcher, serverCommands, "testfor", TestForCommand::register);
        registerVanilla(dispatcher, serverCommands, "spreadplayers", SpreadPlayersCommand::register);
        registerVanilla(dispatcher, serverCommands, "playsound", PlaySoundCommand::register);

        if (serverCommands != null) {
            for (String command : serverCommands) {
                if (dispatcher.getRoot().getChild(command) == null) {
                    dispatcher.register(literal(command)
                            .executes(ctx -> 0)
                            .then(argument("args", StringArgumentType.greedyString())
                                .suggests(SuggestionProviders.ASK_SERVER)
                                .executes(ctx -> 0)));
                }
            }
        }
    }

    public static LiteralArgumentBuilder<CommandSource> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<CommandSource, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

}
