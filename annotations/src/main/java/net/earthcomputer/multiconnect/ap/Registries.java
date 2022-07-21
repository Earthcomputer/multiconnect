package net.earthcomputer.multiconnect.ap;

public enum Registries {
    BANNER_PATTERN,
    BLOCK,
    BLOCK_ENTITY_TYPE,
    BLOCK_STATE(false),
    CAT_VARIANT,
    COMMAND_ARGUMENT_TYPE,
    CUSTOM_STAT,
    ENCHANTMENT,
    ENTITY_POSE(false),
    ENTITY_TYPE,
    FLUID,
    FROG_VARIANT,
    GAME_EVENT,
    INSTRUMENT,
    ITEM,
    MENU,
    PAINTING_VARIANT,
    PARTICLE_TYPE,
    POINT_OF_INTEREST_TYPE,
    POSITION_SOURCE_TYPE,
    RECIPE_SERIALIZER,
    SOUND_EVENT,
    STAT_TYPE,
    MOB_EFFECT,
    TRACKED_DATA_HANDLER(false),
    VILLAGER_PROFESSION,
    VILLAGER_TYPE,
    ;

    private final boolean isRealRegistry;
    private final String resourceKeyFieldName;

    Registries() {
        this.resourceKeyFieldName = name() + "_REGISTRY";
        this.isRealRegistry = true;
    }

    Registries(String resourceKeyFieldName) {
        this.resourceKeyFieldName = resourceKeyFieldName;
        this.isRealRegistry = true;
    }

    Registries(boolean isRealRegistry) {
        this.resourceKeyFieldName = name() + "_REGISTRY";
        this.isRealRegistry = isRealRegistry;
    }

    public boolean isRealRegistry() {
        return isRealRegistry;
    }

    public String getResourceKeyFieldName() {
        return resourceKeyFieldName;
    }
}
