package net.earthcomputer.multiconnect.mixin.bridge;

import net.earthcomputer.multiconnect.protocols.generic.IServerboundSlotPacket;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ClickSlotC2SPacket.class)
public class MixinClickSlotC2SPacket implements IServerboundSlotPacket {
    @Shadow @Final private int slot;

    @Unique private boolean processed = false;

    @Override
    public boolean multiconnect_isProcessed() {
        return processed;
    }

    @Override
    public void multiconnect_setProcessed() {
        this.processed = true;
    }

    @Override
    public int multiconnect_getSlotId() {
        return slot;
    }
}
