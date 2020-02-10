package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.Protocols;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum constants for each protocol, and the auto connection mode
 */
public enum ConnectionMode implements IProtocol {

    // Protocols should go in reverse chronological order
    AUTO("Auto", -1),
    V1_16("20w06a", Protocols.V1_16),
    V1_15_2("1.15.2", Protocols.V1_15_2),
    V1_15_1("1.15.1", Protocols.V1_15_1),
    V1_15("1.15", Protocols.V1_15),
    V1_14_4("1.14.4", Protocols.V1_14_4),
    V1_14_3("1.14.3", Protocols.V1_14_3),
    V1_14_2("1.14.2", Protocols.V1_14_2),
    V1_14_1("1.14.1", Protocols.V1_14_1),
    V1_14("1.14", Protocols.V1_14),
    V1_13_2("1.13.2", Protocols.V1_13_2),
    V1_13_1("1.13.1", Protocols.V1_13_1),
    V1_13("1.13", Protocols.V1_13),
    V1_12_2("1.12.2", Protocols.V1_12_2),
    V1_12_1("1.12.1", Protocols.V1_12_1),
    V1_12("1.12", Protocols.V1_12),
    ;

    private final int value;
    private final String name;
    private final String assetId;

    ConnectionMode(final String name, final int value) {
        this(name, name, value);
    }

    ConnectionMode(final String name, final String assetId, final int value) {
        this.value = value;
        this.name = name;
        this.assetId = assetId;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAssetId() {
        return assetId;
    }

    private static final ConnectionMode[] VALUES = values();
    private static final ConnectionMode[] PROTOCOLS = Arrays.stream(VALUES).filter(it -> it != AUTO).toArray(ConnectionMode[]::new);
    private static final Map<Integer, ConnectionMode> BY_VALUE = new HashMap<>();

    public static ConnectionMode byValue(int value) {
        return BY_VALUE.getOrDefault(value, AUTO);
    }

    public static ConnectionMode[] protocolValues() {
        return PROTOCOLS;
    }

    public static boolean isSupportedProtocol(int protocol) {
        return byValue(protocol) != AUTO;
    }

    public ConnectionMode next() {
        return VALUES[(ordinal() + 1) % VALUES.length];
    }

    static {
        for (ConnectionMode value : VALUES) {
            BY_VALUE.put(value.getValue(), value);
        }
    }

}
