package net.momirealms.craftengine.bukkit.item.behavior;

import net.momirealms.craftengine.bukkit.api.CraftEngineBlocks;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockAttemptPlaceEvent;
import net.momirealms.craftengine.bukkit.api.event.CustomBlockPlaceEvent;
import net.momirealms.craftengine.bukkit.block.BukkitBlockManager;
import net.momirealms.craftengine.bukkit.nms.FastNMS;
import net.momirealms.craftengine.bukkit.util.*;
import net.momirealms.craftengine.core.block.CustomBlock;
import net.momirealms.craftengine.core.block.ImmutableBlockState;
import net.momirealms.craftengine.core.block.UpdateOption;
import net.momirealms.craftengine.core.entity.player.InteractionHand;
import net.momirealms.craftengine.core.entity.player.InteractionResult;
import net.momirealms.craftengine.core.entity.player.Player;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.behavior.ItemBehavior;
import net.momirealms.craftengine.core.item.behavior.ItemBehaviorFactory;
import net.momirealms.craftengine.core.item.context.BlockPlaceContext;
import net.momirealms.craftengine.core.item.context.UseOnContext;
import net.momirealms.craftengine.core.pack.Pack;
import net.momirealms.craftengine.core.plugin.CraftEngine;
import net.momirealms.craftengine.core.plugin.config.Config;
import net.momirealms.craftengine.core.util.Key;
import net.momirealms.craftengine.core.util.MiscUtils;
import net.momirealms.craftengine.core.world.BlockPos;
import net.momirealms.craftengine.core.world.Vec3d;
import org.bukkit.Bukkit;
import org.bukkit.GameEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

public class BlockItemBehavior extends ItemBehavior {
    public static final Factory FACTORY = new Factory();
    private final Key blockId;

    public BlockItemBehavior(Key blockId) {
        this.blockId = blockId;
    }

    @Override
    public InteractionResult useOnBlock(UseOnContext context) {
        return this.place(new BlockPlaceContext(context));
    }

    public InteractionResult place(BlockPlaceContext context) {
        if (!context.canPlace()) {
            return InteractionResult.FAIL;
        }
        Optional<CustomBlock> optionalBlock = BukkitBlockManager.instance().blockById(this.blockId);
        if (optionalBlock.isEmpty()) {
            CraftEngine.instance().logger().warn("Failed to place unknown block " + this.blockId);
            return InteractionResult.FAIL;
        }
        CustomBlock block = optionalBlock.get();
        BlockPlaceContext placeContext = updatePlacementContext(context);
        if (placeContext == null) {
            return InteractionResult.FAIL;
        }
        ImmutableBlockState blockStateToPlace = getPlacementState(placeContext, block);
        if (blockStateToPlace == null) {
            return InteractionResult.FAIL;
        }
        Player player = placeContext.getPlayer();
        BlockPos pos = placeContext.getClickedPos();
        BlockPos againstPos = placeContext.getAgainstPos();
        World world = (World) placeContext.getLevel().platformWorld();
        Location placeLocation = new Location(world, pos.x(), pos.y(), pos.z());

        int gameTicks = player.gameTicks();
        if (!player.updateLastSuccessfulInteractionTick(gameTicks)) {
            return InteractionResult.FAIL;
        }

        Block bukkitBlock = world.getBlockAt(placeLocation);
        Block againstBlock = world.getBlockAt(againstPos.x(), againstPos.y(), againstPos.z());
        org.bukkit.entity.Player bukkitPlayer = (org.bukkit.entity.Player) player.platformPlayer();

        if (player.isAdventureMode()) {
            Object againstBlockState = BlockStateUtils.blockDataToBlockState(againstBlock.getBlockData());
            int stateId = BlockStateUtils.blockStateToId(againstBlockState);
            if (BlockStateUtils.isVanillaBlock(stateId)) {
                if (!AdventureModeUtils.canPlace(context.getItem(), context.getLevel(), againstPos, againstBlockState)) {
                    return InteractionResult.FAIL;
                }
            } else {
                ImmutableBlockState customState = BukkitBlockManager.instance().getImmutableBlockStateUnsafe(stateId);
                // custom block
                if (!AdventureModeUtils.canPlace(context.getItem(), context.getLevel(), againstPos, Config.simplifyAdventurePlaceCheck() ? customState.vanillaBlockState().handle() : againstBlockState)) {
                    return InteractionResult.FAIL;
                }
            }
        }

        // trigger event
        CustomBlockAttemptPlaceEvent attemptPlaceEvent = new CustomBlockAttemptPlaceEvent(bukkitPlayer, placeLocation.clone(), blockStateToPlace,
                DirectionUtils.toBlockFace(context.getClickedFace()), bukkitBlock, context.getHand());
        if (EventUtils.fireAndCheckCancel(attemptPlaceEvent)) {
            return InteractionResult.FAIL;
        }

        // it's just world + pos
        BlockState previousState = bukkitBlock.getState();
        // place custom block
        CraftEngineBlocks.place(placeLocation, blockStateToPlace, UpdateOption.UPDATE_ALL_IMMEDIATE, false);
        // call bukkit event
        BlockPlaceEvent bukkitPlaceEvent = new BlockPlaceEvent(bukkitBlock, previousState, againstBlock, (ItemStack) placeContext.getItem().getItem(), bukkitPlayer, true, context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
        if (EventUtils.fireAndCheckCancel(bukkitPlaceEvent)) {
            // revert changes
            previousState.update(true, false);
            return InteractionResult.FAIL;
        }

        // call custom event
        CustomBlockPlaceEvent customPlaceEvent = new CustomBlockPlaceEvent(bukkitPlayer, placeLocation.clone(), blockStateToPlace, world.getBlockAt(placeLocation), context.getHand());
        if (EventUtils.fireAndCheckCancel(customPlaceEvent)) {
            // revert changes
            previousState.update(true, false);
            return InteractionResult.FAIL;
        }

        if (!player.isCreativeMode()) {
            Item<?> item = placeContext.getItem();
            item.count(item.count() - 1);
            item.load();
        }

        player.swingHand(placeContext.getHand());
        placeContext.getLevel().playBlockSound(new Vec3d(pos.x() + 0.5, pos.y() + 0.5, pos.z() + 0.5), blockStateToPlace.sounds().placeSound());
        world.sendGameEvent(bukkitPlayer, GameEvent.BLOCK_PLACE, new Vector(pos.x(), pos.y(), pos.z()));
        return InteractionResult.SUCCESS;
    }

    // for child class to override
    @Nullable
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        return context;
    }

