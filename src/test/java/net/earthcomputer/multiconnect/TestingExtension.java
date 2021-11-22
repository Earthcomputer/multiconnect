package net.earthcomputer.multiconnect;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.fabricmc.loader.impl.launch.knot.KnotClassLoaderInterface;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class TestingExtension implements BeforeAllCallback, Function<Class<?>, Class<?>> {

    private static ClassLoader knotClassLoader;

    private static AtomicBoolean isSetup = new AtomicBoolean(false);
    private static AtomicBoolean isSettingUp = new AtomicBoolean(true);
    private static void setup() {
        if (isSetup.getAndSet(true)) {
            while (isSettingUp.get()) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            Thread.currentThread().setContextClassLoader(knotClassLoader);
            return;
        }
        System.setProperty("fabric.development", "true");
        System.setProperty("fabric.loader.entrypoint", "net.earthcomputer.multiconnect.TestingDummyMain");
        System.setProperty("multiconnect.unitTestMode", "true");
        Knot knot = new Knot(EnvType.CLIENT, null);
        knot.init(new String[0]);
        knotClassLoader = knot.getClassLoader();
        ((KnotClassLoaderInterface) knotClassLoader).addClassLoaderExclusion("net.earthcomputer.multiconnect.TestingExtension");
        isSettingUp.set(false);
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        setup();
    }

    @Override
    public Class<?> apply(Class<?> clazz) {
        setup();
        try {
            return knotClassLoader.loadClass(clazz.getName());
        } catch (ClassNotFoundException e) {
            return clazz;
        }
    }
}
