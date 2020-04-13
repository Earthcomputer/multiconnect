package net.earthcomputer.multiconnect.protocols.v1_12_2.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.command.arguments.SuggestionProviders;

import java.util.Set;
import java.util.function.Consumer;

public class Commands_1_12_2 {

    private static void registerVanilla(CommandDispatcher<ISuggestionProvider> dispatcher,
                                        Set<String> serverCommands,
                                        String name,
                                        Consumer<CommandDispatcher<ISuggestionProvider>> registerer) {
        if (serverCommands == null || serverCommands.contains(name)) {
            registerer.accept(dispatcher);
        }
    }

    public static void register(CommandDispatcher<ISuggestionProvider> dispatcher, Set<String> serverCommands) {
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
        registerVanilla(dispatcher, serverCommands, "scoreboard", ScoreboardCommand::register);
        registerVanilla(dispatcher, serverCommands, "execute", ExecuteCommand::register);
        registerVanilla(dispatcher, serverCommands, "trigger", TriggerCommand::register);
        registerVanilla(dispatcher, serverCommands, "advancement", AdvancementCommand::register);
        registerVanilla(dispatcher, serverCommands, "recipe", RecipeCommand::register);
        registerVanilla(dispatcher, serverCommands, "summon", SummonCommand::register);
        registerVanilla(dispatcher, serverCommands, "setblock", SetblockCommand::register);
        registerVanilla(dispatcher, serverCommands, "fill", FillCommand::register);
        registerVanilla(dispatcher, serverCommands, "clone", CloneCommand::register);
        registerVanilla(dispatcher, serverCommands, "testforblocks", TestForBlocksCommand::register);
        registerVanilla(dispatcher, serverCommands, "blockdata", BlockDataCommand::register);
        registerVanilla(dispatcher, serverCommands, "testforblock", TestForBlockCommand::register);
        registerVanilla(dispatcher, serverCommands, "tellraw", TellRawCommand::register);
        registerVanilla(dispatcher, serverCommands, "worldborder", WorldborderCommand::register);
        registerVanilla(dispatcher, serverCommands, "title", TitleCommand::register);
        registerVanilla(dispatcher, serverCommands, "entitydata", EntityDataCommand::register);
        registerVanilla(dispatcher, serverCommands, "stopsound", StopSoundCommand::register);
        registerVanilla(dispatcher, serverCommands, "locate", LocateCommand::register);
        registerVanilla(dispatcher, serverCommands, "reload", d -> NoArgCommand.register(d, "reload"));
        registerVanilla(dispatcher, serverCommands, "function", FunctionCommand::register);
        registerVanilla(dispatcher, serverCommands, "op", d -> Simple1ArgCommand.register(d, "op"));
        registerVanilla(dispatcher, serverCommands, "deop", d -> Simple1ArgCommand.register(d, "deop"));
        registerVanilla(dispatcher, serverCommands, "stop", d -> NoArgCommand.register(d, "stop"));
        registerVanilla(dispatcher, serverCommands, "save-all", SaveAllCommand::register);
        registerVanilla(dispatcher, serverCommands, "save-off", d -> NoArgCommand.register(d, "save-off"));
        registerVanilla(dispatcher, serverCommands, "save-on", d -> NoArgCommand.register(d, "save-on"));
        registerVanilla(dispatcher, serverCommands, "ban-ip", d -> Simple1ArgCommand.register(d, "ban-ip"));
        registerVanilla(dispatcher, serverCommands, "pardon-ip", d -> Simple1ArgCommand.register(d, "pardon-ip"));
        registerVanilla(dispatcher, serverCommands, "ban", d -> Simple1ArgCommand.register(d, "ban"));
        registerVanilla(dispatcher, serverCommands, "banlist", BanListCommand::register);
        registerVanilla(dispatcher, serverCommands, "pardon", d -> Simple1ArgCommand.register(d, "pardon"));
        registerVanilla(dispatcher, serverCommands, "kick", d -> Simple1ArgCommand.register(d, "kick"));
        registerVanilla(dispatcher, serverCommands, "list", ListCommand::register);
        registerVanilla(dispatcher, serverCommands, "whitelist", WhitelistCommand::register);
        registerVanilla(dispatcher, serverCommands, "setidletimeout", SetIdleTimeoutCommand::register);

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

    public static LiteralArgumentBuilder<ISuggestionProvider> literal(String name) {
        return LiteralArgumentBuilder.literal(name);
    }

    public static <T> RequiredArgumentBuilder<ISuggestionProvider, T> argument(String name, ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }

}
