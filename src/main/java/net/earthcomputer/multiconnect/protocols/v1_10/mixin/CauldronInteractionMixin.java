package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CauldronInteraction.class)
public interface CauldronInteractionMixin {

    /**
     * TODO: replace with @Inject once this happens
     *
     * @reason Mixin doesn't support @Inject in interfaces yet.
     * @author Earthcomputer
     */
    @SuppressWarnings("target")
    @Overwrite(remap = false, aliases = "lambda$bootStrap$5")
    static InteractionResult method_32220(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(stack, player, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
            player.awardStat(Stats.USE_CAULDRON);
            LayeredCauldronBlock.lowerFillLevel(state, world, pos);
            world.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        }

        return InteractionResult.sidedSuccess(world.isClientSide);

    }

}
