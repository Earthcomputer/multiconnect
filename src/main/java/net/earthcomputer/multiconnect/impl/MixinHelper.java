package net.earthcomputer.multiconnect.impl;

public class MixinHelper {

    /**
     * Pretends to create an instance of an object inside a static accessor.
     * Do this instead of returning null so the IDE doesn't complain about nullability
     */
    @SuppressWarnings("unchecked")
    public static <T> T fakeInstance() {
        return (T) new Object();
    }

}
