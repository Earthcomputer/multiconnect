package net.earthcomputer.multiconnect.connect;

import com.google.common.collect.Lists;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.Protocols;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Enum constants for each protocol, and the auto connection mode
 */
public final class ConnectionMode implements IProtocol {

    private static final List<ConnectionMode> ALL_MODES = new ArrayList<>();
    private static ConnectionMode @Nullable [] protocols = null;
    private static final Map<Integer, ConnectionMode> BY_VALUE = new HashMap<>();
    private static final Set<String> VALID_NAMES = new HashSet<>();
    private static int nextOrdinal = 0;

    // Protocols should go in reverse chronological order
    public static final ConnectionMode AUTO = register("Auto", -1, -1, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_19_3 = register("1.19.3", Protocols.V1_19_3, 3218);
    public static final ConnectionMode V1_19_2 = register("1.19.2", Protocols.V1_19_2, 3117);
    public static final ConnectionMode V1_19 = register("1.19", Protocols.V1_19, 3105, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_18_2 = register("1.18.2", Protocols.V1_18_2, 2975);
    public static final ConnectionMode V1_18 = register("1.18", Protocols.V1_18, 2865, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_17_1 = register("1.17.1", Protocols.V1_17_1, 2730);
    public static final ConnectionMode V1_17 = register("1.17", Protocols.V1_17, 2724, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_16_5 = register("1.16.5", Protocols.V1_16_5, 2584);
    public static final ConnectionMode V1_16_3 = register("1.16.3", Protocols.V1_16_3, 2580);
    public static final ConnectionMode V1_16_2 = register("1.16.2", Protocols.V1_16_2, 2578);
    public static final ConnectionMode V1_16_1 = register("1.16.1", Protocols.V1_16_1, 2567);
    public static final ConnectionMode V1_16 = register("1.16", Protocols.V1_16, 2566, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_15_2 = register("1.15.2", Protocols.V1_15_2, 2230);
    public static final ConnectionMode V1_15_1 = register("1.15.1", Protocols.V1_15_1, 2227);
    public static final ConnectionMode V1_15 = register("1.15", Protocols.V1_15, 2225, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_14_4 = register("1.14.4", Protocols.V1_14_4, 1976);
    public static final ConnectionMode V1_14_3 = register("1.14.3", Protocols.V1_14_3, 1968);
    public static final ConnectionMode V1_14_2 = register("1.14.2", Protocols.V1_14_2, 1963);
    public static final ConnectionMode V1_14_1 = register("1.14.1", Protocols.V1_14_1, 1957);
    public static final ConnectionMode V1_14 = register("1.14", Protocols.V1_14, 1952, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_13_2 = register("1.13.2", Protocols.V1_13_2, 1631);
    public static final ConnectionMode V1_13_1 = register("1.13.1", Protocols.V1_13_1, 1628);
    public static final ConnectionMode V1_13 = register("1.13", Protocols.V1_13, 1519, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_12_2 = register("1.12.2", Protocols.V1_12_2, 1343, InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_12_1 = register("1.12.1", Protocols.V1_12_1, 1241, InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_12 = register("1.12", Protocols.V1_12, 1139, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_11_2 = register("1.11.2", Protocols.V1_11_2, 922);
    public static final ConnectionMode V1_11 = register("1.11", Protocols.V1_11, 921, InitFlags.MAJOR_RELEASE);
    public static final ConnectionMode V1_10 = register("1.10", Protocols.V1_10, 512, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_9_4 = register("1.9.4", Protocols.V1_9_4, 184, InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_9_2 = register("1.9.2", Protocols.V1_9_2, 176, InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_9_1 = register("1.9.1", Protocols.V1_9_1, 175, InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_9 = register("1.9", Protocols.V1_9, 169, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA);
    public static final ConnectionMode V1_8 = register("1.8", Protocols.V1_8, 99, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA);
    // the last value MUST be considered a "major release"

    public static class InitFlags {
        private static final int MAJOR_RELEASE = 1;
        private static final int MULTICONNECT_BETA = 2;
    }

    private final int value;
    private final boolean majorRelease;
    private final String name;
    private final String majorReleaseName;
    private final int dataVersion;
    private final boolean multiconnectBeta;
    private final int ordinal = nextOrdinal++;

    private ConnectionMode(String name, int value, int dataVersion) {
        this(name, value, dataVersion, 0);
    }

    private ConnectionMode(String name, int value, int dataVersion, int initializationFlags) {
        this(name, value, dataVersion, name, initializationFlags);
    }

    private ConnectionMode(String name, int value, int dataVersion, String majorReleaseName, int initializationFlags) {
        this.value = value;
        this.majorRelease = (initializationFlags & InitFlags.MAJOR_RELEASE) != 0;
        this.name = name;
        this.dataVersion = dataVersion;
        this.majorReleaseName = majorReleaseName;
        this.multiconnectBeta = (initializationFlags & InitFlags.MULTICONNECT_BETA) != 0;
    }

    public int ordinal() {
        return ordinal;
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
    public ConnectionMode getMajorRelease() {
        int i;
        //noinspection StatementWithEmptyBody
        for (i = ordinal(); !ALL_MODES.get(i).majorRelease; i++)
            ;
        return ALL_MODES.get(i);
    }

    @Override
    public String getMajorReleaseName() {
        return getMajorRelease().majorReleaseName;
    }

    @Override
    public List<? extends IProtocol> getMinorReleases() {
        if (!majorRelease) {
            throw new UnsupportedOperationException("Cannot call IProtocol.getMinorReleases() on a non-major release");
        }
        int endIndex = ordinal();
        int startIndex;
        //noinspection StatementWithEmptyBody
        for (startIndex = endIndex - 1; startIndex >= 0 && !ALL_MODES.get(startIndex).majorRelease; startIndex--)
            ;
        return Lists.reverse(ALL_MODES.subList(startIndex + 1, endIndex + 1));
    }

    @Override
    public boolean isMulticonnectBeta() {
        return multiconnectBeta;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDataVersion() {
        return dataVersion;
    }

    public static ConnectionMode byValue(int value) {
        return getByValueMap().getOrDefault(value, AUTO);
    }

    /**
     * Newest to oldest
     */
    public static ConnectionMode[] protocolValues() {
        if (protocols == null) {
            protocols = ALL_MODES.stream().filter(it -> it != AUTO).toArray(ConnectionMode[]::new);
        }
        return protocols;
    }

    private static Map<Integer, ConnectionMode> getByValueMap() {
        if (BY_VALUE.isEmpty()) {
            for (final ConnectionMode value : ALL_MODES) {
                BY_VALUE.put(value.getValue(), value);
            }
        }
        return BY_VALUE;
    }

    private static Set<String> getValidNames() {
        if (VALID_NAMES.isEmpty()) {
            for (final ConnectionMode value : ALL_MODES) {
                VALID_NAMES.add(value.getName());
            }
        }
        return VALID_NAMES;
    }

    public static boolean isSupportedProtocol(int protocol) {
        return byValue(protocol) != AUTO;
    }

    public static boolean isSupportedVersionName(String name) {
        return getValidNames().contains(name);
    }

    public static ConnectionMode[] values() { // Binary compatibility
        return ALL_MODES.toArray(ConnectionMode[]::new);
    }

    private static ConnectionMode register(ConnectionMode mode) {
        ALL_MODES.add(mode);

        // Now clear these caches
        protocols = null;
        BY_VALUE.clear();
        VALID_NAMES.clear();

        return mode;
    }

    public static ConnectionMode register(String name, int value, int dataVersion) {
        return register(new ConnectionMode(name, value, dataVersion));
    }

    public static ConnectionMode register(String name, int value, int dataVersion, int initializationFlags) {
        return register(new ConnectionMode(name, value, dataVersion, initializationFlags));
    }

    public static ConnectionMode register(String name, int value, int dataVersion, String majorReleaseName, int initializationFlags) {
        return register(new ConnectionMode(name, value, dataVersion, majorReleaseName, initializationFlags));
    }
}
