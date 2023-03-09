package com.falsepattern.chunk.internal;

import lombok.Getter;

import cpw.mods.fml.common.Mod;
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

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        registrationStage = true;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        registrationStage = false;
    }
}