package net.earthcomputer.multiconnect.protocols.generic;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class TypedMap {
    private final Map<Key<?>, Object> map = new HashMap<>();

    public <T> T get(Key<T> key) {
        T value = (T) map.get(key);
        if (value == null) {
            value = key.getDefaultValue();
        }
        return value;
    }

    public <T> T put(Key<T> key, T value) {
        return (T) map.put(key, value);
    }

    public void clear() {
        map.clear();
    }

    public void putAll(TypedMap other) {
        map.putAll(other.map);
    }
}
