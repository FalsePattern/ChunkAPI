/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

public class LightingManager extends VanillaManager implements ChunkDataManager.ChunkNBTDataManager {
    @Override
    public String id() {
        return "lighting";
    }

    @Override
    public void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt) {
        nbt.setIntArray("HeightMap", chunk.heightMap);
        nbt.setBoolean("LightPopulated", chunk.isLightPopulated);
    }

    @Override
    public void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt) {
        chunk.heightMap = nbt.getIntArray("HeightMap");
        chunk.isLightPopulated = nbt.getBoolean("LightPopulated");
    }

    @Override
    public boolean chunkPrivilegedAccess() {
        return true;
    }
}
