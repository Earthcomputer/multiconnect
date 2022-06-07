package net.earthcomputer.multiconnect.protocols.v1_12_2.mixin;

import net.earthcomputer.multiconnect.api.Protocols;
import net.earthcomputer.multiconnect.impl.ConnectionInfo;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.InjectionPoint;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.struct.InjectionPointData;

import java.util.Collection;

@Mixin(ClientPlayerInteractionManager.class)
public class MixinClientPlayerInteractionManager {
    @Shadow @Final private ClientPlayNetworkHandler networkHandler;

    // TODO: rewrite for 1.19

    // FIXME: change back to custom injection point once Mixin is updated to 0.8.3
//    @Redirect(method = "interactBlock",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 1),
//            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;shouldCancelInteraction()Z")))
//    private void cancelInteractBlockPacket(ClientPlayNetworkHandler networkHandler, Packet<?> packet) {
//        if (ConnectionInfo.protocolVersion > Protocols.V1_12_2) {
//            networkHandler.sendPacket(packet);
//        }
//    }
//
//    @Inject(method = "interactBlock",
//            at = @At(value = "RETURN", ordinal = 0),
//            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", ordinal = 1)))
//    private void onUsedOnBlock(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
//        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
//            if (ci.getReturnValue().isAccepted()) {
//                networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
//            }
//        }
//    }
//
//    @Inject(method = "interactBlock",
//            at = @At(value = "FIELD", target = "Lnet/minecraft/util/ActionResult;PASS:Lnet/minecraft/util/ActionResult;", ordinal = 0),
//            slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;getItemCooldownManager()Lnet/minecraft/entity/player/ItemCooldownManager;", ordinal = 0)))
//    private void onUsedOnBlockCoolingDown(ClientPlayerEntity player, ClientWorld world, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> ci) {
//        if (ConnectionInfo.protocolVersion <= Protocols.V1_12_2) {
//            networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand, hitResult));
//        }
//    }
}

class SendInteractBlockInjectionPoint extends InjectionPoint {
    public SendInteractBlockInjectionPoint(InjectionPointData injectionPointData) {
    }

    @Override
    public boolean find(String desc, InsnList insns, Collection<AbstractInsnNode> nodes) {
        AbstractInsnNode insn;
        for (insn = insns.getFirst(); insn != null; insn = insn.getNext()) {
            if (isGetItemCooldownManager(insn)) {
                break;
            }
        }
        if (insn == null) {
            return false;
        }

        for (insn = insn.getPrevious(); insn != null; insn = insn.getPrevious()) {
            if (isSendPacket(insn)) {
                nodes.add(insn);
                return true;
            }
        }

        return false;
    }

    private static final String CLIENT_PLAYER_ENTITY;
    private static final String GET_ITEM_COOLDOWN_MANAGER;
    private static final String GET_ITEM_COOLDOWN_MANAGER_DESC;
    private static final String CLIENT_PLAY_NETWORK_HANDLER;
    private static final String SEND_PACKET;
    private static final String SEND_PACKET_DESC;

    static {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        CLIENT_PLAYER_ENTITY = mappingResolver.mapClassName("intermediary", "net.minecraft.class_746").replace('.', '/');
        GET_ITEM_COOLDOWN_MANAGER = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1657", "method_7357", "()Lnet/minecraft/class_1796;");
        GET_ITEM_COOLDOWN_MANAGER_DESC = String.format("()L%s;", mappingResolver.mapClassName("intermediary", "net.minecraft.class_1796").replace('.', '/'));
        CLIENT_PLAY_NETWORK_HANDLER = mappingResolver.mapClassName("intermediary", "net.minecraft.class_634").replace('.', '/');
        SEND_PACKET = mappingResolver.mapMethodName("intermediary", "net.minecraft.class_634", "method_2883", "(Lnet/minecraft/class_2596;)V");
        SEND_PACKET_DESC = String.format("(L%s;)V", mappingResolver.mapClassName("intermediary", "net.minecraft.class_2596").replace('.', '/'));
    }

    private static boolean isGetItemCooldownManager(AbstractInsnNode insn) {
        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
            return false;
        }
        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        return CLIENT_PLAYER_ENTITY.equals(methodInsn.owner) && GET_ITEM_COOLDOWN_MANAGER.equals(methodInsn.name) && GET_ITEM_COOLDOWN_MANAGER_DESC.equals(methodInsn.desc);
    }

    private static boolean isSendPacket(AbstractInsnNode insn) {
        if (insn.getOpcode() != Opcodes.INVOKEVIRTUAL) {
            return false;
        }
        MethodInsnNode methodInsn = (MethodInsnNode) insn;
        return CLIENT_PLAY_NETWORK_HANDLER.equals(methodInsn.owner) && SEND_PACKET.equals(methodInsn.name) && SEND_PACKET_DESC.equals(methodInsn.desc);
    }
}
