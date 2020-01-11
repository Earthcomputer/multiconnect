package net.earthcomputer.multiconnect.api;

public enum EnumProtocol {

    AUTO(-1),
    V1_15_1(Protocols.V1_15_1),
    V1_15(Protocols.V1_15),
    V1_14_4(Protocols.V1_14_4),
    V1_14_3(Protocols.V1_14_3),
    V1_14_2(Protocols.V1_14_2),
    V1_14_1(Protocols.V1_14_1),
    V1_14(Protocols.V1_14),
    V1_13_2(Protocols.V1_13_2),
    V1_13_1(Protocols.V1_13_1),
    V1_13(Protocols.V1_13),
    V1_12_2(Protocols.V1_12_2);

    private final int value;

    EnumProtocol(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    private static EnumProtocol[] dataValues = values();

    public EnumProtocol next() {
        EnumProtocol nextValue = AUTO;

        for (EnumProtocol eachValue : dataValues) {
            if (eachValue.value > value) {
                nextValue = eachValue;
            }
        }

        return nextValue;
    }

    public static String getEnumNameForValue(int value) {
        for(EnumProtocol eachValue : dataValues) {
            if (eachValue.value == value) {
                return eachValue.name();
            }
        }
        return AUTO.name();
    }

}