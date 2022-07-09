package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class SwordItemMixin extends TieredItem {
    public SwordItemMixin(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ItemStack stack = user.getItemInHand(hand);
            user.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, user, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return UseAnim.BLOCK;
        }
        return super.getUseAnimation(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return 72000;
        }
        return super.getUseDuration(stack);
    }
}
