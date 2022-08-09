package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.protocols.generic.*;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;

public class Protocol_1_13_2 extends Protocol_1_14 {
    public static final Key<byte[][]> BLOCK_LIGHT_KEY = Key.create("blockLight");
    public static final Key<byte[][]> SKY_LIGHT_KEY = Key.create("skyLight");
}
