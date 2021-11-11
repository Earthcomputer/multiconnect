package net.earthcomputer.multiconnect.protocols.v1_17_1;

import net.earthcomputer.multiconnect.protocols.generic.Key;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18;

import java.util.BitSet;

public class Protocol_1_17_1 extends Protocol_1_18 {
    public static final Key<BitSet> VERTICAL_STRIP_BITMASK = Key.create("verticalStripBitmask");
}
