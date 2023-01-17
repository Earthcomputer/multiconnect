package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.CustomProtocolBuilder;
import net.earthcomputer.multiconnect.api.IProtocol;
import net.earthcomputer.multiconnect.api.ProtocolBehavior;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class CustomProtocolImpl implements CustomProtocolBuilder, IProtocolExt {
    private final int version;
    private final String name;
    private final int dataVersion;
    private boolean majorVersion = false;
    private boolean beta = false;
    private String majorReleaseName;
    private int sortingIndex;
    @Nullable
    private String translationKey;
    @Nullable
    private String majorReleaseTranslationKey;
    @Nullable
    private ProtocolBehavior behavior;

    public CustomProtocolImpl(int version, String name, int dataVersion) {
        this.version = version;
        this.name = name;
        this.dataVersion = dataVersion;
        this.majorReleaseName = name;
        this.sortingIndex = version;
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
    public CustomProtocolBuilder sortingIndex(int sortingIndex) {
        this.sortingIndex = sortingIndex;
        return this;
    }

    @Override
    public CustomProtocolBuilder translationKey(String translationKey) {
        this.translationKey = translationKey;
        if (majorReleaseTranslationKey == null) {
            majorReleaseTranslationKey = translationKey;
        }
        return this;
    }

    @Override
    public CustomProtocolBuilder majorReleaseTranslationKey(String translationKey) {
        this.majorReleaseTranslationKey = translationKey;
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
    public int getSortingIndex() {
        return sortingIndex;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isTranslatable() {
        return translationKey != null;
    }

    @Override
    public String getTranslationKey() {
        return Objects.requireNonNull(translationKey);
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
    public boolean isMajorReleaseTranslatable() {
        return majorReleaseTranslationKey != null;
    }

    @Override
    public String getMajorReleaseTranslationKey() {
        return Objects.requireNonNull(majorReleaseTranslationKey);
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
