package net.earthcomputer.multiconnect.connect;

import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.IProtocolExt;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_10.Protocol_1_10;
import net.earthcomputer.multiconnect.protocols.v1_11.Protocol_1_11_2;
import net.earthcomputer.multiconnect.protocols.v1_12.Protocol_1_12_2;
import net.earthcomputer.multiconnect.protocols.v1_14.Protocol_1_14_4;
import net.earthcomputer.multiconnect.protocols.v1_15.Protocol_1_15_2;
import net.earthcomputer.multiconnect.protocols.v1_16.Protocol_1_16_5;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9_2;
import net.earthcomputer.multiconnect.protocols.v1_9.Protocol_1_9_4;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Enum constants for each protocol, and the auto connection mode
 */
public enum ConnectionMode implements IProtocolExt {

    // Protocols should go in reverse chronological order
    AUTO("Auto", -1, -1, InitFlags.MAJOR_RELEASE),
    V1_19_3("1.19.3", Protocols.V1_19_3, 3218),
    V1_19_2("1.19.2", Protocols.V1_19_2, 3117),
    V1_19("1.19", Protocols.V1_19, 3105, InitFlags.MAJOR_RELEASE),
    V1_18_2("1.18.2", Protocols.V1_18_2, 2975),
    V1_18("1.18", Protocols.V1_18, 2865, InitFlags.MAJOR_RELEASE),
    V1_17_1("1.17.1", Protocols.V1_17_1, 2730),
    V1_17("1.17", Protocols.V1_17, 2724, InitFlags.MAJOR_RELEASE),
    V1_16_5("1.16.5", Protocols.V1_16_5, 2584, new Protocol_1_16_5(), InitFlags.NONE),
    V1_16_3("1.16.3", Protocols.V1_16_3, 2580),
    V1_16_2("1.16.2", Protocols.V1_16_2, 2578),
    V1_16_1("1.16.1", Protocols.V1_16_1, 2567),
    V1_16("1.16", Protocols.V1_16, 2566, InitFlags.MAJOR_RELEASE),
    V1_15_2("1.15.2", Protocols.V1_15_2, 2230, new Protocol_1_15_2(), InitFlags.NONE),
    V1_15_1("1.15.1", Protocols.V1_15_1, 2227),
    V1_15("1.15", Protocols.V1_15, 2225, InitFlags.MAJOR_RELEASE),
    V1_14_4("1.14.4", Protocols.V1_14_4, 1976, new Protocol_1_14_4(), InitFlags.NONE),
    V1_14_3("1.14.3", Protocols.V1_14_3, 1968),
    V1_14_2("1.14.2", Protocols.V1_14_2, 1963),
    V1_14_1("1.14.1", Protocols.V1_14_1, 1957),
    V1_14("1.14", Protocols.V1_14, 1952, InitFlags.MAJOR_RELEASE),
    V1_13_2("1.13.2", Protocols.V1_13_2, 1631),
    V1_13_1("1.13.1", Protocols.V1_13_1, 1628),
    V1_13("1.13", Protocols.V1_13, 1519, InitFlags.MAJOR_RELEASE),
    V1_12_2("1.12.2", Protocols.V1_12_2, 1343, new Protocol_1_12_2(), InitFlags.MULTICONNECT_BETA),
    V1_12_1("1.12.1", Protocols.V1_12_1, 1241, InitFlags.MULTICONNECT_BETA),
    V1_12("1.12", Protocols.V1_12, 1139, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA),
    V1_11_2("1.11.2", Protocols.V1_11_2, 922, new Protocol_1_11_2(), InitFlags.MULTICONNECT_BETA),
    V1_11("1.11", Protocols.V1_11, 921, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA),
    V1_10("1.10", Protocols.V1_10, 512, new Protocol_1_10(), InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA),
    V1_9_4("1.9.4", Protocols.V1_9_4, 184, new Protocol_1_9_4(), InitFlags.MULTICONNECT_BETA),
    V1_9_2("1.9.2", Protocols.V1_9_2, 176, new Protocol_1_9_2(), InitFlags.MULTICONNECT_BETA),
    V1_9_1("1.9.1", Protocols.V1_9_1, 175, InitFlags.MULTICONNECT_BETA),
    V1_9("1.9", Protocols.V1_9, 169, InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA),
    V1_8("1.8", Protocols.V1_8, 99, new Protocol_1_8(), InitFlags.MAJOR_RELEASE | InitFlags.MULTICONNECT_BETA),
    // the last value MUST be considered a "major release"
    ;

    private static class InitFlags {
        private static final int NONE = 0;
        private static final int MAJOR_RELEASE = 1;
        private static final int MULTICONNECT_BETA = 2;
    }

    private final int value;
    private final boolean majorRelease;
    private final String name;
    private final String majorReleaseName;
    private final int dataVersion;
    private final boolean multiconnectBeta;

    ConnectionMode(String name, int value, int dataVersion) {
        this(name, value, dataVersion, InitFlags.NONE);
    }

    ConnectionMode(String name, int value, int dataVersion, int initializationFlags) {
        this(name, value, dataVersion, null, initializationFlags);
    }

    ConnectionMode(String name, int value, int dataVersion, @Nullable ProtocolBehavior behavior, int initializationFlags) {
        this(name, value, dataVersion, name, behavior, initializationFlags);
    }

    ConnectionMode(String name, int value, int dataVersion, String majorReleaseName, @Nullable ProtocolBehavior behavior, int initializationFlags) {
        this.value = value;
        this.majorRelease = (initializationFlags & InitFlags.MAJOR_RELEASE) != 0;
        this.name = name;
        this.dataVersion = dataVersion;
        this.majorReleaseName = majorReleaseName;
        this.multiconnectBeta = (initializationFlags & InitFlags.MULTICONNECT_BETA) != 0;
        if (value != -1) {
            ProtocolRegistry.register(this, behavior);
        }
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
    public IProtocolExt getMajorRelease() {
        return ProtocolRegistry.getMajorRelease(this);
    }

    @Override
    public String getMajorReleaseName() {
        return getMajorRelease().getOverriddenMajorReleaseName();
    }

    @Override
    public String getOverriddenMajorReleaseName() {
        return majorReleaseName;
    }

    @Override
    public List<IProtocol> getMinorReleases() {
        return ProtocolRegistry.getMinorReleases(this);
    }

    @Override
    public boolean isMulticonnectBeta() {
        return multiconnectBeta;
    }

    @Override
    public boolean isMulticonnectExtension() {
        return false;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDataVersion() {
        return dataVersion;
    }
}