package net.earthcomputer.multiconnect.packets.v1_18_2;

import net.earthcomputer.multiconnect.ap.MessageVariant;
import net.earthcomputer.multiconnect.ap.Type;
import net.earthcomputer.multiconnect.ap.Types;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.packets.CommonTypes;

@MessageVariant(minVersion = Protocols.V1_13, maxVersion = Protocols.V1_18_2)
public class VibrationPath_1_18_2 implements CommonTypes.VibrationPath {
    public double originX;
    public double originY;
    public double originZ;
    public double destX;
    public double destY;
    public double destZ;
    @Type(Types.INT)
    public int ticks;
}
