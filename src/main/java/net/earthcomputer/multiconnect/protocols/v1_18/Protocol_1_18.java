package net.earthcomputer.multiconnect.protocols.v1_18;

import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.generic.TagRegistry;
import net.earthcomputer.multiconnect.protocols.v1_18_2.Protocol_1_18_2;
import net.earthcomputer.multiconnect.transformer.VarInt;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.tag.BlockTags;

public class Protocol_1_18 extends Protocol_1_18_2 {
    public static void registerTranslators() {
        ProtocolRegistry.registerInboundTranslator(RemoveEntityStatusEffectS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readUnsignedByte())); // effect id
            buf.applyPendingReads();
        });
        ProtocolRegistry.registerInboundTranslator(EntityStatusEffectS2CPacket.class, buf -> {
            buf.enablePassthroughMode();
            buf.readVarInt(); // entity id
            buf.disablePassthroughMode();
            buf.pendingRead(VarInt.class, new VarInt(buf.readByte() & 0xff)); // effect id
            buf.applyPendingReads();
        });
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.addTag(BlockTags.FALL_DAMAGE_RESETTING, BlockTags.CLIMBABLE);
        tags.add(BlockTags.FALL_DAMAGE_RESETTING, Blocks.SWEET_BERRY_BUSH, Blocks.COBWEB);
        super.addExtraBlockTags(tags);
    }
}
