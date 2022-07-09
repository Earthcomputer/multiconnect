package net.earthcomputer.multiconnect;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

public class TestUtil {
    public static void callBootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Bootstrap.getMissingTranslations(); // forces initialization of translation keys
    }
}
