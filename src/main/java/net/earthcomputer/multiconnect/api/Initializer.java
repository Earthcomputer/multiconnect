package net.earthcomputer.multiconnect.api;

import net.minecraftforge.fml.common.Mod;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

/**
 * Used to inject MultiConnect into Forge
 */
@Mod("multiconnect")
public class Initializer implements IMixinConnector {

    @Override
    public void connect() {
        MixinBootstrap.init();
        Mixins.addConfigurations("multiconnect.mixins.json",
                "multiconnect.1_14_4.mixins.json",
                "multiconnect.1_14_3.mixins.json",
                "multiconnect.1_14_1.mixins.json",
                "multiconnect.1_14.mixins.json",
                "multiconnect.1_13_2.mixins.json",
                "multiconnect.1_13.mixins.json",
                "multiconnect.1_12_2.mixins.json",
                "multiconnect.1_12_1.mixins.json",
                "multiconnect.1_12.mixins.json");
    }

}

