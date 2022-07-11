package net.earthcomputer.multiconnect.protocols.v1_12;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateData;

public class BlockStateReverseData {

    public static final Map<ResourceLocation, List<String>> OLD_PROPERTIES = new HashMap<>();
    public static final Map<Pair<ResourceLocation, String>, List<String>> OLD_PROPERTY_VALUES = new HashMap<>();

    static {
        // load block state flattening class
        BlockStateData.upgradeBlock(0);
    }

}
