package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.NetworkEnum;

import java.util.List;

@MessageVariant
public class SPacketCustomChatCompletions {
    public Action action;
    public List<String> entries;

    @NetworkEnum
    public enum Action {
        ADD,
        REMOVE,
        SET,
    }
}
