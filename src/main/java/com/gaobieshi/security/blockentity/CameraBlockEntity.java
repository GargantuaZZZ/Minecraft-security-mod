package com.gaobieshi.security.blockentity;

import com.gaobieshi.security.block.ReceiverBlock;
import com.gaobieshi.security.GaobieshiSecurity;
import com.gaobieshi.security.registry.GbsBlockEntities;
import com.gaobieshi.security.registry.GbsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CameraBlockEntity extends BlockEntity {
    private static final String SELECTED_DIM = "GbsSelectedCameraDimension";
    private static final String SELECTED_X = "GbsSelectedCameraX";
    private static final String SELECTED_Y = "GbsSelectedCameraY";
    private static final String SELECTED_Z = "GbsSelectedCameraZ";

    private boolean enabled = false;
    private String cameraName = "高别师的摄像头";
    private String ownerName = "";
    private final Set<String> permittedPlayers = new HashSet<>();
    private final Set<String> trustedPlayers = new HashSet<>();
    private final Set<BlockPos> watchOffsets = new HashSet<>();
    private BlockPos receiverOffset;
    private int cooldownTicks = 0;
    private float alertVolume = 1.0F;

    public CameraBlockEntity(BlockPos pos, BlockState state) {
        super(GbsBlockEntities.CAMERA.get(), pos, state);
        cameraName = "摄像头(" + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")";
    }

    public static void selectFor(ServerPlayer player, ResourceKey<Level> dimension, BlockPos pos) {
        CompoundTag tag = player.getPersistentData();
        tag.putString(SELECTED_DIM, dimension.location().toString());
        tag.putInt(SELECTED_X, pos.getX());
        tag.putInt(SELECTED_Y, pos.getY());
        tag.putInt(SELECTED_Z, pos.getZ());
    }

    public static CameraBlockEntity getSelected(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(SELECTED_DIM)) {
            player.sendSystemMessage(Component.literal("请先右键一个高别师的摄像头。"));
            return null;
        }

        ResourceLocation dimId = ResourceLocation.tryParse(tag.getString(SELECTED_DIM));
        if (dimId == null) {
            player.sendSystemMessage(Component.literal("选中的摄像头维度无效，请重新右键摄像头。"));
            return null;
        }

        ResourceKey<Level> dimKey = ResourceKey.create(Registries.DIMENSION, dimId);
        ServerLevel selectedLevel = player.server.getLevel(dimKey);
        if (selectedLevel == null) {
            player.sendSystemMessage(Component.literal("找不到摄像头所在维度，请重新右键摄像头。"));
            return null;
        }

        BlockPos pos = new BlockPos(tag.getInt(SELECTED_X), tag.getInt(SELECTED_Y), tag.getInt(SELECTED_Z));
        if (!(selectedLevel.getBlockEntity(pos) instanceof CameraBlockEntity camera)) {
            player.sendSystemMessage(Component.literal("选中的摄像头不存在了，请重新右键摄像头。"));
            return null;
        }
        return camera;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, CameraBlockEntity camera) {
        if (!(level instanceof ServerLevel serverLevel) || !camera.enabled || camera.watchOffsets.isEmpty()) {
            return;
        }

        if (camera.cooldownTicks > 0) {
            camera.cooldownTicks--;
            return;
        }

        AABB area = new AABB(
                pos.getX() - 2.0D, pos.getY() - 5.0D, pos.getZ() - 2.0D,
                pos.getX() + 3.0D, pos.getY(), pos.getZ() + 3.0D
        );

        for (LivingEntity entity : serverLevel.getEntitiesOfClass(LivingEntity.class, area)) {
            BlockPos entityPos = entity.blockPosition();
            BlockPos offset = entityPos.subtract(pos);
            if (!camera.watchOffsets.contains(offset)) {
                continue;
            }

            if (entity instanceof Player player) {
                String playerName = player.getGameProfile().getName();
                if (camera.trustedPlayers.contains(playerName)) {
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal(playerName + "触发了" + camera.cameraName), false);
                    camera.playAlertSound(serverLevel);
                    camera.triggerReceiver(serverLevel);
                } else {
                    serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                            Component.literal("警告：无权限人员靠近" + camera.cameraName), false);
                }
            } else {
                serverLevel.getServer().getPlayerList().broadcastSystemMessage(
                        Component.literal("警告：无权限人员靠近" + camera.cameraName), false);
            }

            camera.cooldownTicks = 40;
            camera.setChanged();
            return;
        }
    }

    public void sendHelp(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("已选择：" + cameraName));
        player.sendSystemMessage(Component.literal("状态：" + (enabled ? "监控开启" : "监控关闭")
                + "，监控格：" + watchOffsets.size()
                + "，授权玩家：" + (trustedPlayers.isEmpty() ? "无" : String.join(", ", trustedPlayers))
                + "，提示音量：" + formatVolume()));
        player.sendSystemMessage(Component.literal("/gbs toggle - 打开/关闭监控"));
        player.sendSystemMessage(Component.literal("/gbs name 名称 - 设置摄像头名称"));
        player.sendSystemMessage(Component.literal("/gbs volume 0-200 - 设置授权人员触发提示音音量"));
        player.sendSystemMessage(Component.literal("/gbs trust 玩家名 - 添加授权玩家"));
        player.sendSystemMessage(Component.literal("/gbs untrust 玩家名 - 移除授权玩家"));
        player.sendSystemMessage(Component.literal("GUI 区域编辑 - 用虚拟方块选择监控区域"));
        player.sendSystemMessage(Component.literal("/gbs area clear - 清空监控区域"));
        player.sendSystemMessage(Component.literal("/gbs bind - 绑定下方5x5x5内的一个接收器"));
        player.sendSystemMessage(Component.literal("/gbs info - 查看当前配置"));
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
        player.sendSystemMessage(Component.literal("已允许配置摄像头：" + name.trim()));
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
        player.sendSystemMessage(Component.literal("已移除摄像头配置权限：" + cleanName));
    }

    public String getCameraName() {
        return cameraName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getAlertVolumePercent() {
        return Math.round(alertVolume * 100.0F);
    }

    public int getWatchCount() {
        return watchOffsets.size();
    }

    public String getTrustedPlayersText() {
        return trustedPlayers.isEmpty() ? "无" : String.join(", ", trustedPlayers);
    }

    public List<String> getTrustedPlayers() {
        List<String> players = new ArrayList<>(trustedPlayers);
        players.sort(Comparator.naturalOrder());
        return players;
    }

    public String getBoundReceiverName() {
        if (receiverOffset == null) {
            return "未绑定";
        }

        return getReceiverName(worldPosition.offset(receiverOffset));
    }

    public void unbindReceiver(ServerPlayer player) {
        receiverOffset = null;
        setChanged();
        player.sendSystemMessage(Component.literal("已解绑接收器。"));
    }

    public boolean isWatchOffset(BlockPos offset) {
        return watchOffsets.contains(offset);
    }

    public void addWatchOffset(BlockPos offset) {
        watchOffsets.add(offset);
        setChanged();
    }

    public void removeWatchOffset(BlockPos offset) {
        watchOffsets.remove(offset);
        setChanged();
    }

    public boolean isValidWatchOffset(BlockPos offset) {
        return offset.getX() >= -2 && offset.getX() <= 2
                && offset.getY() <= -1 && offset.getY() >= -5
                && offset.getZ() >= -2 && offset.getZ() <= 2;
    }

    public void toggle(ServerPlayer player) {
        enabled = !enabled;
        setChanged();
        player.sendSystemMessage(Component.literal(cameraName + "：" + (enabled ? "监控已开启" : "监控已关闭")));
    }

    public void setCameraName(ServerPlayer player, String name) {
        cameraName = name;
        setChanged();
        player.sendSystemMessage(Component.literal("摄像头名称已设置为：" + cameraName));
    }

    public void setAlertVolume(ServerPlayer player, int percent) {
        alertVolume = percent / 100.0F;
        setChanged();
        player.sendSystemMessage(Component.literal("授权人员触发提示音音量已设置为：" + formatVolume()));
    }

    public void trust(ServerPlayer player, String name) {
        trustedPlayers.add(name);
        setChanged();
        player.sendSystemMessage(Component.literal("已授权玩家：" + name));
    }

    public void untrust(ServerPlayer player, String name) {
        trustedPlayers.remove(name);
        setChanged();
        player.sendSystemMessage(Component.literal("已移除授权玩家：" + name));
    }

    public void addAllAir(ServerPlayer player) {
        if (level == null) {
            return;
        }

        watchOffsets.clear();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy >= -5; dy--) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos target = worldPosition.offset(dx, dy, dz);
                    if (level.isEmptyBlock(target)) {
                        watchOffsets.add(new BlockPos(dx, dy, dz));
                    }
                }
            }
        }

        setChanged();
        player.sendSystemMessage(Component.literal("已添加 " + watchOffsets.size() + " 个空气格作为监控区域。"));
    }

    public void clearArea(ServerPlayer player) {
        watchOffsets.clear();
        setChanged();
        player.sendSystemMessage(Component.literal("已清空监控区域。"));
    }

    public void bindReceiver(ServerPlayer player) {
        if (level == null) {
            return;
        }

        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -1; dy >= -5; dy--) {
                for (int dz = -2; dz <= 2; dz++) {
                    BlockPos target = worldPosition.offset(dx, dy, dz);
                    if (level.getBlockState(target).is(GbsBlocks.RECEIVER.get())) {
                        receiverOffset = new BlockPos(dx, dy, dz);
                        setChanged();
                        String receiverName = getReceiverName(target);
                        player.sendSystemMessage(Component.literal("已绑定接收器：" + receiverName
                                + " (" + target.getX() + ", " + target.getY() + ", " + target.getZ() + ")"));
                        return;
                    }
                }
            }
        }

        player.sendSystemMessage(Component.literal("没有在摄像头下方 5x5x5 范围内找到高别师的接收器。"));
    }

    private void triggerReceiver(ServerLevel serverLevel) {
        if (receiverOffset == null) {
            return;
        }

        BlockPos receiverPos = worldPosition.offset(receiverOffset);
        if (serverLevel.getBlockState(receiverPos).is(GbsBlocks.RECEIVER.get())) {
            ReceiverBlock.trigger(serverLevel, receiverPos, 40);
        }
    }

    private void playAlertSound(ServerLevel serverLevel) {
        if (alertVolume <= 0.0F) {
            return;
        }

        ResourceLocation soundId = new ResourceLocation(GaobieshiSecurity.MOD_ID, "lawson");
        serverLevel.playSound(null, worldPosition, SoundEvent.createVariableRangeEvent(soundId),
                SoundSource.BLOCKS, alertVolume, 1.0F);
    }

    private String getReceiverName(BlockPos pos) {
        if (level != null && level.getBlockEntity(pos) instanceof ReceiverBlockEntity receiver) {
            return receiver.getReceiverName();
        }
        return "高别师的接收器";
    }

    private String formatVolume() {
        return Math.round(alertVolume * 100.0F) + "%";
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Enabled", enabled);
        tag.putString("CameraName", cameraName);
        tag.putString("OwnerName", getOwnerName());
        tag.putFloat("AlertVolume", alertVolume);

        ListTag permittedTag = new ListTag();
        for (String name : permittedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", name);
            permittedTag.add(entry);
        }
        tag.put("PermittedPlayers", permittedTag);

        ListTag trustedTag = new ListTag();
        for (String name : trustedPlayers) {
            CompoundTag entry = new CompoundTag();
            entry.putString("Name", name);
            trustedTag.add(entry);
        }
        tag.put("TrustedPlayers", trustedTag);

        ListTag watchTag = new ListTag();
        for (BlockPos offset : watchOffsets) {
            CompoundTag entry = new CompoundTag();
            entry.putInt("X", offset.getX());
            entry.putInt("Y", offset.getY());
            entry.putInt("Z", offset.getZ());
            watchTag.add(entry);
        }
        tag.put("WatchOffsets", watchTag);

        if (receiverOffset != null) {
            tag.putInt("ReceiverX", receiverOffset.getX());
            tag.putInt("ReceiverY", receiverOffset.getY());
            tag.putInt("ReceiverZ", receiverOffset.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        enabled = tag.getBoolean("Enabled");
        cameraName = tag.contains("CameraName") ? tag.getString("CameraName") : cameraName;
        ownerName = tag.contains("OwnerName") ? tag.getString("OwnerName") : ownerName;
        alertVolume = tag.contains("AlertVolume") ? tag.getFloat("AlertVolume") : alertVolume;

        permittedPlayers.clear();
        ListTag permittedTag = tag.getList("PermittedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < permittedTag.size(); i++) {
            permittedPlayers.add(permittedTag.getCompound(i).getString("Name"));
        }
        if (!ownerName.isEmpty()) {
            permittedPlayers.add(ownerName);
        }

        trustedPlayers.clear();
        ListTag trustedTag = tag.getList("TrustedPlayers", Tag.TAG_COMPOUND);
        for (int i = 0; i < trustedTag.size(); i++) {
            trustedPlayers.add(trustedTag.getCompound(i).getString("Name"));
        }

        watchOffsets.clear();
        ListTag watchTag = tag.getList("WatchOffsets", Tag.TAG_COMPOUND);
        for (int i = 0; i < watchTag.size(); i++) {
            CompoundTag entry = watchTag.getCompound(i);
            watchOffsets.add(new BlockPos(entry.getInt("X"), entry.getInt("Y"), entry.getInt("Z")));
        }

        if (tag.contains("ReceiverX")) {
            receiverOffset = new BlockPos(tag.getInt("ReceiverX"), tag.getInt("ReceiverY"), tag.getInt("ReceiverZ"));
        } else {
            receiverOffset = null;
        }
    }
}
