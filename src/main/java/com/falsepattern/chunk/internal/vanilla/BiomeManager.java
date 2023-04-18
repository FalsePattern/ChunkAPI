/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import java.nio.ByteBuffer;

public class BiomeManager extends VanillaManager
        implements ChunkDataManager.PacketDataManager, ChunkDataManager.ChunkNBTDataManager {
    public static final int BYTES_PER_CHUNK = 256;

    @Override
    public String id() {
        return "biome";
    }

    @Override
    public int maxPacketSize() {
        return BYTES_PER_CHUNK;
    }

    @Override
    public void writeToBuffer(@NotNull Chunk chunk, int ebsMask, boolean forceUpdate, @NotNull ByteBuffer data) {
        if (forceUpdate) {
            data.put(chunk.getBiomeArray());
        }
    }

    @Override
    public void readFromBuffer(@NotNull Chunk chunk, int ebsMask, boolean forceUpdate, @NotNull ByteBuffer buffer) {
        if (forceUpdate) {
            buffer.get(chunk.getBiomeArray());
        }
    }

    @Override
    public boolean chunkPrivilegedAccess() {
        return true;
    }


    @Override
    public void writeChunkToNBT(@NotNull Chunk chunk, @NotNull NBTTagCompound nbt) {
        nbt.setByteArray("Biomes", chunk.getBiomeArray());
    }

    @Override
    public void readChunkFromNBT(@NotNull Chunk chunk, @NotNull NBTTagCompound nbt) {
        if (nbt.hasKey("Biomes", 7)) {
            chunk.setBiomeArray(nbt.getByteArray("Biomes"));
        }
    }
}
