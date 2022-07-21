package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class RegistryAccess1_18_2Fix extends NewExperimentalRegistryAccessFix {
    public RegistryAccess1_18_2Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.18.2");
    }

    @Override
    protected Dynamic<?> translateBiome(Dynamic<?> fromDynamic) {
        return fromDynamic;
    }
}
