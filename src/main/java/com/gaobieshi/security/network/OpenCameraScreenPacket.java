package com.gaobieshi.security.network;

import com.gaobieshi.security.blockentity.CameraBlockEntity;
import com.gaobieshi.security.client.GbsClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record OpenCameraScreenPacket(BlockPos pos, String name, boolean enabled, int volumePercent,
                                     String trustedPlayers, List<String> trustedPlayerList, int watchCount, String receiverName,
                                     String ownerName, List<String> permittedPlayers, List<String> onlinePlayers) {
    public static OpenCameraScreenPacket from(BlockPos pos, CameraBlockEntity camera) {
        return new OpenCameraScreenPacket(pos, camera.getCameraName(), camera.isEnabled(),
                camera.getAlertVolumePercent(), camera.getTrustedPlayersText(), camera.getTrustedPlayers(), camera.getWatchCount(),
                camera.getBoundReceiverName(), camera.getOwnerName(), camera.getPermittedPlayers(), List.of());
    }

    public OpenCameraScreenPacket withOnlinePlayers(List<String> onlinePlayers) {
        return new OpenCameraScreenPacket(pos, name, enabled, volumePercent, trustedPlayers, trustedPlayerList, watchCount,
                receiverName, ownerName, permittedPlayers, onlinePlayers);
    }

    public static void encode(OpenCameraScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.name);
        buf.writeBoolean(packet.enabled);
        buf.writeInt(packet.volumePercent);
        buf.writeUtf(packet.trustedPlayers);
        buf.writeCollection(packet.trustedPlayerList, FriendlyByteBuf::writeUtf);
        buf.writeInt(packet.watchCount);
        buf.writeUtf(packet.receiverName);
        buf.writeUtf(packet.ownerName);
        buf.writeCollection(packet.permittedPlayers, FriendlyByteBuf::writeUtf);
        buf.writeCollection(packet.onlinePlayers, FriendlyByteBuf::writeUtf);
    }

    public static OpenCameraScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenCameraScreenPacket(buf.readBlockPos(), buf.readUtf(), buf.readBoolean(), buf.readInt(),
                buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf), buf.readInt(), buf.readUtf(), buf.readUtf(),
                buf.readList(FriendlyByteBuf::readUtf), buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(OpenCameraScreenPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> GbsClientScreens.openCamera(packet)));
        context.get().setPacketHandled(true);
    }
}
