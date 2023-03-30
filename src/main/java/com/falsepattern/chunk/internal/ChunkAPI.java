/*
 * -------------------------------------------------------------------------------
 * @author FalsePattern
 *
 * Copyright 2023
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 * -------------------------------------------------------------------------------
 */

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataRegistry;
import com.falsepattern.chunk.internal.vanilla.BiomeManager;
import com.falsepattern.chunk.internal.vanilla.BlockIDManager;
import com.falsepattern.chunk.internal.vanilla.BlocklightManager;
import com.falsepattern.chunk.internal.vanilla.MetadataManager;
import com.falsepattern.chunk.internal.vanilla.SkylightManager;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.10.15,);")
public class ChunkAPI {

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ChunkDataRegistry.registerDataManager(new BlockIDManager());
        ChunkDataRegistry.registerDataManager(new MetadataManager());
        ChunkDataRegistry.registerDataManager(new BlocklightManager());
        ChunkDataRegistry.registerDataManager(new SkylightManager());
        ChunkDataRegistry.registerDataManager(new BiomeManager());
    }
}