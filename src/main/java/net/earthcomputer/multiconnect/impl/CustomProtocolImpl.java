package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.CustomProtocolBuilder;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class CustomProtocolImpl implements CustomProtocolBuilder, IProtocolExt {
    private final int version;
    private final String name;
    private final int dataVersion;
    private boolean majorVersion = false;
    private boolean beta = false;
    private String majorReleaseName;
    @Nullable
    private ProtocolBehavior behavior;

    public CustomProtocolImpl(int version, String name, int dataVersion) {
        this.version = version;
        this.name = name;
        this.dataVersion = dataVersion;
        this.majorReleaseName = name;
    }

    // region CustomProtocolBuilder impl

    @Override
    public CustomProtocolBuilder majorVersion() {
        this.majorVersion = true;
        return this;
    }

    @Override
    public CustomProtocolBuilder markBeta() {
        this.beta = true;
        return this;
    }

    @Override
    public CustomProtocolBuilder behavior(ProtocolBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    @Override
    public CustomProtocolBuilder majorReleaseName(String name) {
        this.majorReleaseName = name;
        return this;
    }

    @Override
    public IProtocol register() {
        ProtocolRegistry.register(this, behavior);
        return this;
    }

    // endregion

    // region IProtocolExt impl

    @Override
    public int getValue() {
        return version;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getDataVersion() {
        return dataVersion;
    }

    @Override
    public boolean isMajorRelease() {
        return majorVersion;
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
        return beta;
    }

    @Override
    public boolean isMulticonnectExtension() {
        return true;
    }

    // endregion
}
