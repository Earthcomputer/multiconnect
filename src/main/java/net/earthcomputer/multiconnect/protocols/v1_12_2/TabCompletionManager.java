package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.suggestion.Suggestion;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.RequestCommandCompletionsC2SPacket;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.server.command.CommandSource;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TabCompletionManager {

    private static final Queue<Entry> entries = new ArrayDeque<>();
    private static final Queue<CompletableFuture<List<String>>> customCompletions = new ArrayDeque<>();

    public static void reset() {
        entries.clear();
        customCompletions.clear();
    }

    public static void addTabCompletionRequest(int id, String message) {
        synchronized (entries) {
            entries.add(new Entry(id, message));
        }
    }

    public static Entry nextEntry() {
        synchronized (entries) {
            return entries.poll();
        }
    }

    public static void requestCommandList() {
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(-1, "/"));
    }

    public static CompletableFuture<List<String>> requestCustomCompletion(String command) {
        assert MinecraftClient.getInstance().getNetworkHandler() != null;
        CompletableFuture<List<String>> future = new CompletableFuture<>();
        customCompletions.add(future);
        MinecraftClient.getInstance().getNetworkHandler().sendPacket(new RequestCommandCompletionsC2SPacket(-2, command));
        return future;
    }

    public static boolean handleCustomCompletions(CommandSuggestionsS2CPacket packet) {
        if (packet.getCompletionId() == -1) {
            CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
            Commands_1_12_2.registerAll(dispatcher, packet.getSuggestions().getList().stream()
                    .map(Suggestion::getText)
                    .filter(str -> !str.isEmpty())
                    .map(str -> str.substring(1))
                    .collect(Collectors.toSet()));
            assert MinecraftClient.getInstance().getNetworkHandler() != null;
            MinecraftClient.getInstance().getNetworkHandler().onCommandTree(new CommandTreeS2CPacket(dispatcher.getRoot()));
            return true;
        } else if (packet.getCompletionId() == -2) {
            if (customCompletions.isEmpty())
                return false;
            customCompletions.remove().complete(packet.getSuggestions().getList().stream().map(Suggestion::getText).collect(Collectors.toList()));
            return true;
        } else {
            return false;
        }
    }

    public static final class Entry {
        private final int id;
        private final String message;

        public Entry(int id, String message) {
            this.id = id;
            this.message = message;
        }

        public int getId() {
            return id;
        }

        public String getMessage() {
            return message;
        }
    }

}
