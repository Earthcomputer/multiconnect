package net.earthcomputer.multiconnect.impl;

import net.earthcomputer.multiconnect.api.IProtocol;

public interface IProtocolExt extends IProtocol {
    String getOverriddenMajorReleaseName();
    int getSortingIndex();
    boolean isTranslatable();
    String getTranslationKey();
    boolean isMajorReleaseTranslatable();
    String getMajorReleaseTranslationKey();
}
