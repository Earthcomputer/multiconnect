package net.earthcomputer.multiconnect.api;

import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface CustomProtocolBuilder {
    CustomProtocolBuilder majorVersion();
    CustomProtocolBuilder markBeta();
    CustomProtocolBuilder behavior(ProtocolBehavior behavior);
    CustomProtocolBuilder majorReleaseName(String name);
    CustomProtocolBuilder sortingIndex(int sortingIndex);
    CustomProtocolBuilder translationKey(String translationKey);
    CustomProtocolBuilder majorReleaseTranslationKey(String translationKey);
    IProtocol register();
}
