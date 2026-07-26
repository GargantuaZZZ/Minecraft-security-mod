package com.gaobieshi.security.network;

import com.gaobieshi.security.blockentity.CameraBlockEntity;
import com.gaobieshi.security.edit.GbsAreaEditHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record CameraUpdatePacket(BlockPos pos, String action, String value) {
    public static final String SET_NAME = "set_name";
    public static final String SET_VOLUME = "set_volume";
    public static final String TOGGLE = "toggle";
    public static final String TRUST = "trust";
    public static final String UNTRUST = "untrust";
    public static final String ADD_AREA = "add_area";
    public static final String CLEAR_AREA = "clear_area";
    public static final String BIND = "bind";
    public static final String UNBIND = "unbind";
    public static final String GRANT_PERMISSION = "grant_permission";
    public static final String REVOKE_PERMISSION = "revoke_permission";
    public static final String BEGIN_AREA_EDIT = "begin_area_edit";
    public static final String END_AREA_EDIT = "end_area_edit";

    public static void encode(CameraUpdatePacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.action);
        buf.writeUtf(packet.value);
    }

    public static CameraUpdatePacket decode(FriendlyByteBuf buf) {
        return new CameraUpdatePacket(buf.readBlockPos(), buf.readUtf(), buf.readUtf());
    }

    public static void handle(CameraUpdatePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !player.level().isLoaded(packet.pos)
                    || player.blockPosition().distSqr(packet.pos) > 64.0D) {
                return;
            }

            if (!(player.level().getBlockEntity(packet.pos) instanceof CameraBlockEntity camera)) {
                player.sendSystemMessage(Component.literal("这个高别师的摄像头不存在了。"));
                return;
            }

            if (!camera.canConfigure(player)) {
                player.sendSystemMessage(Component.literal("你没有权限配置这个高别师的摄像头。"));
                return;
            }

            switch (packet.action) {
                case SET_NAME -> camera.setCameraName(player, packet.value.trim());
                case SET_VOLUME -> camera.setAlertVolume(player, parseVolume(packet.value));
                case TOGGLE -> camera.toggle(player);
                case TRUST -> camera.trust(player, packet.value.trim());
                case UNTRUST -> camera.untrust(player, packet.value.trim());
                case ADD_AREA -> camera.addAllAir(player);
                case CLEAR_AREA -> camera.clearArea(player);
                case BIND -> camera.bindReceiver(player);
                case UNBIND -> camera.unbindReceiver(player);
                case GRANT_PERMISSION -> camera.grantPermission(player, packet.value);
                case REVOKE_PERMISSION -> camera.revokePermission(player, packet.value);
                case BEGIN_AREA_EDIT -> GbsAreaEditHandler.begin(player, packet.pos, camera);
                case END_AREA_EDIT -> GbsAreaEditHandler.end(player);
                default -> player.sendSystemMessage(Component.literal("未知的摄像头设置操作。"));
            }
        });
        context.get().setPacketHandled(true);
    }

    private static int parseVolume(String value) {
        try {
            return Math.max(0, Math.min(200, Integer.parseInt(value.trim())));
        } catch (NumberFormatException ignored) {
            return 100;
        }
    }
}
