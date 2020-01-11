package net.earthcomputer.multiconnect.api;

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

    private final int dataValue;

    private final String displayName;

    EnumProtocol(final String name, final int value) {
        dataValue = value;
        displayName = name;
    }

    public int getValue() { return dataValue; }

    public String getDisplayName() { return displayName; }

    private static EnumProtocol[] dataValues = values();

    public EnumProtocol next() {
        EnumProtocol nextValue = AUTO;

        for (EnumProtocol protocolValue : dataValues) {
            if (protocolValue.dataValue > dataValue) {
                nextValue = protocolValue;
            }
        }

        return nextValue;
    }

}