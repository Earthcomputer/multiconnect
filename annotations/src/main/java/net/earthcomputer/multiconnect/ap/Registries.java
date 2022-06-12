package net.earthcomputer.multiconnect.ap;

public enum Registries {
    BLOCK,
    BLOCK_ENTITY_TYPE,
    BLOCK_STATE(false),
    CAT_VARIANT,
    COMMAND_ARGUMENT_TYPE,
    CUSTOM_STAT,
    ENTITY_POSE(false),
    ENTITY_TYPE,
    FLUID,
    FROG_VARIANT,
    GAME_EVENT,
    ITEM,
    PAINTING_VARIANT,
    PARTICLE_TYPE,
    POSITION_SOURCE_TYPE,
    RECIPE_SERIALIZER,
    SCREEN_HANDLER("MENU_KEY"),
    SOUND_EVENT,
    STAT_TYPE,
    STATUS_EFFECT("MOB_EFFECT_KEY"),
    TRACKED_DATA_HANDLER(false),
    VILLAGER_PROFESSION,
    VILLAGER_TYPE,
    ;

    private final boolean isRealRegistry;
    private final String registryKeyFieldName;

    Registries() {
        this.registryKeyFieldName = name() + "_KEY";
        this.isRealRegistry = true;
    }

    Registries(String registryKeyFieldName) {
        this.registryKeyFieldName = registryKeyFieldName;
        this.isRealRegistry = true;
    }

    Registries(boolean isRealRegistry) {
        this.registryKeyFieldName = name() + "_KEY";
        this.isRealRegistry = isRealRegistry;
    }

    public boolean isRealRegistry() {
        return isRealRegistry;
    }

    public String getRegistryKeyFieldName() {
        return registryKeyFieldName;
    }
}
