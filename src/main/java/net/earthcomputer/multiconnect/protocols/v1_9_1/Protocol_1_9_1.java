package net.earthcomputer.multiconnect.protocols.v1_9_1;

import net.earthcomputer.multiconnect.protocols.v1_9_2.Protocol_1_9_2;

public class Protocol_1_9_1 extends Protocol_1_9_2 {
    // This protocol has 1 change, fixing the readableBytes() in readVarIntArray.
    // Not changing anything in multiconnect actually fixes the client-side bug connecting to 1.9.1 servers.
}
