package com.ldtteam.structurize.util;

import com.ldtteam.structurize.api.util.ItemStackUtils;
import net.minecraft.block.*;
import net.minecraft.item.*;
import net.minecraft.state.BooleanProperty;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.registries.GameData;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

/**
 * Utility class for all Block type checking.
 */
public final class BlockUtils
{
    /**
     * Predicated to determine if a block is free to place.
     */
    @NotNull
    private static final List<BiPredicate<Block, IForgeBlockState>> freeToPlaceBlocks =
      Arrays.asList(
        (block, iBlockState) -> block.equals(Blocks.AIR),
        (block, iBlockState) -> iBlockState.getBlockState().getMaterial().isLiquid(),
        (block, iBlockState) -> BlockUtils.isWater(block.getDefaultState()),
        (block, iBlockState) -> block instanceof LeavesBlock,
        (block, iBlockState) -> block instanceof DoublePlantBlock,
        (block, iBlockState) -> block.equals(Blocks.GRASS),
        (block, iBlockState) -> block instanceof DoorBlock
                                  && iBlockState != null
                                  && iBlockState.getBlockState().get(BooleanProperty.create("upper"))

      );

    /**
     * Private constructor to hide the public one.
     */
    private BlockUtils()
    {
        //Hides implicit constructor.
    }

    /**
     * Updates the rotation of the structure depending on the input.
     *
     * @param rotation the rotation to be set.
     * @return returns the Rotation object.
     */
    public static Rotation getRotation(final int rotation)
    {
        switch (rotation)
        {
            case 1:
                return Rotation.CLOCKWISE_90;
            case 2:
                return Rotation.CLOCKWISE_180;
            case 3:
                return Rotation.COUNTERCLOCKWISE_90;
            default:
                return Rotation.NONE;
        }
    }

    /**
     * Gets a rotation from a block facing.
     *
     * @param facing the block facing.
     * @return the int rotation.
     */
    public static int getRotationFromFacing(final Direction facing)
    {
        switch (facing)
        {
            case SOUTH:
                return 2;
            case EAST:
                return 1;
            case WEST:
                return 3;
            default:
                return 0;
        }
    }

    /**
     * Get the filler block at a certain location.
     * If block follows gravity laws return dirt.
     *
     * @param world    the world the block is in.
     * @param location the location it is at.
     * @return the IBlockState of the filler block.
     */
    public static IForgeBlockState getSubstitutionBlockAtWorld(@NotNull final World world, @NotNull final BlockPos location)
    {
        final IForgeBlockState filler = world.getBiome(location).getSurfaceBuilderConfig().getTop();
        if (filler.getBlockState().getBlock() == Blocks.SAND)
        {
            return Blocks.SANDSTONE.getDefaultState();
        }
        if (filler.getBlockState().getBlock() instanceof FallingBlock)
        {
            return Blocks.DIRT.getDefaultState();
        }
        return filler;
    }

    /**
     * Checks if the block is water.
     *
     * @param iBlockState block state to be checked.
     * @return true if is water.
     */
    public static boolean isWater(final IForgeBlockState iBlockState)
    {
        return Objects.equals(iBlockState, Blocks.WATER.getDefaultState())
                 || Objects.equals(iBlockState, Blocks.WATER.getDefaultState());
    }

