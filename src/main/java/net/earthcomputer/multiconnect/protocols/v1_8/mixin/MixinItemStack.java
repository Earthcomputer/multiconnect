package net.earthcomputer.multiconnect.protocols.v1_8.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.earthcomputer.multiconnect.protocols.v1_8.Protocol_1_8;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.OptionalDouble;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    @Shadow public abstract Item getItem();

    @Redirect(method = "getTooltip",
            slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/entity/attribute/EntityAttributes;GENERIC_ATTACK_DAMAGE:Lnet/minecraft/entity/attribute/EntityAttribute;", ordinal = 0)),
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/PlayerEntity;getAttributeBaseValue(Lnet/minecraft/entity/attribute/EntityAttribute;)D", ordinal = 0))
    private double redirectGetAttackDamageBaseValue(PlayerEntity player, EntityAttribute attribute) {
        if (ConnectionInfo.protocolVersion <= Protocols.V1_8) {
            return 0;
        } else {
            return player.getAttributeBaseValue(attribute);
        }
    }

    @ModifyVariable(method = "getAttributeModifiers",
            ordinal = 0,
            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;getAttributeModifiers(Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;")),
            at = @At(value = "STORE", ordinal = 1))
    private Multimap<EntityAttribute, EntityAttributeModifier> modifyAttributeModifiers(Multimap<EntityAttribute, EntityAttributeModifier> modifiers) {
        if (ConnectionInfo.protocolVersion > Protocols.V1_8) {
            return modifiers;
        }
        if (modifiers.isEmpty()) {
            return modifiers;
        }
        modifiers = HashMultimap.create(modifiers);
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        OptionalDouble defaultAttackDamage = Protocol_1_8.getDefaultAttackDamage(getItem());
        if (defaultAttackDamage.isPresent()) {
            modifiers.put(EntityAttributes.GENERIC_ATTACK_DAMAGE, new EntityAttributeModifier(ItemAccessor.getAttackDamageModifierId(), "Weapon Modifier", defaultAttackDamage.getAsDouble(), EntityAttributeModifier.Operation.ADDITION));
        }
        modifiers.removeAll(EntityAttributes.GENERIC_ATTACK_SPEED);
        // TODO: display armor?
        modifiers.removeAll(EntityAttributes.GENERIC_ARMOR);
        modifiers.removeAll(EntityAttributes.GENERIC_ARMOR_TOUGHNESS);
        return modifiers;
    }
}
