package net.earthcomputer.multiconnect.packets.latest;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CPacketBookUpdate;

import java.util.List;
import java.util.Optional;

@MessageVariant(minVersion = Protocols.V1_17_1)
public class CPacketBookUpdate_Latest implements CPacketBookUpdate {
    public int slot;
    public List<String> pages;
    public Optional<String> title;
}
