package net.earthcomputer.multiconnect.protocols.v1_12_2;

import com.mojang.datafixers.Dynamic;
import net.minecraft.datafixer.fix.BlockStateFlattening;

public class BlockStateReverseFlattening {

    public static final Dynamic[] IDS_TO_OLD_STATES = new Dynamic[4096];

    public static Dynamic<?> reverseLookupState(int stateId) {
        if (stateId < 0 || stateId >= IDS_TO_OLD_STATES.length)
            return IDS_TO_OLD_STATES[0];
        Dynamic<?> ret = IDS_TO_OLD_STATES[stateId];
        if (ret == null)
            return IDS_TO_OLD_STATES[0];
        return ret;
    }

    static {
        // load block state flattening class
        BlockStateFlattening.lookupStateBlock(0);
    }

}
