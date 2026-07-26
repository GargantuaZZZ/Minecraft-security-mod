package com.gaobieshi.security.network;

import com.gaobieshi.security.GaobieshiSecurity;
import com.gaobieshi.security.blockentity.CameraBlockEntity;
import com.gaobieshi.security.blockentity.ReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

public final class GbsNetwork {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(GaobieshiSecurity.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        CHANNEL.registerMessage(id++, OpenCameraScreenPacket.class, OpenCameraScreenPacket::encode,
                OpenCameraScreenPacket::decode, OpenCameraScreenPacket::handle);
        CHANNEL.registerMessage(id++, CameraUpdatePacket.class, CameraUpdatePacket::encode,
                CameraUpdatePacket::decode, CameraUpdatePacket::handle);
        CHANNEL.registerMessage(id++, OpenReceiverScreenPacket.class, OpenReceiverScreenPacket::encode,
                OpenReceiverScreenPacket::decode, OpenReceiverScreenPacket::handle);
        CHANNEL.registerMessage(id, ReceiverUpdatePacket.class, ReceiverUpdatePacket::encode,
                ReceiverUpdatePacket::decode, ReceiverUpdatePacket::handle);
    }

    public static void openCamera(ServerPlayer player, BlockPos pos, CameraBlockEntity camera) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                OpenCameraScreenPacket.from(pos, camera).withOnlinePlayers(onlinePlayers(player)));
    }

    public static void openReceiver(ServerPlayer player, BlockPos pos, ReceiverBlockEntity receiver) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                OpenReceiverScreenPacket.from(pos, receiver).withOnlinePlayers(onlinePlayers(player)));
    }

    private static List<String> onlinePlayers(ServerPlayer player) {
        return player.server.getPlayerList().getPlayers().stream()
                .map(serverPlayer -> serverPlayer.getGameProfile().getName())
                .sorted()
                .toList();
    }

    private GbsNetwork() {
    }
}
