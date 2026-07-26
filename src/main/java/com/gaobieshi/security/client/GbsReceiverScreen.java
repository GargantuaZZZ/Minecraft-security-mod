package com.gaobieshi.security.client;

import com.gaobieshi.security.network.GbsNetwork;
import com.gaobieshi.security.network.OpenReceiverScreenPacket;
import com.gaobieshi.security.network.ReceiverUpdatePacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GbsReceiverScreen extends Screen {
    private final OpenReceiverScreenPacket packet;
    private EditBox nameInput;
    private boolean inverted;

    public GbsReceiverScreen(OpenReceiverScreenPacket packet) {
        super(Component.literal("高别师接收器"));
        this.packet = packet;
        this.inverted = packet.inverted();
    }

    @Override
    protected void init() {
        int panelX = width / 2 - 120;
        int y = 62;

        nameInput = new EditBox(font, panelX, y, 150, 20, Component.literal("接收器名称"));
        nameInput.setMaxLength(64);
        nameInput.setValue(packet.name());
        addRenderableWidget(nameInput);
        addRenderableWidget(Button.builder(Component.literal("保存名称"), button ->
                        GbsNetwork.CHANNEL.sendToServer(new ReceiverUpdatePacket(packet.pos(),
                                ReceiverUpdatePacket.SET_NAME, nameInput.getValue())))
                .bounds(panelX + 160, y, 80, 20).build());

        y += 36;
        addRenderableWidget(Button.builder(polarityText(), button -> {
                    GbsNetwork.CHANNEL.sendToServer(new ReceiverUpdatePacket(packet.pos(),
                            ReceiverUpdatePacket.TOGGLE_POLARITY, ""));
                    inverted = !inverted;
                    button.setMessage(polarityText());
                })
                .bounds(panelX, y, 116, 20).build());
        addRenderableWidget(Button.builder(Component.literal("完成"), button -> onClose())
                .bounds(panelX + 124, y, 116, 20).build());

        y += 34;
        addPlayerPermissionButtons(panelX, y);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.drawCenteredString(font, title, width / 2, 24, 0xFFFFFF);
        int panelX = width / 2 - 120;
        graphics.drawString(font, "极性：" + polarityLabel(), panelX, 46, 0xD7E8FF, false);
        graphics.drawString(font, "拥有者：" + packet.ownerName(), panelX, 128, 0xD7E8FF, false);
        graphics.drawString(font, "可配置玩家：" + String.join(", ", packet.permittedPlayers()), panelX, 144, 0xD7E8FF, false);
        graphics.drawString(font, "在线玩家权限列表", panelX, 162, 0xAABBDD, false);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component polarityText() {
        return Component.literal(inverted ? "改为触发时充能" : "改为未触发时充能");
    }

    private String polarityLabel() {
        return inverted ? "未触发时充能" : "触发时充能";
    }

    private void addPlayerPermissionButtons(int panelX, int y) {
        int row = 0;
        for (String playerName : packet.onlinePlayers()) {
            int buttonY = y + row * 24;
            if (buttonY > height - 28) {
                break;
            }
            boolean permitted = packet.permittedPlayers().contains(playerName);
            String label = (permitted ? "移除 " : "允许 ") + playerName;
            addRenderableWidget(Button.builder(Component.literal(label), button ->
                            GbsNetwork.CHANNEL.sendToServer(new ReceiverUpdatePacket(packet.pos(),
                                    permitted ? ReceiverUpdatePacket.REVOKE_PERMISSION : ReceiverUpdatePacket.GRANT_PERMISSION,
                                    playerName)))
                    .bounds(panelX, buttonY, 240, 20).build());
            row++;
        }
    }
}
