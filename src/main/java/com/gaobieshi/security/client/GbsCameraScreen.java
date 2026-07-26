package com.gaobieshi.security.client;

import com.gaobieshi.security.network.CameraUpdatePacket;
import com.gaobieshi.security.network.GbsNetwork;
import com.gaobieshi.security.network.OpenCameraScreenPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GbsCameraScreen extends Screen {
    private final OpenCameraScreenPacket packet;
    private EditBox nameInput;
    private EditBox volumeInput;
    private boolean enabled;

    public GbsCameraScreen(OpenCameraScreenPacket packet) {
        super(Component.literal("高别师摄像头"));
        this.packet = packet;
        this.enabled = packet.enabled();
    }

    @Override
    protected void init() {
        int panelX = width / 2 - 170;
        int y = 52;

        nameInput = new EditBox(font, panelX, y, 220, 20, Component.literal("摄像头名称"));
        nameInput.setMaxLength(64);
        nameInput.setValue(packet.name());
        addRenderableWidget(nameInput);
        addRenderableWidget(Button.builder(Component.literal("保存名称"), button ->
                        send(CameraUpdatePacket.SET_NAME, nameInput.getValue()))
                .bounds(panelX + 230, y, 110, 20).build());

        y += 32;
        volumeInput = new EditBox(font, panelX, y, 80, 20, Component.literal("提示音量"));
        volumeInput.setMaxLength(3);
        volumeInput.setValue(Integer.toString(packet.volumePercent()));
        addRenderableWidget(volumeInput);
        addRenderableWidget(Button.builder(Component.literal("保存音量"), button ->
                        send(CameraUpdatePacket.SET_VOLUME, volumeInput.getValue()))
                .bounds(panelX + 90, y, 110, 20).build());
        addRenderableWidget(Button.builder(toggleText(), button -> {
                    send(CameraUpdatePacket.TOGGLE, "");
                    enabled = !enabled;
                    button.setMessage(toggleText());
                })
                .bounds(panelX + 210, y, 130, 20).build());

        y += 32;
        addRenderableWidget(Button.builder(Component.literal("清空监控区域"), button ->
                        send(CameraUpdatePacket.CLEAR_AREA, ""))
                .bounds(panelX, y, 164, 20).build());

        y += 28;
        addRenderableWidget(Button.builder(Component.literal("进入区域编辑"), button ->
                        send(CameraUpdatePacket.BEGIN_AREA_EDIT, ""))
                .bounds(panelX, y, 164, 20).build());
        addRenderableWidget(Button.builder(Component.literal("退出区域编辑"), button ->
                        send(CameraUpdatePacket.END_AREA_EDIT, ""))
                .bounds(panelX + 176, y, 164, 20).build());

        y += 28;
        addRenderableWidget(Button.builder(Component.literal("绑定下方接收器"), button ->
                        send(CameraUpdatePacket.BIND, ""))
                .bounds(panelX, y, 164, 20).build());
        addRenderableWidget(Button.builder(Component.literal("解绑接收器"), button ->
                        send(CameraUpdatePacket.UNBIND, ""))
                .bounds(panelX + 176, y, 164, 20).build());

        y += 32;
        addPlayerPermissionButtons(panelX, y);

        y = height - 30;
        addRenderableWidget(Button.builder(Component.literal("完成"), button -> onClose())
                .bounds(width / 2 - 50, y, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 18, 0xFFFFFF);
        int panelX = width / 2 - 170;
        int infoY = Math.max(height / 2 + 28, 270);
        graphics.drawString(font, "监控：" + (enabled ? "开启" : "关闭")
                + "    监控格：" + packet.watchCount()
                + "    拥有者：" + packet.ownerName(), panelX, infoY, 0xD7E8FF, false);
        graphics.drawString(font, "绑定接收器：" + packet.receiverName(), panelX, infoY + 16, 0xD7E8FF, false);
        graphics.drawString(font, "授权触发玩家：" + packet.trustedPlayers(), panelX, infoY + 32, 0xD7E8FF, false);
        graphics.drawString(font, "可配置玩家：" + String.join(", ", packet.permittedPlayers()), panelX, infoY + 48, 0xD7E8FF, false);
        graphics.drawString(font, "区域编辑：放置临时绿色玻璃加入监控，破坏它移除，退出编辑后玻璃消失。", panelX, infoY + 68, 0xAABBDD, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component toggleText() {
        return Component.literal(enabled ? "关闭监控" : "开启监控");
    }

    private void send(String action, String value) {
        GbsNetwork.CHANNEL.sendToServer(new CameraUpdatePacket(packet.pos(), action, value));
    }

    private void addPlayerPermissionButtons(int panelX, int y) {
        int row = 0;
        for (String playerName : packet.onlinePlayers()) {
            int buttonY = y + row * 30;
            if (buttonY > height / 2 + 8) {
                break;
            }
            boolean trusted = packet.trustedPlayerList().contains(playerName);
            boolean permitted = packet.permittedPlayers().contains(playerName);
            String trustLabel = (trusted ? "触发移除 " : "触发允许 ") + playerName;
            String configLabel = (permitted ? "配置移除 " : "配置允许 ") + playerName;
            addRenderableWidget(Button.builder(Component.literal(configLabel), button ->
                            send(permitted ? CameraUpdatePacket.REVOKE_PERMISSION : CameraUpdatePacket.GRANT_PERMISSION, playerName))
                    .bounds(panelX + 174, buttonY, 166, 20).build());
            addRenderableWidget(Button.builder(Component.literal(trustLabel), button ->
                            send(trusted ? CameraUpdatePacket.UNTRUST : CameraUpdatePacket.TRUST, playerName))
                    .bounds(panelX, buttonY, 166, 20).build());
            row++;
        }
    }
}
