package net.earthcomputer.multiconnect.packets;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.minecraft.util.Identifier;

import java.util.Optional;

@MessageVariant
public class SPacketSelectAdvancementTab {
    public Optional<Identifier> tabId;
}
