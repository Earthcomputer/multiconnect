package net.earthcomputer.multiconnect.api;

public enum EnumProtocol {

    AUTO(-1),
    V1_15_1(575),
    V1_15(573),
    V1_14_4(498),
    V1_14_3(490),
    V1_14_2(485),
    V1_14_1(480),
    V1_14(477),
    V1_13_2(404),
    V1_13_1(401),
    V1_13(393),
    V1_12_2(340);

    private final int value;

    EnumProtocol(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }

    private static EnumProtocol[] dataValues = values();

    public EnumProtocol next()
    {
        return dataValues[(ordinal()+1) % dataValues.length];
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