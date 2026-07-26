package com.gaobieshi.security.registry;

import com.gaobieshi.security.GaobieshiSecurity;
import com.gaobieshi.security.blockentity.CameraBlockEntity;
import com.gaobieshi.security.blockentity.ReceiverBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class GbsBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, GaobieshiSecurity.MOD_ID);

    public static final RegistryObject<BlockEntityType<CameraBlockEntity>> CAMERA = BLOCK_ENTITIES.register(
            "camera",
            () -> BlockEntityType.Builder.of(CameraBlockEntity::new, GbsBlocks.CAMERA.get()).build(null)
    );

    public static final RegistryObject<BlockEntityType<ReceiverBlockEntity>> RECEIVER = BLOCK_ENTITIES.register(
            "receiver",
            () -> BlockEntityType.Builder.of(ReceiverBlockEntity::new, GbsBlocks.RECEIVER.get()).build(null)
    );

    private GbsBlockEntities() {
    }
}
