package net.momirealms.craftEngineFabricMod.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

public class CraftEngineFabricModClient implements ClientModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitializeClient() {
        Registries.BLOCK.forEach(block -> {
            Identifier id = Registries.BLOCK.getId(block);
            if (id.getNamespace().equals(CraftEngineFabricModClient.MOD_ID)) {
                BlockRenderLayerMap.INSTANCE.putBlock(block, RenderLayer.getTranslucent());
            }
        });
    }
}
