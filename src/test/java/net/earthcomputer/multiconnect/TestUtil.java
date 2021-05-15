package net.earthcomputer.multiconnect;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;

public class TestUtil {
    public static void callBootstrap() {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        Bootstrap.getMissingTranslations(); // forces initialization of translation keys
    }
}
