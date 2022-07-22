package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;

public class RegistryAccess1_16_2Fix extends OldExperimentalRegistryAccessFix {
    public RegistryAccess1_16_2Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.16.2");
    }
}
