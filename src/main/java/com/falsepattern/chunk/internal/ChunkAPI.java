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
import lombok.Getter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = Tags.MODID,
     version = Tags.VERSION,
     name = Tags.MODNAME,
     acceptedMinecraftVersions = "[1.7.10]",
     dependencies = "required-after:falsepatternlib@[0.10.15,);")
public class ChunkAPI {
    @Getter
    private static boolean registrationStage = false;

    @Getter
    private static boolean disableStage = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registrationStage = true;
        disableStage = true;
        ChunkDataRegistry.registerDataManager(new BlockIDManager());
        ChunkDataRegistry.registerDataManager(new MetadataManager());
        ChunkDataRegistry.registerDataManager(new BlocklightManager());
        ChunkDataRegistry.registerDataManager(new SkylightManager());
        ChunkDataRegistry.registerDataManager(new BiomeManager());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        registrationStage = false;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        disableStage = false;
    }
}