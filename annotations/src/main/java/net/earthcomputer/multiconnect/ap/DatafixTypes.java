package net.earthcomputer.multiconnect.ap;

public enum DatafixTypes {
    BLOCK_ENTITY(false),
    DIMENSION(true),
    REGISTRY_MANAGER(true),
    STATUS_EFFECT_FACTOR_DATA(true),
    ;

    private final boolean isMulticonnect;

    DatafixTypes(boolean isMulticonnect) {
        this.isMulticonnect = isMulticonnect;
    }

    public boolean isMulticonnect() {
        return isMulticonnect;
    }
}
