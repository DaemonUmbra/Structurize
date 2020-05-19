package com.ldtteam.structurize.placement;

import com.ldtteam.structures.helpers.BlueprintIterator;
import com.ldtteam.structures.helpers.IStructureHandler;
import com.ldtteam.structurize.api.util.ItemStackUtils;
import com.ldtteam.structurize.api.util.Log;
import com.ldtteam.structurize.placement.handlers.IPlacementHandler;
import com.ldtteam.structurize.placement.handlers.PlacementHandlers;
import com.ldtteam.structurize.util.*;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static com.ldtteam.structures.helpers.BlueprintIterator.NULL_POS;

/**
 * result = StructurePlacer.execute(this)
 *
 * if result = missingItems
 * return MISSING_ITE
 * else if result = block break
 * return BREAK_BLOCK
 * else if result = finished
 * return nextState
 * else
 * return getState()
 *
 * on success we have to write the increased progress pos to NBT.
 */

/**
 * Interface for using the structure codebase.
 */
public class StructurePlacer
{
    /**
     * The structure iterator.
     */
    protected final BlueprintIterator iterator;

    /**
     * The handler.
     */
    protected final IStructureHandler handler;

    /**
     * Create a new structure placer.
     *
     * @param handler the structure handler.
     */
    public StructurePlacer(final IStructureHandler handler)
    {
        this.iterator = new BlueprintIterator(handler);
        this.handler = handler;
    }

    /**
     * Execute structure placement.
     * @param world the world.
     * @param storage the change storage.
     * @param inputPos the pos to start from.
     * @param operation the operation to execute.
     * @param iterateFunction the function to iterate.
     * @param includeEntities if entities should be included.
     * @return the result.
     */
    public StructurePhasePlacementResult executeStructureStep(
      final World world,
      final ChangeStorage storage,
      final BlockPos inputPos,
      final Operation operation,
      final Supplier<BlueprintIterator.Result> iterateFunction,
      final boolean includeEntities)
    {
        if (includeEntities)
        {
            iterator.includeEntities();
        }

        iterator.setProgressPos(new BlockPos(inputPos.getX(), inputPos.getY(), inputPos.getZ()));
        if (inputPos.equals(NULL_POS) && iterateFunction.get() != BlueprintIterator.Result.NEW_BLOCK)
        {
            iterator.reset();
            return new StructurePhasePlacementResult(iterator.getProgressPos(), new BlockPlacementResult(iterator.getProgressPos(), BlockPlacementResult.Result.FINISHED));
        }

        int count = 0;

        do
        {
            @NotNull final BlockPos localPos = iterator.getProgressPos();
            if (count >= handler.getStepsPerCall())
            {
                return new StructurePhasePlacementResult(localPos, new BlockPlacementResult(localPos, BlockPlacementResult.Result.LIMIT_REACHED));
            }

            final BlockState localState = handler.getBluePrint().getBlockState(localPos);
            if (localState == null)
            {
                continue;
            }

            final BlockPos worldPos = handler.getWorldPos().add(localPos).subtract(handler.getBluePrint().getPrimaryBlockOffset());
            if (storage != null)
            {
                storage.addPositionStorage(worldPos, world);
            }

            final BlockPlacementResult result;
            switch (operation)
            {
                case BLOCK_REMOVAL:
                    world.removeBlock(worldPos, false);
                    result = new BlockPlacementResult(localPos, BlockPlacementResult.Result.SUCCESS);
                    break;
                case WATER_REMOVAL:
                    final BlockState worldState = world.getBlockState(worldPos);
                    if (worldState.getBlock() instanceof IBucketPickupHandler || worldState.getBlock() instanceof FlowingFluidBlock)
                    {
                        BlockUtils.removeFluid(world, worldPos);
                    }
                    result = new BlockPlacementResult(localPos, BlockPlacementResult.Result.SUCCESS);
                    break;
                default:
                    result = handleBlockPlacement(world, worldPos, storage, localState, handler.getBluePrint().getTileEntityData(worldPos, localPos));
            }
            count++;

            if (result.getResult() == BlockPlacementResult.Result.MISSING_ITEMS || result.getResult() == BlockPlacementResult.Result.FAIL || result.getResult() == BlockPlacementResult.Result.BREAK_BLOCK)
            {
                return new StructurePhasePlacementResult(localPos, result);
            }
        }
        while (iterateFunction.get() == BlueprintIterator.Result.NEW_BLOCK);

        iterator.reset();
        return new StructurePhasePlacementResult(iterator.getProgressPos(), new BlockPlacementResult(iterator.getProgressPos(), BlockPlacementResult.Result.FINISHED));
    }

    /**
     * This method handles the block placement.
     * When we extract this into another mod, we have to override the method.
     *
     * @param world          the world.
     * @param worldPos       the world position.
     * @param storage        the change storage.
     * @param localState     the local state.
     * @param tileEntityData the tileEntity.
     */
    public BlockPlacementResult handleBlockPlacement(
      final World world,
      final BlockPos worldPos,
      final ChangeStorage storage,
      final BlockState localState,
      final CompoundNBT tileEntityData)
    {
        final BlockState worldState = world.getBlockState(worldPos);
        boolean sameBlockInWorld = false;
        if (worldState.getBlock() == localState.getBlock())
        {
            sameBlockInWorld = true;
        }

        if (!(worldState.getBlock() instanceof AirBlock))
        {
            if (!handler.allowReplace())
            {
                return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.BREAK_BLOCK);
            }
        }

