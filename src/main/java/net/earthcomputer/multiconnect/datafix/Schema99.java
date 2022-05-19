package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

public class Schema99 extends Schema {
    public Schema99(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        // register a dummy recursive type, DFU crashes otherwise.
        // see https://github.com/Mojang/DataFixerUpper/issues/45
        schema.registerType(true, () -> "dummy", DSL::remainder);

        schema.registerType(false, MulticonnectDFU.REGISTRY_MANAGER, DSL::remainder);
        schema.registerType(false, MulticonnectDFU.DIMENSION, DSL::remainder);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        return Collections.emptyMap();
    }
}
