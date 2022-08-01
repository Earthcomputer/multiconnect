package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.schemas.Schema;

public class RegistryAccess1_17Fix extends OldExperimentalRegistryAccessFix {
    public RegistryAccess1_17Fix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType, "1.17");
    }
}
