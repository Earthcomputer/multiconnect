package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;

public class DynamicRegistries1_18Fix extends NewExperimentalDynamicRegistriesFix {
    public DynamicRegistries1_18Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.18");
    }

    @Override
    protected Dynamic<?> translateBiome(Dynamic<?> fromDynamic) {
        fromDynamic.remove("depth");
        fromDynamic.remove("scale");
        return fromDynamic;
    }
}
