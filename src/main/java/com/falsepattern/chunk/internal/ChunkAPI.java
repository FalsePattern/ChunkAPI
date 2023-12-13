/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.chunk.internal.vanilla.BiomeManager;
import com.falsepattern.chunk.internal.vanilla.BlockIDManager;
import com.falsepattern.chunk.internal.vanilla.BlocklightManager;
import com.falsepattern.chunk.internal.vanilla.LightingManager;
import com.falsepattern.chunk.internal.vanilla.MetadataManager;
import com.falsepattern.chunk.internal.vanilla.SkylightManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[1.0.0,);")
public class ChunkAPI {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ChunkDataRegistry.registerDataManager(new BlockIDManager());
        ChunkDataRegistry.registerDataManager(new MetadataManager());
        ChunkDataRegistry.registerDataManager(new LightingManager());
        ChunkDataRegistry.registerDataManager(new BlocklightManager());
        ChunkDataRegistry.registerDataManager(new SkylightManager());
        ChunkDataRegistry.registerDataManager(new BiomeManager());
    }
}