package com.falsepattern.chunk.internal.vanilla;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;

import java.nio.ByteBuffer;

public class BiomeManager extends VanillaManager {
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
    public void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data) {
        if (forceUpdate) {
            data.put(chunk.getBiomeArray());
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer) {
        if (forceUpdate) {
            buffer.get(chunk.getBiomeArray());
        }
    }

    @Override
    public void writeToNBT(Chunk chunk, @NotNull NBTTagCompound tag) {

    }

    @Override
    public void readFromNBT(Chunk chunk, @NotNull NBTTagCompound tag) {

    }
}
