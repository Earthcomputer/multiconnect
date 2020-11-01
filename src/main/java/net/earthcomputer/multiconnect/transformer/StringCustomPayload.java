package net.earthcomputer.multiconnect.transformer;

public final class StringCustomPayload {
    private final String value;

    public StringCustomPayload(String value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != StringCustomPayload.class) return false;
        StringCustomPayload that = (StringCustomPayload) obj;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return "StringCustomPayload{" + value + "}";
    }
}
