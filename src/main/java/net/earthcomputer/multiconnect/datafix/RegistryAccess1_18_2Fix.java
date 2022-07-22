package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;

public class RegistryAccess1_18_2Fix extends NewExperimentalRegistryAccessFix {
    public RegistryAccess1_18_2Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.18.2");
    }
}
