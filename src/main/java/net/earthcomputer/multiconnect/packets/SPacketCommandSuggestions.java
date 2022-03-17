package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;

import java.util.List;
import java.util.Optional;

@MessageVariant
public class SPacketCommandSuggestions {
    public int transactionId;
    public int start;
    public int length;
    public List<Match> matches;

    @MessageVariant
    public static class Match {
        public String match;
        public Optional<CommonTypes.Text> tooltip;
    }
}
