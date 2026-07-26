package com.gaobieshi.security.command;

import com.gaobieshi.security.blockentity.CameraBlockEntity;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public final class GbsCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("gbs")
                .executes(ctx -> info(ctx.getSource()))
                .then(Commands.literal("info")
                        .executes(ctx -> info(ctx.getSource())))
                .then(Commands.literal("toggle")
                        .executes(ctx -> withCamera(ctx.getSource(), CameraBlockEntity::toggle)))
                .then(Commands.literal("name")
                        .then(Commands.argument("name", StringArgumentType.greedyString())
                                .executes(ctx -> withCamera(ctx.getSource(), (camera, player) ->
                                        camera.setCameraName(player, StringArgumentType.getString(ctx, "name"))))))
                .then(Commands.literal("volume")
                        .then(Commands.argument("percent", IntegerArgumentType.integer(0, 200))
                                .executes(ctx -> withCamera(ctx.getSource(), (camera, player) ->
                                        camera.setAlertVolume(player, IntegerArgumentType.getInteger(ctx, "percent"))))))
                .then(Commands.literal("trust")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> withCamera(ctx.getSource(), (camera, player) ->
                                        camera.trust(player, StringArgumentType.getString(ctx, "player"))))))
                .then(Commands.literal("untrust")
                        .then(Commands.argument("player", StringArgumentType.word())
                                .executes(ctx -> withCamera(ctx.getSource(), (camera, player) ->
                                        camera.untrust(player, StringArgumentType.getString(ctx, "player"))))))
                .then(Commands.literal("area")
                        .then(Commands.literal("addall")
                                .executes(ctx -> withCamera(ctx.getSource(), CameraBlockEntity::addAllAir)))
                        .then(Commands.literal("clear")
                                .executes(ctx -> withCamera(ctx.getSource(), CameraBlockEntity::clearArea))))
                .then(Commands.literal("bind")
                        .executes(ctx -> withCamera(ctx.getSource(), CameraBlockEntity::bindReceiver))));
    }

    private static int info(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        CameraBlockEntity camera = CameraBlockEntity.getSelected(player);
        if (camera == null) {
            return 0;
        }
        camera.sendHelp(player);
        return 1;
    }

    private static int withCamera(CommandSourceStack source, CameraAction action)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        CameraBlockEntity camera = CameraBlockEntity.getSelected(player);
        if (camera == null) {
            return 0;
        }
        if (!camera.canConfigure(player)) {
            player.sendSystemMessage(net.minecraft.network.chat.Component.literal("你没有权限配置这个高别师的摄像头。"));
            return 0;
        }
        action.run(camera, player);
        return 1;
    }

    @FunctionalInterface
    private interface CameraAction {
        void run(CameraBlockEntity camera, ServerPlayer player);
    }

    private GbsCommands() {
    }
}
