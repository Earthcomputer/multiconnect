package net.earthcomputer.multiconnect.ap;

public enum Registries {
    BLOCK,
    BLOCK_STATE,
    CUSTOM_STAT,
    ENTITY_TYPE,
    FLUID,
    GAME_EVENT,
    ITEM,
    MOTIVE,
    PARTICLE_TYPE,
    POSITION_SOURCE_TYPE,
    RECIPE_SERIALIZER,
    SCREEN_HANDLER("MENU_KEY"),
    SOUND_EVENT,
    STAT_TYPE,
    STATUS_EFFECT("MOB_EFFECT_KEY"),
    VILLAGER_PROFESSION,
    VILLAGER_TYPE,
    ;

    private final String registryKeyFieldName;

    Registries() {
        this.registryKeyFieldName = name() + "_KEY";
    }

    Registries(String registryKeyFieldName) {
        this.registryKeyFieldName = registryKeyFieldName;
    }

    public String getRegistryKeyFieldName() {
        return registryKeyFieldName;
    }
}
