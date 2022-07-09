package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import net.minecraft.SharedConstants;
import net.minecraft.Util;

public class MulticonnectDFU {
    private MulticonnectDFU() {}

    public static final DSL.TypeReference DIMENSION = () -> "dimension";
    public static final DSL.TypeReference REGISTRY_ACCESS = () -> "registry_access";
    public static final DSL.TypeReference STATUS_EFFECT_FACTOR_DATA = () -> "status_effect_factor_data";

    public static final DataFixer FIXER = Util.make(() -> {
        var builder = new DataFixerBuilder(SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        builder.addSchema(99, Schema99::new);
        Schema schema16_2 = builder.addSchema(2578, Schema::new);
        builder.addFixer(new RegistryAccess1_16_2Fix(schema16_2, true));
        Schema schema_17 = builder.addSchema(2724, Schema::new);
        builder.addFixer(new RegistryAccess1_17Fix(schema_17, true));
        Schema schema_18 = builder.addSchema(2865, Schema::new);
        builder.addFixer(new RegistryAccess1_18Fix(schema_18, true));
        Schema schema_18_2 = builder.addSchema(2975, Schema::new);
        builder.addFixer(new RegistryAccess1_18_2Fix(schema_18_2, true));
        Schema schema_19 = builder.addSchema(3103, Schema::new);
        builder.addFixer(new RegistryAccess1_19Fix(schema_19, true));
        return builder.buildOptimized(Util.bootstrapExecutor());
    });
}
