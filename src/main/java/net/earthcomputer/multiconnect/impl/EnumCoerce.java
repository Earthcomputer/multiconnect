package net.earthcomputer.multiconnect.impl;

import java.util.EnumMap;

public final class EnumCoerce<T extends Enum<T>, U extends Enum<U>> {
    private final EnumMap<T, U> map;

    public EnumCoerce(Class<T> tClass, Class<U> uClass) {
        map = new EnumMap<>(tClass);
        for (T tConst : tClass.getEnumConstants()) {
            for (U uConst : uClass.getEnumConstants()) {
                if (tConst.name().equals(uConst.name())) {
                    map.put(tConst, uConst);
                    break;
                }
            }
        }
    }

    public U coerce(T tConst) {
        return map.get(tConst);
    }
}
