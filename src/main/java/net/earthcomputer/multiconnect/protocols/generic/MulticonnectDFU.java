package net.earthcomputer.multiconnect.protocols.generic;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;

public class MulticonnectDFU {
    private MulticonnectDFU() {}

    public static final DSL.TypeReference DIMENSION = () -> "dimension";
    public static final DSL.TypeReference REGISTRY_MANAGER = () -> "registry_manager";

    public static final DataFixer FIXER = Util.make(() -> {
        var builder = new DataFixerBuilder(SharedConstants.getGameVersion().getWorldVersion());
        // TODO: the fixes
        return builder.build(Util.getBootstrapExecutor());
    });
}
