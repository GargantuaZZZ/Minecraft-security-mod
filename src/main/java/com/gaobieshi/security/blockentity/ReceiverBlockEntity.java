package com.gaobieshi.security.blockentity;

import com.gaobieshi.security.registry.GbsBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ReceiverBlockEntity extends BlockEntity {
    private String receiverName;
    private String ownerName = "";
    private final Set<String> permittedPlayers = new HashSet<>();
    private boolean inverted;

    public ReceiverBlockEntity(BlockPos pos, BlockState state) {
        super(GbsBlockEntities.RECEIVER.get(), pos, state);
        receiverName = "接收器(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
    }

    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
        setChanged();
    }

    public boolean isInverted() {
        return inverted;
    }

    public boolean canConfigure(ServerPlayer player) {
        String playerName = player.getGameProfile().getName();
        return ownerName.isEmpty() || playerName.equals(ownerName) || permittedPlayers.contains(playerName);
    }

    public void setOwnerIfAbsent(ServerPlayer player) {
        if (ownerName == null || ownerName.isEmpty()) {
            ownerName = player.getGameProfile().getName();
            permittedPlayers.add(ownerName);
            setChanged();
        }
    }

    public String getOwnerName() {
        return ownerName == null || ownerName.isEmpty() ? "无" : ownerName;
    }

    public List<String> getPermittedPlayers() {
        List<String> players = new ArrayList<>(permittedPlayers);
        players.sort(Comparator.naturalOrder());
        return players;
    }

    public void grantPermission(ServerPlayer player, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        permittedPlayers.add(name.trim());
        setChanged();
        player.sendSystemMessage(Component.literal("已允许配置接收器：" + name.trim()));
    }

    public void revokePermission(ServerPlayer player, String name) {
        if (name == null || name.isBlank()) {
            return;
        }
        String cleanName = name.trim();
        if (cleanName.equals(ownerName)) {
            player.sendSystemMessage(Component.literal("不能移除拥有者的配置权限。"));
            return;
        }
        permittedPlayers.remove(cleanName);
        setChanged();
        player.sendSystemMessage(Component.literal("已移除接收器配置权限：" + cleanName));
    }

    public void toggleInverted() {
        inverted = !inverted;
        setChanged();
    }

    public Component polarityText() {
        return Component.literal(inverted ? "未触发时充能" : "触发时充能");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("ReceiverName", receiverName);
        tag.putString("OwnerName", getOwnerName());
        tag.putBoolean("Inverted", inverted);

        ListTag permittedTag = new ListTag();
        for (String name : permittedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", name);
            permittedTag.add(entry);
        }
        tag.put("PermittedPlayers", permittedTag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        receiverName = tag.contains("ReceiverName") ? tag.getString("ReceiverName") : receiverName;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : ownerName;
        inverted = tag.getBoolean("Inverted");

        permittedPlayers.clear();
        ListTag permittedTag = tag.getList("PermittedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < permittedTag.size(); i++) {
            permittedPlayers.add(permittedTag.getCompound(i).getString("Name"));
        }
        if (!ownerName.isEmpty()) {
            permittedPlayers.add(ownerName);
        }
    }
}
