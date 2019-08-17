package net.earthcomputer.multiconnect.protocols;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14;
import net.earthcomputer.multiconnect.protocols.v1_14_1.Protocol_1_14_1;
import net.earthcomputer.multiconnect.protocols.v1_14_2.Protocol_1_14_2;
import net.earthcomputer.multiconnect.protocols.v1_14_3.Protocol_1_14_3;
import net.earthcomputer.multiconnect.protocols.v1_14_4.Protocol_1_14_4;
import net.minecraft.SharedConstants;

import static net.earthcomputer.multiconnect.api.Protocols.*;

public class ProtocolRegistry {

    private static Int2ObjectOpenHashMap<AbstractProtocol> protocols = new Int2ObjectOpenHashMap<>();
    static {
        protocols.put(V1_14_4, new Protocol_1_14_4());
        protocols.put(V1_14_3, new Protocol_1_14_3());
        protocols.put(V1_14_2, new Protocol_1_14_2());
        protocols.put(V1_14_1, new Protocol_1_14_1());
        protocols.put(V1_14, new Protocol_1_14());
    }

    public static boolean isSupported(int version) {
        return protocols.containsKey(version);
    }

    public static AbstractProtocol get(int version) {
        return protocols.get(version);
    }

    public static AbstractProtocol latest() {
        return protocols.get(SharedConstants.getGameVersion().getProtocolVersion());
    }

}
