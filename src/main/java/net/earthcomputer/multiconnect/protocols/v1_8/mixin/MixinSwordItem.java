package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class MixinSwordItem extends ToolItem {
    public MixinSwordItem(ToolMaterial material, Settings settings) {
        super(material, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            ItemStack stack = user.getStackInHand(hand);
            user.setCurrentHand(hand);
            return TypedActionResult.consume(stack);
        }
        return super.use(world, user, hand);
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return UseAction.BLOCK;
        }
        return super.getUseAction(stack);
    }

    @Override
    public int getMaxUseTime(ItemStack stack) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return 72000;
        }
        return super.getMaxUseTime(stack);
    }
}
