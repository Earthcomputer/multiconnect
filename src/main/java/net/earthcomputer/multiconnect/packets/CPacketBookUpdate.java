package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.List;
import java.util.Optional;

@Message(minVersion = Protocols.V1_17_1)
public class CPacketBookUpdate {
    public int slot;
    public List<String> pages;
    public Optional<String> title;
}
