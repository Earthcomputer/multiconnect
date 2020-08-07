package net.earthcomputer.multiconnect.connect;

import com.google.common.collect.Lists;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.DropDownWidget;

import java.util.*;

/**
 * Enum constants for each protocol, and the auto connection mode
 */
public enum ConnectionMode implements IProtocol {

    // Protocols should go in reverse chronological order
    AUTO("Auto", -1, -1, true),
    V1_16_2("1.16.2-rc1", Protocols.V1_16_2, 2576),
    V1_16_1("1.16.1", Protocols.V1_16_1, 2567),
    V1_16("1.16", Protocols.V1_16, 2566, true),
    V1_15_2("1.15.2", Protocols.V1_15_2, 2230),
    V1_15_1("1.15.1", Protocols.V1_15_1, 2227),
    V1_15("1.15", Protocols.V1_15, 2225, true),
    V1_14_4("1.14.4", Protocols.V1_14_4, 1976),
    V1_14_3("1.14.3", Protocols.V1_14_3, 1968),
    V1_14_2("1.14.2", Protocols.V1_14_2, 1963),
    V1_14_1("1.14.1", Protocols.V1_14_1, 1957),
    V1_14("1.14", Protocols.V1_14, 1952, true),
    V1_13_2("1.13.2", Protocols.V1_13_2, 1631),
    V1_13_1("1.13.1", Protocols.V1_13_1, 1628),
    V1_13("1.13", Protocols.V1_13, 1519, true),
    V1_12_2("1.12.2", Protocols.V1_12_2, 1343),
    V1_12_1("1.12.1", Protocols.V1_12_1, 1241),
    V1_12("1.12", Protocols.V1_12, 1139, true),
    V1_11_2("1.11.2", Protocols.V1_11_2, 922),
    V1_11("1.11", Protocols.V1_11, 921, true),
    V1_10("1.10", Protocols.V1_10, 512, true),
    // the last value MUST be considered a "major release"
    ;

    private final int value;
    private final boolean majorRelease;
    private final String name;
    private final int dataVersion;

    ConnectionMode(String name, int value, int dataVersion) {
        this(name, value, dataVersion, false);
    }

    ConnectionMode(String name, int value, int dataVersion, boolean majorRelease) {
        this.value = value;
        this.majorRelease = majorRelease;
        this.name = name;
        this.dataVersion = dataVersion;
    }

    @Override
    public int getValue() {
        return value;
    }

    @Override
    public boolean isMajorRelease() {
        return majorRelease;
    }

    @Override
    public IProtocol getMajorRelease() {
        int i;
        //noinspection StatementWithEmptyBody
        for (i = ordinal(); !VALUES[i].majorRelease; i++)
            ;
        return VALUES[i];
    }

    @Override
    public List<IProtocol> getMinorReleases() {
        if (!majorRelease) {
            throw new UnsupportedOperationException("Cannot call IProtocol.getMinorReleases() on a non-major release");
        }
        int endIndex = ordinal();
        int startIndex;
        //noinspection StatementWithEmptyBody
        for (startIndex = endIndex - 1; startIndex >= 0 && !VALUES[startIndex].majorRelease; startIndex--)
            ;
        return Lists.reverse(Arrays.<IProtocol>asList(VALUES).subList(startIndex + 1, endIndex + 1));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDataVersion() {
        return dataVersion;
    }

    private static final ConnectionMode[] VALUES = values();
    private static final ConnectionMode[] PROTOCOLS = Arrays.stream(VALUES).filter(it -> it != AUTO).toArray(ConnectionMode[]::new);
    private static final Map<Integer, ConnectionMode> BY_VALUE = new HashMap<>();
    private static final Set<String> VALID_NAMES = new HashSet<>();

    public static ConnectionMode byValue(int value) {
        return BY_VALUE.getOrDefault(value, AUTO);
    }

    public static ConnectionMode[] protocolValues() {
        return PROTOCOLS;
    }

    public static boolean isSupportedProtocol(int protocol) {
        return byValue(protocol) != AUTO;
    }

    public static boolean isSupportedVersionName(String name) {
        return VALID_NAMES.contains(name);
    }

    public static void populateDropDownWidget(DropDownWidget<ConnectionMode> dropDown) {
        for (ConnectionMode mode : VALUES) {
            if (mode.majorRelease) {
                DropDownWidget<ConnectionMode>.Category category = dropDown.add(mode);
                List<IProtocol> children = mode.getMinorReleases();
                if (children.size() > 1) {
                    for (IProtocol child : children) {
                        category.add((ConnectionMode) child);
                    }
                }
            }
        }
    }

    static {
        for (ConnectionMode value : VALUES) {
            BY_VALUE.put(value.getValue(), value);
            VALID_NAMES.add(value.getName());
        }
    }

}
