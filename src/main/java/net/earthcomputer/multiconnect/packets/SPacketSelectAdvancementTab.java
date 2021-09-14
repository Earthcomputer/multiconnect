package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.Message;
import net.minecraft.util.Identifier;

import java.util.Optional;

@Message
public class SPacketSelectAdvancementTab {
    public Optional<Identifier> tabId;
}
