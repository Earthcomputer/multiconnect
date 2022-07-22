package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;

public class RegistryAccess1_19Fix extends NewExperimentalRegistryAccessFix {
    public RegistryAccess1_19Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.19");
    }
}
