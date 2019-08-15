package net.earthcomputer.multiconnect.protocol;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.SharedConstants;

public class Protocols {

    public static final int V1_14_4 = 498;

    private static Int2ObjectOpenHashMap<IProtocol> protocols = new Int2ObjectOpenHashMap<>();
    static {
        protocols.put(V1_14_4, new Protocol_1_14_4());
    }

    public static boolean isSupported(int version) {
        return protocols.containsKey(version);
    }

    public static IProtocol get(int version) {
        return protocols.get(version);
    }

    public static IProtocol latest() {
        return protocols.get(SharedConstants.getGameVersion().getProtocolVersion());
    }

}
