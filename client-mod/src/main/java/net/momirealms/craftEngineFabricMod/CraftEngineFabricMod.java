package net.momirealms.craftEngineFabricMod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import net.momirealms.craftEngineFabricMod.util.RegisterBlocks;
import net.momirealms.craftEngineFabricMod.util.YamlUtils;

import java.io.IOException;
import java.util.Map;

public class CraftEngineFabricMod implements ModInitializer {
    public static final String MOD_ID = "craftengine";

    @Override
    public void onInitialize() {
        try {
            YamlUtils.ensureConfigFile("additional-real-blocks.yml");
            YamlUtils.ensureConfigFile("mappings.yml");
            Map<Identifier, Integer> map = YamlUtils.loadMappingsAndAdditionalBlocks();
            System.out.println("Loaded " + map.size() + " additional real blocks.");
            for (Map.Entry<Identifier, Integer> entry : map.entrySet()) {
                Identifier replacedBlockId = entry.getKey();
                for (int i = 0; i < entry.getValue(); i++) {
                    RegisterBlocks.register(replacedBlockId.getPath() + "_" + i);
                }
            }
            System.out.println("Registered " + map.size() + " additional real blocks.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
