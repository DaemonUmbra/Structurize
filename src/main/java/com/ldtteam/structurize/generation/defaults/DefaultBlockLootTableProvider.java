package com.ldtteam.structurize.generation.defaults;

import com.google.gson.JsonObject;
import com.ldtteam.structurize.blocks.ModBlocks;
import com.ldtteam.structurize.generation.AbstractLootTableProvider;
import com.ldtteam.structurize.generation.DataGeneratorConstants;
import net.minecraft.block.Block;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * This class generates the default loot_table for blocks.
 * (if a block is destroyed, it drops it's item).
 */
public class DefaultBlockLootTableProvider extends AbstractLootTableProvider
{
    private final DataGenerator generator;

    public DefaultBlockLootTableProvider(final DataGenerator generator)
    {
        this.generator = generator;
    }

    @Override
    public void act(@NotNull DirectoryCache cache) throws IOException
    {
        saveBlocks(ModBlocks.getTimberFrames(), cache);
        saveBlocks(ModBlocks.getPaperwalls(), cache);
        saveBlocks(ModBlocks.getShingles(), cache);
        saveBlocks(ModBlocks.getShingleSlabs(), cache);

        saveBlock(ModBlocks.blockSubstitution, cache);
        saveBlock(ModBlocks.blockSolidSubstitution, cache);

        saveBlock(ModBlocks.blockCactusPlank, cache);
        saveBlock(ModBlocks.blockCactusDoor, cache);
        saveBlock(ModBlocks.blockCactusTrapdoor, cache);
        saveBlock(ModBlocks.blockCactusStair, cache);
        saveBlock(ModBlocks.blockCactusSlab, cache);
        saveBlock(ModBlocks.blockCactusFence, cache);
        saveBlock(ModBlocks.blockCactusFenceGate, cache);

        saveBlock(ModBlocks.multiBlock, cache);
    }

    private <T extends Block> void saveBlocks(final List<T> blocks, final DirectoryCache cache) throws IOException
    {
        for (Block block : blocks)
        {
            saveBlock(block, cache);
        }
    }

    private void saveBlock(final Block block, final DirectoryCache cache) throws IOException
    {
        if (block.getRegistryName() != null)
        {
            final JsonObject lootTable = createDefaultBlockDropTable(block.getRegistryName());
            final Path savePath = generator.getOutputFolder().resolve(DataGeneratorConstants.LOOT_TABLES_DIR).resolve(block.getRegistryName().getPath() + ".json");
            IDataProvider.save(DataGeneratorConstants.GSON, cache, lootTable, savePath);
        }
    }

    @Override
    @NotNull
    public String getName()
    {
        return "Default Block Loot Tables Provider";
    }
}
