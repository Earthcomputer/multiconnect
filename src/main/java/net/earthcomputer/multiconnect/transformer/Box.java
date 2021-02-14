package net.earthcomputer.multiconnect.transformer;

abstract class Box<T> {

    private final T value;

    public Box(T value) {
        this.value = value;
    }

    public final T get() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other == null) return false;
        if (other.getClass() != getClass()) return false;
        Box<?> that = (Box<?>) other;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

}