    protected ImmutableBlockState getPlacementState(BlockPlaceContext context, CustomBlock block) {
        ImmutableBlockState state = block.getStateForPlacement(context);
        return state != null && this.canPlace(context, state) ? state : null;
    }

    protected boolean checkStatePlacement() {
        return true;
    }

    protected boolean canPlace(BlockPlaceContext context, ImmutableBlockState state) {
        try {
            Object player = context.getPlayer().serverPlayer();
            Object blockState = state.customBlockState().handle();
            Object blockPos = LocationUtils.toBlockPos(context.getClickedPos());
            Object voxelShape = Reflections.method$CollisionContext$of.invoke(null, player);
            Object world = FastNMS.INSTANCE.field$CraftWorld$ServerLevel((World) context.getLevel().platformWorld());
            boolean defaultReturn = ((!this.checkStatePlacement() || (boolean) Reflections.method$BlockStateBase$canSurvive.invoke(blockState, world, blockPos))
                    && (boolean) Reflections.method$ServerLevel$checkEntityCollision.invoke(world, blockState, player, voxelShape, blockPos, true));
            Block block = FastNMS.INSTANCE.method$CraftBlock$at(world, blockPos);
            BlockData blockData = FastNMS.INSTANCE.method$CraftBlockData$fromData(blockState);
            BlockCanBuildEvent canBuildEvent = new BlockCanBuildEvent(block, (org.bukkit.entity.Player) context.getPlayer().platformPlayer(), blockData, defaultReturn, context.getHand() == InteractionHand.MAIN_HAND ? EquipmentSlot.HAND : EquipmentSlot.OFF_HAND);
            Bukkit.getPluginManager().callEvent(canBuildEvent);
            return canBuildEvent.isBuildable();
        } catch (ReflectiveOperationException e) {
            CraftEngine.instance().logger().warn("Failed to check canPlace", e);
            return false;
        }
    }

    public Key blockId() {
        return this.blockId;
    }

    public static class Factory implements ItemBehaviorFactory {
        @Override
        public ItemBehavior create(Pack pack, Path path, Key key, Map<String, Object> arguments) {
            Object id = arguments.get("block");
            if (id == null) {
                throw new IllegalArgumentException("Missing required parameter 'block' for block_item behavior");
            }
            if (id instanceof Map<?, ?> map) {
                BukkitBlockManager.instance().parser().parseSection(pack, path, key, MiscUtils.castToMap(map, false));
                return new BlockItemBehavior(key);
            } else {
                return new BlockItemBehavior(Key.of(id.toString()));
            }
        }
    }
}
