package net.earthcomputer.multiconnect.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum constants for each protocol, including the number and name of each
 */
public enum EnumProtocol {

    AUTO("Auto", -1),
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
    V1_12_2("1.12.2", Protocols.V1_12_2);

    private final int value;

    private final String name;

    EnumProtocol(final String name, final int value) {
        this.value = value;
        this.name = name;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    private static final EnumProtocol[] VALUES = values();
    private static final Map<Integer, EnumProtocol> BY_VALUE = new HashMap<>();

    public static EnumProtocol byValue(int value) {
        return BY_VALUE.get(value);
    }

    public EnumProtocol next() {
        EnumProtocol nextValue = AUTO;
        int max = Integer.MAX_VALUE;

        for (EnumProtocol protocolValue : VALUES) {
            if (protocolValue.value > value && protocolValue.value < max) {
                nextValue = protocolValue;
                max = nextValue.value;
            }
        }

        return nextValue;
    }

    static {
        for (EnumProtocol value : VALUES) {
            BY_VALUE.put(value.getValue(), value);
        }
    }

}
