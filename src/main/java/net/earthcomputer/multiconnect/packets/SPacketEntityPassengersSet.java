package net.earthcomputer.multiconnect.packets;

import it.unimi.dsi.fastutil.ints.IntList;
import net.earthcomputer.multiconnect.ap.MessageVariant;

@MessageVariant
public class SPacketEntityPassengersSet {
    public int vehicleId;
    public IntList passengerIds;
}
