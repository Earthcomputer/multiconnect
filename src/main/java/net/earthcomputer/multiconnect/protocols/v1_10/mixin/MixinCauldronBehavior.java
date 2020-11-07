package net.earthcomputer.multiconnect.protocols.v1_10.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.block.BlockState;
import net.minecraft.block.WaterCauldronBlock;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(CauldronBehavior.class)
public interface MixinCauldronBehavior {

    /**
     * TODO: replace with @Inject once this happens
     *
     * @reason Mixin doesn't support @Inject in interfaces yet.
     * @author Earthcomputer
     */
    @SuppressWarnings({"OverwriteTarget", "target"})
    @Overwrite(remap = false)
    static ActionResult method_32220(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_10) {
            return ActionResult.PASS;
        }

        if (!world.isClient) {
            player.setStackInHand(hand, ItemUsage.method_30012(stack, player, PotionUtil.setPotion(new ItemStack(Items.POTION), Potions.WATER)));
            player.incrementStat(Stats.USE_CAULDRON);
            WaterCauldronBlock.subtractWaterLevel(state, world, pos);
            world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        return ActionResult.success(world.isClient);

    }

}
