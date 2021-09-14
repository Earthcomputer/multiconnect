package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;

import java.util.Optional;

@Message
public class SPacketResourcePackSend {
    public String url;
    public String hash;
    public boolean forced;
    public Optional<CommonTypes.Text> promptMessage;
}
