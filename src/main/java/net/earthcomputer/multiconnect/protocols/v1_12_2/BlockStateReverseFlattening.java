package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.datafixers.Dynamic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.datafix.fixes.BlockStateFlatteningMap;
import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockStateReverseFlattening {

    public static final Dynamic[] IDS_TO_OLD_STATES = new Dynamic[4096];
    public static final Map<ResourceLocation, List<String>> OLD_PROPERTIES = new HashMap<>();
    public static final Map<Pair<ResourceLocation, String>, List<String>> OLD_PROPERTY_VALUES = new HashMap<>();

    public static Dynamic<?> reverseLookupState(int stateId) {
        if (stateId < 0 || stateId >= IDS_TO_OLD_STATES.length)
            return IDS_TO_OLD_STATES[0];
        Dynamic<?> ret = IDS_TO_OLD_STATES[stateId];
        if (ret == null)
            return IDS_TO_OLD_STATES[0];
        return ret;
    }

    public static String reverseLookupStateBlock(int stateId) {
        if (stateId < 0 || stateId >= IDS_TO_OLD_STATES.length)
            return "minecraft:air";
        Dynamic<?> val = IDS_TO_OLD_STATES[stateId];
        if (val == null)
            return "minecraft:air";
        return val.get("Name").asString("");
    }

    static {
        // load block state flattening class
        BlockStateFlatteningMap.updateId(0);
    }

}
