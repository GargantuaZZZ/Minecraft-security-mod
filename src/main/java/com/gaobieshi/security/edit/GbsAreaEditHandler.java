package com.gaobieshi.security.edit;

import com.gaobieshi.security.blockentity.CameraBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class GbsAreaEditHandler {
    private static final Map<UUID, EditSession> SESSIONS = new HashMap<>();
    private static final String MARKER_NAME = "监控区域配置";
    private static final String EXIT_NAME = "退出编辑模式";

    public static void begin(ServerPlayer player, BlockPos cameraPos, CameraBlockEntity camera) {
        end(player);
        SESSIONS.put(player.getUUID(), new EditSession(cameraPos));
        giveEditItems(player);
        player.sendSystemMessage(Component.literal("已进入监控区域编辑：放置“监控区域配置”标记监控区域，破坏标记移除，右键“退出编辑模式”望远镜结束。"));
    }

    public static void end(ServerPlayer player) {
        EditSession session = SESSIONS.remove(player.getUUID());
        if (session != null && player.level() instanceof ServerLevel serverLevel) {
            clearSessionMarkers(serverLevel, session);
            removeEditItems(player);
            player.sendSystemMessage(Component.literal("已退出监控区域编辑。"));
        }
    }

    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (isExitItem(player.getItemInHand(event.getHand()))) {
            end(player);
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
            return;
        }
        if (isCameraClick(player, event.getPos())) {
            return;
        }
        if (isEditing(player) && isMarkerItem(player.getItemInHand(event.getHand()))) {
            BlockPos target = event.getPos().relative(event.getFace());
            handlePlace(player, target, player.getItemInHand(event.getHand()));
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (isCameraClick(player, event.getPos())) {
            return;
        }
        handleBreak(player, event.getPos());
        if (isEditing(player)) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            end(player);
        }
    }

    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (isEditing(player) && isExitItem(player.getItemInHand(event.getHand()))) {
            end(player);
            event.setCanceled(true);
        }
    }

    private static boolean isEditing(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    private static boolean isCameraClick(ServerPlayer player, BlockPos pos) {
        EditSession session = SESSIONS.get(player.getUUID());
        return session != null && session.cameraPos.equals(pos);
    }

    private static void handlePlace(ServerPlayer player, BlockPos markerPos, ItemStack markerStack) {
        EditSession session = SESSIONS.get(player.getUUID());
        if (session == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(serverLevel.getBlockEntity(session.cameraPos) instanceof CameraBlockEntity camera)) {
            end(player);
            return;
        }

        if (!camera.canConfigure(player)) {
            end(player);
            return;
        }

        BlockPos offset = markerPos.subtract(session.cameraPos);
        if (!camera.isValidWatchOffset(offset)) {
            player.sendSystemMessage(Component.literal("监控区域配置只能放在摄像头下方 5x5x5 范围内。"));
            return;
        }

        if (!serverLevel.getBlockState(markerPos).isAir()) {
            player.sendSystemMessage(Component.literal("监控区域配置只能放在空气方块中。"));
            return;
        }

        serverLevel.setBlock(markerPos, Blocks.LIME_STAINED_GLASS.defaultBlockState(), 3);
        camera.addWatchOffset(offset);
        session.markers.add(markerPos);
        if (!player.getAbilities().instabuild) {
            markerStack.shrink(1);
        }
        player.sendSystemMessage(Component.literal("已加入监控区域：" + formatPos(markerPos)));
    }

    private static void handleBreak(ServerPlayer player, BlockPos markerPos) {
        EditSession session = SESSIONS.get(player.getUUID());
        if (session == null || !(player.level() instanceof ServerLevel serverLevel)) {
            return;
        }

        if (!(serverLevel.getBlockEntity(session.cameraPos) instanceof CameraBlockEntity camera)) {
            end(player);
            return;
        }

        BlockPos offset = markerPos.subtract(session.cameraPos);
        if (!camera.isValidWatchOffset(offset)) {
            player.sendSystemMessage(Component.literal("只能破坏编辑模式中的监控区域配置标记。"));
            return;
        }

        if (!serverLevel.getBlockState(markerPos).is(Blocks.LIME_STAINED_GLASS)) {
            player.sendSystemMessage(Component.literal("这里只能破坏“监控区域配置”绿色玻璃。"));
            return;
        }

        serverLevel.setBlock(markerPos, Blocks.AIR.defaultBlockState(), 3);
        camera.removeWatchOffset(offset);
        session.markers.remove(markerPos);
        player.sendSystemMessage(Component.literal("已移除监控区域：" + formatPos(markerPos)));
    }

    private static void clearSessionMarkers(ServerLevel level, EditSession session) {
        for (BlockPos marker : session.markers) {
            if (level.getBlockState(marker).is(Blocks.LIME_STAINED_GLASS)) {
                level.setBlock(marker, Blocks.AIR.defaultBlockState(), 3);
            }
        }
    }

    private static void giveEditItems(ServerPlayer player) {
        ItemStack marker = new ItemStack(Items.LIME_STAINED_GLASS, 64);
        marker.setHoverName(Component.literal(MARKER_NAME));
        ItemStack exit = new ItemStack(Items.SPYGLASS);
        exit.setHoverName(Component.literal(EXIT_NAME));
        player.getInventory().add(marker);
        player.getInventory().add(exit);
    }

    private static void removeEditItems(ServerPlayer player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isMarkerItem(stack) || isExitItem(stack)) {
                player.getInventory().setItem(slot, ItemStack.EMPTY);
            }
        }
    }

    private static boolean isMarkerItem(ItemStack stack) {
        return stack.is(Items.LIME_STAINED_GLASS) && stack.hasCustomHoverName()
                && MARKER_NAME.equals(stack.getHoverName().getString());
    }

    private static boolean isExitItem(ItemStack stack) {
        return stack.is(Items.SPYGLASS) && stack.hasCustomHoverName()
                && EXIT_NAME.equals(stack.getHoverName().getString());
    }

    private static String formatPos(BlockPos pos) {
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    private static final class EditSession {
        private final BlockPos cameraPos;
        private final Set<BlockPos> markers = new HashSet<>();

        private EditSession(BlockPos cameraPos) {
            this.cameraPos = cameraPos;
        }
    }

    private GbsAreaEditHandler() {
    }
}