        for (final CompoundNBT compound : iterator.getBluePrintPositionInfo().getEntities())
        {
            if (compound != null)
            {
                try
                {
                    final BlockPos pos = this.handler.getWorldPos().subtract(handler.getBluePrint().getPrimaryBlockOffset());

                    final Optional<EntityType<?>> type = EntityType.readEntityType(compound);
                    if (type.isPresent())
                    {
                        final Entity entity = type.get().create(world);
                        if (entity != null)
                        {
                            entity.deserializeNBT(compound);

                            entity.setUniqueId(UUID.randomUUID());
                            final Vec3d posInWorld = entity.getPositionVector().add(pos.getX(), pos.getY(), pos.getZ());
                            entity.setPosition(posInWorld.x, posInWorld.y, posInWorld.z);

                            final List<? extends Entity> list = world.getEntitiesWithinAABB(entity.getClass(), new AxisAlignedBB(posInWorld.add(1,1,1), posInWorld.add(-1,-1,-1)));
                            boolean foundEntity = false;
                            for (Entity worldEntity: list)
                            {
                                if (worldEntity.getPositionVector().equals(entity.getPositionVector()))
                                {
                                    foundEntity = true;
                                    break;
                                }
                            }

                            if (foundEntity)
                            {
                                break;
                            }

                            world.addEntity(entity);
                            if (storage != null)
                            {
                                storage.addToBeKilledEntity(entity);
                            }
                        }
                    }
                }
                catch (final RuntimeException e)
                {
                    Log.getLogger().info("Couldn't restore entity", e);
                }
            }
        }

        for (final IPlacementHandler placementHandler : PlacementHandlers.handlers)
        {
            if (placementHandler.canHandle(world, worldPos, localState))
            {
                final List<ItemStack> requiredItems = new ArrayList<>();

                if (!sameBlockInWorld && !this.handler.isCreative())
                {
                    for (final ItemStack stack : placementHandler.getRequiredItems(world, worldPos, localState, tileEntityData, false))
                    {
                        if (!stack.isEmpty() && !this.handler.isStackFree(stack))
                        {
                            requiredItems.add(stack);
                        }
                    }

                    if (!InventoryUtils.hasRequiredItems(handler.getInventory(), requiredItems))
                    {
                        return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.MISSING_ITEMS, requiredItems);
                    }
                }

                if (!(worldState.getBlock() instanceof AirBlock))
                {
                    if (!sameBlockInWorld
                          && worldState.getMaterial() != Material.AIR
                          && !(worldState.getBlock() instanceof DoublePlantBlock && worldState.get(DoublePlantBlock.HALF).equals(DoubleBlockHalf.UPPER)))
                    {
                        if (!handler.isCreative())
                        {
                            final List<ItemStack> items = BlockUtils.getBlockDrops(world, worldPos, 0, handler.getHeldItem());
                            for (final ItemStack item : items)
                            {
                                InventoryUtils.transferIntoNextBestSlot(item, handler.getInventory());
                            }
                        }

                        world.removeBlock(worldPos, false);
                    }
                }

                final IPlacementHandler.ActionProcessingResult result = placementHandler.handle(world, worldPos, localState, tileEntityData, !this.handler.fancyPlacement(), this.handler.getWorldPos(), this.handler.getSettings());
                if (result == IPlacementHandler.ActionProcessingResult.DENY)
                {
                    return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.FAIL);
                }

                this.handler.triggerSuccess(worldPos);

                if (result == IPlacementHandler.ActionProcessingResult.PASS)
                {
                    return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.SUCCESS);
                }

                if (!this.handler.isCreative() && !sameBlockInWorld)
                {
                    this.handler.triggerSuccess(worldPos);
                    for (final ItemStack tempStack : requiredItems)
                    {
                        if (!ItemStackUtils.isEmpty(tempStack))
                        {
                            InventoryUtils.consumeStack(tempStack, handler.getInventory());
                        }
                    }
                }

                return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.SUCCESS);
            }
        }
        return new BlockPlacementResult(worldPos, BlockPlacementResult.Result.FAIL);
    }

    /**
     * result = StructurePlacer.execute(this)
     *
     * if result = missingItems
     * return MISSING_ITE
     * else if result = block break
     * return BREAK_BLOCK
     * else if result = finished
     * return nextState
     * else
     * return getState()
     *
     * on success we have to write the increased progress pos to NBT.
     */

    /**
     * Check if there is enough free space to place a structure in the world.
     *
     * @param pos coordinates
     * @return true if there is free space.
     */
    public boolean checkForFreeSpace(@NotNull final BlockPos pos)
    {
        iterator.setProgressPos(pos);
        while (iterator.increment() == BlueprintIterator.Result.NEW_BLOCK)
        {
            @NotNull final BlockPos localPos = iterator.getProgressPos();

            final BlockPos worldPos = pos.add(localPos);

            if (worldPos.getY() <= pos.getY() && !handler.getWorld().getBlockState(worldPos.down()).getMaterial().isSolid())
            {
                iterator.reset();
                return false;
            }

            final BlockState worldState = handler.getWorld().getBlockState(worldPos);
            if (worldState.getBlock() == Blocks.BEDROCK)
            {
                iterator.reset();
                return false;
            }

            if (worldPos.getY() > pos.getY() && worldState.getBlock() != Blocks.AIR)
            {
                iterator.reset();
                return false;
            }
        }

        iterator.reset();
        return true;
    }

    /**
     * Get the iterator instance.
     * @return the BlueprintIterator.
     */
    public BlueprintIterator getIterator()
    {
        return iterator;
    }

    /**
     * The different operations.
     */
    public enum Operation
    {
        WATER_REMOVAL,
        BLOCK_REMOVAL,
        BLOCK_PLACEMENT
    }

}
