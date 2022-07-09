package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.OptionalDouble;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow public abstract Item getItem();

    @Redirect(method = "getTooltipLines",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/ai/attributes/Attributes;ATTACK_DAMAGE:Lnet/minecraft/world/entity/ai/attributes/Attribute;", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getAttributeBaseValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D", ordinal = 0))
    private double redirectGetAttackDamageBaseValue(Player player, Attribute attribute) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return 0;
        } else {
            return player.getAttributeBaseValue(attribute);
        }
    }

    @ModifyVariable(method = "getAttributeModifiers",
            ordinal = 0,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;getDefaultAttributeModifiers(Lnet/minecraft/world/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;")),
            at = @At(value = "STORE", ordinal = 0))
    private Multimap<Attribute, AttributeModifier> modifyAttributeModifiers(Multimap<Attribute, AttributeModifier> modifiers) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_8) {
            return modifiers;
        }
        if (modifiers.isEmpty()) {
            return modifiers;
        }
        modifiers = HashMultimap.create(modifiers);
        modifiers.removeAll(Attributes.ATTACK_DAMAGE);
        OptionalDouble defaultAttackDamage = Protocol_1_8.getDefaultAttackDamage(getItem());
        if (defaultAttackDamage.isPresent()) {
            modifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ItemAccessor.getBaseAttackDamageUuid(), "Weapon Modifier", defaultAttackDamage.getAsDouble(), AttributeModifier.Operation.ADDITION));
        }
        modifiers.removeAll(Attributes.ATTACK_SPEED);
        // TODO: display armor?
        modifiers.removeAll(Attributes.ARMOR);
        modifiers.removeAll(Attributes.ARMOR_TOUGHNESS);
        return modifiers;
    }
}
