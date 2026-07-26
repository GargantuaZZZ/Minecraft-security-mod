package com.gaobieshi.security.client;

import com.gaobieshi.security.network.OpenCameraScreenPacket;
import com.gaobieshi.security.network.OpenReceiverScreenPacket;
import net.minecraft.client.Minecraft;

public final class GbsClientScreens {
    public static void openCamera(OpenCameraScreenPacket packet) {
        Minecraft.getInstance().setScreen(new GbsCameraScreen(packet));
    }

    public static void openReceiver(OpenReceiverScreenPacket packet) {
        Minecraft.getInstance().setScreen(new GbsReceiverScreen(packet));
    }

    private GbsClientScreens() {
    }
}
