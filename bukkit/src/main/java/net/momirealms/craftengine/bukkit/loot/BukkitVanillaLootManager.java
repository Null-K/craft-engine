package net.momirealms.craftengine.bukkit.loot;

import net.momirealms.craftengine.bukkit.plugin.BukkitCraftEngine;
import net.momirealms.craftengine.bukkit.plugin.user.BukkitServerPlayer;
import net.momirealms.craftengine.bukkit.util.BlockStateUtils;
import net.momirealms.craftengine.bukkit.util.KeyUtils;
import net.momirealms.craftengine.bukkit.util.Reflections;
import net.momirealms.craftengine.bukkit.world.BukkitWorld;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.loot.AbstractVanillaLootManager;
import net.momirealms.craftengine.core.loot.LootTable;
import net.momirealms.craftengine.core.loot.VanillaLoot;
import net.momirealms.craftengine.core.loot.parameter.LootParameters;
import net.momirealms.craftengine.core.pack.LoadingSequence;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.config.ConfigSectionParser;
import net.momirealms.craftengine.core.plugin.locale.TranslationManager;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.util.VersionHelper;
import net.momirealms.craftengine.core.util.context.ContextHolder;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

// note: block listeners are in BlockEventListener to reduce performance cost
public class BukkitVanillaLootManager extends AbstractVanillaLootManager implements Listener {
    private final BukkitCraftEngine plugin;
    private final VanillaLootParser vanillaLootParser;

    public BukkitVanillaLootManager(BukkitCraftEngine plugin) {
        this.plugin = plugin;
        this.vanillaLootParser = new VanillaLootParser();
    }

    @Override
    public void delayedInit() {
        Bukkit.getPluginManager().registerEvents(this, plugin.bootstrap());
    }

    @Override
    public void disable() {
        HandlerList.unregisterAll(this);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        Key key = KeyUtils.namespacedKey2Key(entity.getType().getKey());
        Optional.ofNullable(this.entityLoots.get(key)).ifPresent(loot -> {
            if (loot.override()) {
                event.getDrops().clear();
                event.setDroppedExp(0);
            }
            Location location = entity.getLocation();
            net.momirealms.craftengine.core.world.World world = new BukkitWorld(entity.getWorld());
            Vec3d vec3d = new Vec3d(location.getBlockX() + 0.5, location.getBlockY() + 0.5, location.getBlockZ() + 0.5);
            ContextHolder.Builder builder = ContextHolder.builder();
            builder.withParameter(LootParameters.WORLD, world);
            builder.withParameter(LootParameters.LOCATION, vec3d);
            if (VersionHelper.isVersionNewerThan1_20_5()) {
                if (event.getDamageSource().getCausingEntity() instanceof Player player) {
                    BukkitServerPlayer serverPlayer = this.plugin.adapt(player);
                    builder.withParameter(LootParameters.PLAYER, serverPlayer);
                    builder.withOptionalParameter(LootParameters.TOOL, serverPlayer.getItemInHand(InteractionHand.MAIN_HAND));
                }
            }
            ContextHolder contextHolder = builder.build();
            for (LootTable<?> lootTable : loot.lootTables()) {
                for (Item<?> item : lootTable.getRandomItems(contextHolder, world)) {
                    world.dropItemNaturally(vec3d, item);
                }
            }
        });
    }

    @Override
    public ConfigSectionParser parser() {
        return this.vanillaLootParser;
    }

    public class VanillaLootParser implements ConfigSectionParser {
        public static final String[] CONFIG_SECTION_NAME = new String[] {"vanilla-loots", "vanilla-loot", "loots", "loot"};

        @Override
        public int loadingSequence() {
            return LoadingSequence.VANILLA_LOOTS;
        }

        @Override
        public String[] sectionId() {
            return CONFIG_SECTION_NAME;
        }

        @Override
        public void parseSection(Pack pack, Path path, Key id, Map<String, Object> section) {
            String type = (String) section.get("type");
            if (type == null) {
                TranslationManager.instance().log("warning.config.vanilla_loot.type_not_exist", path.toString(), id.toString());
                return;
            }
            VanillaLoot.Type typeEnum = VanillaLoot.Type.valueOf(type.toUpperCase(Locale.ENGLISH));
            boolean override = (boolean) section.getOrDefault("override", false);
            List<String> targets = MiscUtils.getAsStringList(section.getOrDefault("target", List.of()));
            LootTable<?> lootTable = LootTable.fromMap(MiscUtils.castToMap(section.get("loot"), false));
            switch (typeEnum) {
                case BLOCK -> {
                    for (String target : targets) {
                        if (target.endsWith("]") && target.contains("[")) {
                            java.lang.Object blockState = BlockStateUtils.blockDataToBlockState(Bukkit.createBlockData(target));
                            if (blockState == Reflections.instance$Blocks$AIR$defaultState) {
                                TranslationManager.instance().log("warning.config.vanilla_loot.block.invalid_target", path.toString(), id.toString(), target);
                                return;
                            }
                            VanillaLoot vanillaLoot = blockLoots.computeIfAbsent(BlockStateUtils.blockStateToId(blockState), k -> new VanillaLoot(VanillaLoot.Type.BLOCK));
                            vanillaLoot.addLootTable(lootTable);
                        } else {
                            for (Object blockState : BlockStateUtils.getAllVanillaBlockStates(Key.of(target))) {
                                if (blockState == Reflections.instance$Blocks$AIR$defaultState) {
                                    TranslationManager.instance().log("warning.config.vanilla_loot.block.invalid_target", path.toString(), id.toString(), target);
                                    return;
                                }
                                VanillaLoot vanillaLoot = blockLoots.computeIfAbsent(BlockStateUtils.blockStateToId(blockState), k -> new VanillaLoot(VanillaLoot.Type.BLOCK));
                                if (override) vanillaLoot.override(true);
                                vanillaLoot.addLootTable(lootTable);
                            }
                        }
                    }
                }
                case ENTITY -> {
                    for (String target : targets) {
                        Key key = Key.of(target);
                        VanillaLoot vanillaLoot = entityLoots.computeIfAbsent(key, k -> new VanillaLoot(VanillaLoot.Type.ENTITY));
                        vanillaLoot.addLootTable(lootTable);
                        if (override) vanillaLoot.override(true);
                    }
                }
            }
        }
    }
}
