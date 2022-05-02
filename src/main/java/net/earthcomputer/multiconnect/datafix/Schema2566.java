package net.earthcomputer.multiconnect.datafix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;

import java.util.Map;
import java.util.function.Supplier;

public class Schema2566 extends Schema {
    public Schema2566(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        schema.registerType(false, MulticonnectDFU.REGISTRY_MANAGER, DSL::remainder);
        schema.registerType(false, MulticonnectDFU.DIMENSION, DSL::remainder);
    }
}
