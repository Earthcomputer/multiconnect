package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.brigadier.CommandDispatcher;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.PacketSystem;
import net.earthcomputer.multiconnect.packets.latest.CPacketRequestCommandCompletions_Latest;
import net.earthcomputer.multiconnect.protocols.v1_12_2.command.Commands_1_12_2;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;

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
        var packet = new CPacketRequestCommandCompletions_Latest();
        packet.transactionId = -1;
        packet.text = "/";
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            PacketSystem.sendToServer(networkHandler, Protocols.V1_13, packet);
        }
    }

    public static CompletableFuture<List<String>> requestCustomCompletion(String command) {
        var future = new CompletableFuture<List<String>>();
        customCompletions.add(future);
        var packet = new CPacketRequestCommandCompletions_Latest();
        packet.transactionId = -2;
        packet.text = command;
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            PacketSystem.sendToServer(networkHandler, Protocols.V1_13, packet);
        }
        return future;
    }

    public static boolean handleCustomCompletions(Entry entry, List<String> suggestions) {
        if (entry.id() == -1) {
            MinecraftClient.getInstance().execute(() -> {
                ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
                if (networkHandler != null) {
                    networkHandler.onCommandTree(new CommandTreeS2CPacket(new CommandDispatcher<CommandSource>().getRoot()));
                    Commands_1_12_2.registerAll(networkHandler.getCommandDispatcher(), suggestions.stream()
                            .filter(str -> !str.isEmpty())
                            .map(str -> str.substring(1))
                            .collect(Collectors.toSet()));
                }
            });
            return true;
        } else if (entry.id() == -2) {
            if (customCompletions.isEmpty())
                return false;
            customCompletions.remove().complete(suggestions);
            return true;
        } else {
            return false;
        }
    }

    public record Entry(int id, String message) {
    }

}
