package com.gaobieshi.security.network;

import com.gaobieshi.security.blockentity.ReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record ReceiverUpdatePacket(BlockPos pos, String action, String value) {
    public static final String SET_NAME = "set_name";
    public static final String TOGGLE_POLARITY = "toggle_polarity";
    public static final String GRANT_PERMISSION = "grant_permission";
    public static final String REVOKE_PERMISSION = "revoke_permission";

    public static void encode(ReceiverUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.action);
        buf.writeUtf(packet.value);
    }

    public static ReceiverUpdatePacket decode(FriendlyByteBuf buf) {
        return new ReceiverUpdatePacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(ReceiverUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !player.level().isLoaded(packet.pos)
                    || player.blockPosition().distSqr(packet.pos) > 64.0D) {
                return;
            }

            if (!(player.level().getBlockEntity(packet.pos) instanceof ReceiverBlockEntity receiver)) {
                player.sendSystemMessage(Component.literal("这个高别师的接收器不存在了。"));
                return;
            }

            if (!receiver.canConfigure(player)) {
                player.sendSystemMessage(Component.literal("你没有权限配置这个高别师的接收器。"));
                return;
            }

            switch (packet.action) {
                case SET_NAME -> {
                    receiver.setReceiverName(packet.value.trim());
                    player.sendSystemMessage(Component.literal("接收器名称已设置为：" + receiver.getReceiverName()));
                }
                case TOGGLE_POLARITY -> {
                    receiver.toggleInverted();
                    player.level().updateNeighborsAt(packet.pos, player.level().getBlockState(packet.pos).getBlock());
                    player.sendSystemMessage(Component.literal("接收器极性已设置为：").append(receiver.polarityText()));
                }
                case GRANT_PERMISSION -> receiver.grantPermission(player, packet.value);
                case REVOKE_PERMISSION -> receiver.revokePermission(player, packet.value);
                default -> player.sendSystemMessage(Component.literal("未知的接收器设置操作。"));
            }
        });
        context.get().setPacketHandled(true);
    }
}
