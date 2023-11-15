package com.falsepattern.chunk.internal.core;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import com.falsepattern.chunk.internal.Tags;
import com.google.common.eventbus.EventBus;
import lombok.Data;
import lombok.val;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.WorldAccessContainer;
import cpw.mods.fml.common.versioning.DefaultArtifactVersion;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ChunkAPICoreModContainer extends DummyModContainer implements WorldAccessContainer {
    public ChunkAPICoreModContainer() {
        super(createMetadata());
    }

    private static ModMetadata createMetadata() {
        val meta = new ModMetadata();
        meta.modId = Tags.MODID + "core";
        meta.name = Tags.MODNAME + " Core";
        meta.version = Tags.VERSION;
        meta.dependencies.add(new DefaultArtifactVersion(Tags.MODID, Tags.VERSION));
        meta.parent = Tags.MODID;
        return meta;
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller) {
        return true;
    }

    @Override
    public NBTTagCompound getDataForWriting(SaveHandler handler, WorldInfo info) {
        val tag = new NBTTagCompound();
        ChunkDataRegistryImpl.writeLevelDat(tag);
        return tag;
    }

    @Override
    public void readData(SaveHandler handler, WorldInfo info, Map<String, NBTBase> propertyMap, NBTTagCompound tag) {
        ChunkDataRegistryImpl.readLevelDat(tag);
    }
}