    private static Item getItem(@NotNull final IForgeBlockState forgeBlockState)
    {
        //todo test if beds and banners work and huge mushroom and doors and some redstone things too.
        final BlockState blockState = forgeBlockState.getBlockState();
        if (blockState.getBlockState().getBlock().equals(Blocks.LAVA))
        {
            return Items.LAVA_BUCKET;
        }
        else if (blockState.getBlock() instanceof BrewingStandBlock)
        {
            return Items.BREWING_STAND;
        }
        else if (blockState.getBlock() instanceof CakeBlock)
        {
            return Items.CAKE;
        }
        else if (blockState.getBlock() instanceof CauldronBlock)
        {
            return Items.CAULDRON;
        }
        else if (blockState.getBlock() instanceof CocoaBlock)
        {
            return Items.COCOA_BEANS;
        }
        else if (blockState.getBlock() instanceof CropsBlock)
        {
            final ItemStack stack = ((CropsBlock) blockState.getBlock()).getItem(null, null, blockState);
            if (stack != null)
            {
                return stack.getItem();
            }

            return Items.WHEAT_SEEDS;
        }
        else if (blockState.getBlock() instanceof DaylightDetectorBlock)
        {
            return Item.getItemFromBlock(Blocks.DAYLIGHT_DETECTOR);
        }
        else if (blockState.getBlock() instanceof FarmlandBlock || blockState.getBlock() instanceof GrassPathBlock)
        {
            return Item.getItemFromBlock(Blocks.DIRT);
        }
        else if (blockState.getBlock() instanceof FireBlock)
        {
            return Items.FLINT_AND_STEEL;
        }
        else if (blockState.getBlock() instanceof FlowerPotBlock)
        {
            return Items.FLOWER_POT;
        }
        else if (blockState.getBlock() instanceof FurnaceBlock)
        {
            return Item.getItemFromBlock(Blocks.FURNACE);
        }
        else if (blockState.getBlock() instanceof NetherWartBlock)
        {
            return Items.NETHER_WART;
        }
        else if (blockState.getBlock() instanceof RedstoneTorchBlock)
        {
            return Item.getItemFromBlock(Blocks.REDSTONE_TORCH);
        }
        else if (blockState.getBlock() instanceof RedstoneWireBlock)
        {
            return Items.REDSTONE;
        }
        else if (blockState.getBlock() instanceof SkullBlock)
        {
            return Items.SKELETON_SKULL;
        }
        else if (blockState.getBlock() instanceof StemBlock)
        {
            final ItemStack stack = ((StemBlock) blockState.getBlock()).getItem(null, null, blockState);
            if (!ItemStackUtils.isEmpty(stack))
            {
                return stack.getItem();
            }
            return Items.MELON_SEEDS;
        }
        else
        {
            return GameData.getBlockItemMap().get(blockState.getBlock());
        }
    }

    /**
     * Get a blockState from an itemStack.
     *
     * @param stack the stack to analyze.
     * @return the IBlockState.
     */
    public static BlockState getBlockStateFromStack(final ItemStack stack)
    {
        if (stack.getItem() == Items.AIR)
        {
            return Blocks.AIR.getDefaultState();
        }

        if (stack.getItem() == Items.WATER_BUCKET)
        {
            return Blocks.WATER.getDefaultState();
        }

        if (stack.getItem() == Items.LAVA_BUCKET)
        {
            return Blocks.LAVA.getDefaultState();
        }

        return stack.getItem() instanceof BlockItem ? ((BlockItem) stack.getItem()).getBlock().getDefaultState() : Blocks.GOLD_BLOCK.getDefaultState();
    }

    /**
     * Mimics pick block.
     *
     * @param blockState the block and state we are creating an ItemStack for.
     * @return ItemStack fromt the BlockState.
     */
    public static ItemStack getItemStackFromBlockState(@NotNull final IForgeBlockState blockState)
    {
        if (blockState.getBlockState().getBlock() instanceof IFluidBlock)
        {
            return FluidUtil.getFilledBucket(new FluidStack(((IFluidBlock) blockState.getBlockState().getBlock()).getFluid(), 1000));
        }
        final Item item = getItem(blockState);

        if (item == null)
        {
            return null;
        }

        return new ItemStack(blockState.getBlockState().getBlock(), 1);
    }

