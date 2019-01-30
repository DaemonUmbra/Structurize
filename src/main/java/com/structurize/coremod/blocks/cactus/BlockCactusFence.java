package com.structurize.coremod.blocks.cactus;

import com.structurize.api.util.constant.Constants;
import com.structurize.coremod.blocks.AbstractBlockStructurize;
import com.structurize.coremod.blocks.AbstractBlockStructurizeFence;
import com.structurize.coremod.creativetab.ModCreativeTabs;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

import java.util.Locale;

public class BlockCactusFence extends AbstractBlockStructurizeFence<BlockCactusFence>
{
    public BlockCactusFence()
    {
        super(Material.WOOD, MapColor.GREEN);
        setRegistryName(Constants.MOD_ID.toLowerCase() + ":" + "blockcactusfence");
        setTranslationKey(Constants.MOD_ID.toLowerCase(Locale.ENGLISH) + "." + "blockcactusfence");
        setCreativeTab(ModCreativeTabs.STRUCTURIZE);
        setSoundType(SoundType.WOOD);
    }
}
