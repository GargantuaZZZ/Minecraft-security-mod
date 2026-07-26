package com.gaobieshi.security.network;

import com.gaobieshi.security.blockentity.ReceiverBlockEntity;
import com.gaobieshi.security.client.GbsClientScreens;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

public record OpenReceiverScreenPacket(BlockPos pos, String name, boolean inverted, String ownerName,
                                       List<String> permittedPlayers, List<String> onlinePlayers) {
    public static OpenReceiverScreenPacket from(BlockPos pos, ReceiverBlockEntity receiver) {
        return new OpenReceiverScreenPacket(pos, receiver.getReceiverName(), receiver.isInverted(),
                receiver.getOwnerName(), receiver.getPermittedPlayers(), List.of());
    }

    public OpenReceiverScreenPacket withOnlinePlayers(List<String> onlinePlayers) {
        return new OpenReceiverScreenPacket(pos, name, inverted, ownerName, permittedPlayers, onlinePlayers);
    }

    public static void encode(OpenReceiverScreenPacket packet, FriendlyByteBuf buf) {
        buf.writeBlockPos(packet.pos);
        buf.writeUtf(packet.name);
        buf.writeBoolean(packet.inverted);
        buf.writeUtf(packet.ownerName);
        buf.writeCollection(packet.permittedPlayers, FriendlyByteBuf::writeUtf);
        buf.writeCollection(packet.onlinePlayers, FriendlyByteBuf::writeUtf);
    }

    public static OpenReceiverScreenPacket decode(FriendlyByteBuf buf) {
        return new OpenReceiverScreenPacket(buf.readBlockPos(), buf.readUtf(), buf.readBoolean(), buf.readUtf(),
                buf.readList(FriendlyByteBuf::readUtf), buf.readList(FriendlyByteBuf::readUtf));
    }

    public static void handle(OpenReceiverScreenPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> GbsClientScreens.openReceiver(packet)));
        context.get().setPacketHandled(true);
    }
}
