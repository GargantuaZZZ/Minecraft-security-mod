package com.gaobieshi.security.registry;

import com.gaobieshi.security.GaobieshiSecurity;
import com.gaobieshi.security.block.CameraBlock;
import com.gaobieshi.security.block.ReceiverBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GbsBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, GaobieshiSecurity.MOD_ID);
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, GaobieshiSecurity.MOD_ID);

    public static final RegistryObject<Block> CAMERA = BLOCKS.register("camera", () ->
            new CameraBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()));

    public static final RegistryObject<Block> RECEIVER = BLOCKS.register("receiver", () ->
            new ReceiverBlock(BlockBehaviour.Properties.of()
                    .strength(3.0F, 6.0F)
                    .sound(SoundType.METAL)
                    .requiresCorrectToolForDrops()));

    public static final RegistryObject<Block> AREA_MARKER = BLOCKS.register("area_marker", () ->
            new Block(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)));

    public static final RegistryObject<Block> AREA_MARKER_SELECTED = BLOCKS.register("area_marker_selected", () ->
            new Block(BlockBehaviour.Properties.of()
                    .noCollission()
                    .noOcclusion()
                    .strength(-1.0F, 3600000.0F)));

    public static final RegistryObject<Item> CAMERA_ITEM = ITEMS.register("camera", () ->
            new BlockItem(CAMERA.get(), new Item.Properties()));

    public static final RegistryObject<Item> RECEIVER_ITEM = ITEMS.register("receiver", () ->
            new BlockItem(RECEIVER.get(), new Item.Properties()));

    private GbsBlocks() {
    }
}
