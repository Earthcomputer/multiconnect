package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.List;
import java.util.Optional;

@Message
public class CPacketBookUpdate {
    public CommonTypes.Hand hand;
    public List<String> pages;
    public Optional<String> title;
}
