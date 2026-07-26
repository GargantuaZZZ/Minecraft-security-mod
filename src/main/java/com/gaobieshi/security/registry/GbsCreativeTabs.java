package com.gaobieshi.security.registry;

import com.gaobieshi.security.GaobieshiSecurity;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class GbsCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, GaobieshiSecurity.MOD_ID);

    public static final RegistryObject<CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.gaobieshi_security.main"))
                    .icon(() -> new ItemStack(GbsBlocks.CAMERA_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(GbsBlocks.CAMERA_ITEM.get());
                        output.accept(GbsBlocks.RECEIVER_ITEM.get());
                    })
                    .build());

    private GbsCreativeTabs() {
    }
}

