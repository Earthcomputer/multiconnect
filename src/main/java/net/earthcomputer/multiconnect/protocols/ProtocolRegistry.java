package net.earthcomputer.multiconnect.protocols;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.earthcomputer.multiconnect.protocols.generic.AbstractProtocol;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_1;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_1;
import net.earthcomputer.multiconnect.protocols.v1_13.Protocol_1_13_2;
import net.earthcomputer.multiconnect.protocols.v1_14.*;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15_1;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15_2;
import net.earthcomputer.multiconnect.protocols.v1_16.*;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17;
import net.earthcomputer.multiconnect.protocols.v1_17.Protocol_1_17_1;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18;
import net.earthcomputer.multiconnect.protocols.v1_18.Protocol_1_18_2;
import net.earthcomputer.multiconnect.protocols.v1_19.Protocol_1_19;
import net.earthcomputer.multiconnect.protocols.v1_19.Protocol_1_19_2;
import net.earthcomputer.multiconnect.protocols.v1_19.Protocol_1_19_3;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9_1;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9_2;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9_4;
import net.minecraft.SharedConstants;

import static net.earthcomputer.multiconnect.api.Protocols.*;

public class ProtocolRegistry {
    private static final Int2ObjectOpenHashMap<AbstractProtocol> protocols = new Int2ObjectOpenHashMap<>();

    public static AbstractProtocol get(int version) {
        return protocols.get(version);
    }

    public static AbstractProtocol latest() {
        return protocols.get(SharedConstants.getCurrentVersion().getProtocolVersion());
    }

    public static Iterable<AbstractProtocol> all() {
        return protocols.values();
    }


    public static void register(int version, AbstractProtocol protocol) {
        protocols.put(version, protocol);
    }

    static {
        register(V1_19_3, new Protocol_1_19_3());
        register(V1_19_2, new Protocol_1_19_2());
        register(V1_19, new Protocol_1_19());
        register(V1_18_2, new Protocol_1_18_2());
        register(V1_18, new Protocol_1_18());
        register(V1_17_1, new Protocol_1_17_1());
        register(V1_17, new Protocol_1_17());
        register(V1_16_5, new Protocol_1_16_5());
        register(V1_16_3, new Protocol_1_16_3());
        register(V1_16_2, new Protocol_1_16_2());
        register(V1_16_1, new Protocol_1_16_1());
        register(V1_16, new Protocol_1_16());
        register(V1_15_2, new Protocol_1_15_2());
        register(V1_15_1, new Protocol_1_15_1());
        register(V1_15, new Protocol_1_15());
        register(V1_14_4, new Protocol_1_14_4());
        register(V1_14_3, new Protocol_1_14_3());
        register(V1_14_2, new Protocol_1_14_2());
        register(V1_14_1, new Protocol_1_14_1());
        register(V1_14, new Protocol_1_14());
        register(V1_13_2, new Protocol_1_13_2());
        register(V1_13_1, new Protocol_1_13_1());
        register(V1_13, new Protocol_1_13());
        register(V1_12_2, new Protocol_1_12_2());
        register(V1_12_1, new Protocol_1_12_1());
        register(V1_12, new Protocol_1_12());
        register(V1_11_2, new Protocol_1_11_2());
        register(V1_11, new Protocol_1_11());
        register(V1_10, new Protocol_1_10());
        register(V1_9_4, new Protocol_1_9_4());
        register(V1_9_2, new Protocol_1_9_2());
        register(V1_9_1, new Protocol_1_9_1());
        register(V1_9, new Protocol_1_9());
        register(V1_8, new Protocol_1_8());
    }

}
