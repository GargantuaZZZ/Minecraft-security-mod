package com.gaobieshi.security;

import com.gaobieshi.security.command.GbsCommands;
import com.gaobieshi.security.edit.GbsAreaEditHandler;
import com.gaobieshi.security.network.GbsNetwork;
import com.gaobieshi.security.registry.GbsBlockEntities;
import com.gaobieshi.security.registry.GbsBlocks;
import com.gaobieshi.security.registry.GbsCreativeTabs;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(GaobieshiSecurity.MOD_ID)
public class GaobieshiSecurity {
    public static final String MOD_ID = "gaobieshi_security";

    public GaobieshiSecurity() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
        GbsNetwork.register();
        GbsBlocks.BLOCKS.register(modBus);
        GbsBlocks.ITEMS.register(modBus);
        GbsBlockEntities.BLOCK_ENTITIES.register(modBus);
        GbsCreativeTabs.CREATIVE_MODE_TABS.register(modBus);

        MinecraftForge.EVENT_BUS.addListener(this::registerCommands);
        MinecraftForge.EVENT_BUS.addListener(GbsAreaEditHandler::onRightClickBlock);
        MinecraftForge.EVENT_BUS.addListener(GbsAreaEditHandler::onLeftClickBlock);
        MinecraftForge.EVENT_BUS.addListener(GbsAreaEditHandler::onRightClickItem);
        MinecraftForge.EVENT_BUS.addListener(GbsAreaEditHandler::onLogout);
    }

    private void registerCommands(RegisterCommandsEvent event) {
        GbsCommands.register(event.getDispatcher());
    }
}
