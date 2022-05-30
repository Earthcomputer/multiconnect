package net.earthcomputer.multiconnect.packets.v1_12_2;

import net.earthcomputer.multiconnect.ap.Argument;
import net.earthcomputer.multiconnect.ap.Handler;
import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.SPacketCommandSuggestions;
import net.earthcomputer.multiconnect.packets.latest.SPacketCommandSuggestions_Latest;
import net.earthcomputer.multiconnect.protocols.v1_12_2.TabCompletionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@MessageVariant(maxVersion = Protocols.V1_12_2)
public class SPacketCommandSuggestions_1_12_2 implements SPacketCommandSuggestions {
    private static final Logger LOGGER = LogManager.getLogger();

    public List<String> suggestions;

    @Handler
    public static List<SPacketCommandSuggestions_Latest> handle(
            @Argument("suggestions") List<String> suggestions
    ) {
        List<SPacketCommandSuggestions_Latest> packets = new ArrayList<>(1);

        var entry = TabCompletionManager.nextEntry();
        if (entry == null) {
            LOGGER.warn("Received unrequested tab completion packet");
            return packets;
        }

        if (TabCompletionManager.handleCustomCompletions(entry, suggestions)) {
            return packets;
        }

        var packet = new SPacketCommandSuggestions_Latest();
        packet.transactionId = entry.id();
        String message = entry.message();
        int start = message.lastIndexOf(' ') + 1;
        if (start == 0 && message.startsWith("/")) {
            start = 1;
        }
        packet.start = start;
        packet.length = message.length() - start;
        packet.matches = suggestions.stream()
                .map(suggestion -> new SPacketCommandSuggestions_Latest.Match(suggestion, Optional.empty()))
                .collect(Collectors.toCollection(ArrayList::new));
        packets.add(packet);
        return packets;
    }
}
