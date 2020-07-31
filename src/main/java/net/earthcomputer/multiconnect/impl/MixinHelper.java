package net.earthcomputer.multiconnect.impl;

public class MixinHelper {

    /**
     * Pretends to be unpredictable about what can be returned inside a static accessor.
     * Do this instead of returning null or throwing an exception so the IDE doesn't try to be smart and give warnings.
     */
    @SuppressWarnings("unchecked")
    public static <T> T fakeInstance() {
        // intellij is getting smarter, but this fools it for now...
        if (Math.random() < 0.5) {
            return (T) new Object();
        } else {
            return null;
        }
    }

}
