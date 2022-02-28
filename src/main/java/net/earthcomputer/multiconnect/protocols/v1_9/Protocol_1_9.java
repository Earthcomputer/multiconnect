package net.earthcomputer.multiconnect.protocols.v1_9;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.RegistryBuilder;
import net.earthcomputer.multiconnect.protocols.generic.RegistryMutator;
import net.earthcomputer.multiconnect.protocols.v1_9_1.Protocol_1_9_1;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.registry.Registry;

public class Protocol_1_9 extends Protocol_1_9_1 {

    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(GameJoinS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readInt(); // entity id
            buf.readUnsignedByte(); // hardcore + game mode
            buf.disablePassthroughMode();
            buf.pendingRead(Integer.class, (int)buf.readByte()); // dimension id
            buf.applyPendingReads();
        });
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_9, Registry.SOUND_EVENT_KEY, this::mutateSoundEventRegistry);
    }

    private void mutateSoundEventRegistry(RegistryBuilder<SoundEvent> registry) {
        registry.unregister(SoundEvents.ITEM_ELYTRA_FLYING);
    }
}