    /**
     * Handle the placement of a specific block for a blockState at a certain position with a fakePlayer.
     *
     * @param world      the world object.
     * @param fakePlayer the fake player to place.
     * @param itemStack  the describing itemStack.
     * @param blockState the blockState in the world.
     * @param here       the position.
     */
    public static void handleCorrectBlockPlacement(final World world, final FakePlayer fakePlayer, final ItemStack itemStack, final BlockState blockState, final BlockPos here)
    {
        final ItemStack stackToPlace = itemStack.copy();
        stackToPlace.setCount(stackToPlace.getMaxStackSize());
        fakePlayer.setHeldItem(Hand.MAIN_HAND, stackToPlace);

        if (itemStack.getItem() instanceof BedItem)
        {
            //todo beds?
            //fakePlayer.rotationYaw = blockState.getBlockState().get(BedBlock.).getHorizontalIndex() * 90;
        }

        //todo does placing down slabs and doors still work? and slabs?
        final Direction facing = (itemStack.getItem() instanceof BedItem ? Direction.UP : Direction.NORTH);
        ForgeHooks.onPlaceItemIntoWorld(new ItemUseContext(fakePlayer, Hand.MAIN_HAND, new BlockRayTraceResult(new Vec3d(0,0,0), facing, here, true)));

        final BlockState newBlockState = world.getBlockState(here);
        if (newBlockState.getBlock() instanceof StairsBlock && blockState.getBlock() instanceof StairsBlock)
        {
            BlockState transformation = newBlockState.with(StairsBlock.FACING, blockState.get(StairsBlock.FACING));
            transformation = transformation.with(StairsBlock.HALF, blockState.get(StairsBlock.HALF));
            transformation = transformation.with(StairsBlock.SHAPE, blockState.get(StairsBlock.SHAPE));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof HorizontalBlock && blockState.getBlock() instanceof HorizontalBlock
                   && !(blockState.getBlock() instanceof BedBlock))
        {
            final BlockState transformation = newBlockState.with(HorizontalBlock.HORIZONTAL_FACING, blockState.get(HorizontalBlock.HORIZONTAL_FACING));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof DirectionalBlock && blockState.getBlock() instanceof DirectionalBlock)
        {
            final BlockState transformation = newBlockState.with(DirectionalBlock.FACING, blockState.get(DirectionalBlock.FACING));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof SlabBlock && blockState.getBlock() instanceof SlabBlock)
        {
            final BlockState transformation;
            transformation = newBlockState.with(SlabBlock.TYPE, blockState.get(SlabBlock.TYPE));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof LogBlock && blockState.getBlock() instanceof LogBlock)
        {
            final BlockState transformation = newBlockState.with(LogBlock.AXIS, blockState.get(LogBlock.AXIS));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof RotatedPillarBlock && blockState.getBlock() instanceof RotatedPillarBlock)
        {
            final BlockState transformation = newBlockState.with(RotatedPillarBlock.AXIS, blockState.get(RotatedPillarBlock.AXIS));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof TrapDoorBlock && blockState.getBlock() instanceof TrapDoorBlock)
        {
            BlockState transformation = newBlockState.with(TrapDoorBlock.HALF, blockState.get(TrapDoorBlock.HALF));
            transformation = transformation.with(TrapDoorBlock.HORIZONTAL_FACING, blockState.get(TrapDoorBlock.HORIZONTAL_FACING));
            transformation = transformation.with(TrapDoorBlock.OPEN, blockState.get(TrapDoorBlock.OPEN));
            world.setBlockState(here, transformation);
        }
        else if (newBlockState.getBlock() instanceof DoorBlock && blockState.getBlock() instanceof DoorBlock)
        {
            final BlockState transformation = newBlockState.with(DoorBlock.FACING, blockState.get(DoorBlock.FACING));
            world.setBlockState(here, transformation);
        }
        else if (stackToPlace.getItem() == Items.LAVA_BUCKET)
        {
            world.setBlockState(here, Blocks.LAVA.getDefaultState());
        }
        else if (stackToPlace.getItem() == Items.WATER_BUCKET)
        {
            world.setBlockState(here, Blocks.WATER.getDefaultState());
        }
    }
}
