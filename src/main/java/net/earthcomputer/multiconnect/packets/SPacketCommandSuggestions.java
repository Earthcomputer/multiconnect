package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.List;
import java.util.Optional;

@Message
public class SPacketCommandSuggestions {
    public int transactionId;
    public int start;
    public int length;
    public List<Match> matches;

    @Message
    public static class Match {
        public String match;
        public Optional<CommonTypes.Text> tooltip;
    }
}
