package net.earthcomputer.multiconnect.protocols.v1_13;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ISimpleRegistry;
import net.earthcomputer.multiconnect.impl.RegistryMutator;
import net.earthcomputer.multiconnect.impl.TagRegistry;
import net.earthcomputer.multiconnect.protocols.ProtocolRegistry;
import net.earthcomputer.multiconnect.protocols.v1_13_1.Protocol_1_13_1;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.TntBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.BookUpdateC2SPacket;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Hand;
import net.minecraft.util.registry.Registry;

public class Protocol_1_13 extends Protocol_1_13_1 {

    public static void registerTranslators() {
        ProtocolRegistry.registerOutboundTranslator(BookUpdateC2SPacket.class, buf -> {
            buf.passthroughWrite(ItemStack.class); // book item
            buf.passthroughWrite(Boolean.class); // signed
            buf.skipWrite(Hand.class); // hand
        });
    }

    @Override
    public void mutateRegistries(RegistryMutator mutator) {
        super.mutateRegistries(mutator);
        mutator.mutate(Protocols.V1_13, Registry.BLOCK, this::modifyBlockRegistry);
    }

    private void modifyBlockRegistry(ISimpleRegistry<Block> registry) {
        registry.unregister(Blocks.DEAD_TUBE_CORAL);
        registry.unregister(Blocks.DEAD_BRAIN_CORAL);
        registry.unregister(Blocks.DEAD_HORN_CORAL);
        registry.unregister(Blocks.DEAD_BUBBLE_CORAL);
        registry.unregister(Blocks.DEAD_FIRE_CORAL);
    }

    @Override
    public boolean acceptBlockState(BlockState state) {
        if (state.getBlock() == Blocks.TNT && state.get(TntBlock.UNSTABLE))
            return false;
        return super.acceptBlockState(state);
    }

    @Override
    public void addExtraBlockTags(TagRegistry<Block> tags) {
        tags.add(BlockTags.UNDERWATER_BONEMEALS, Blocks.SEAGRASS);
        tags.addTag(BlockTags.UNDERWATER_BONEMEALS, BlockTags.CORALS);
        tags.addTag(BlockTags.UNDERWATER_BONEMEALS, BlockTags.WALL_CORALS);
        tags.add(BlockTags.CORAL_PLANTS, Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL, Blocks.BUBBLE_CORAL, Blocks.FIRE_CORAL, Blocks.HORN_CORAL);
        super.addExtraBlockTags(tags);
    }
}
