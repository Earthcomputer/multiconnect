package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;
import net.earthcomputer.multiconnect.packets.SPacketCommandSuggestions;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_13)
public class SPacketCommandSuggestions_Latest implements SPacketCommandSuggestions {
    public int transactionId;
    public int start;
    public int length;
    public List<Match> matches;

    @MessageVariant
    public static class Match {
        public String match;
        public Optional<CommonTypes.Text> tooltip;

        public Match() {
        }

        public Match(String match, Optional<CommonTypes.Text> tooltip) {
            this.match = match;
            this.tooltip = tooltip;
        }
    }
}
