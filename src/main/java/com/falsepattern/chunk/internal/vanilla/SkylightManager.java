package com.falsepattern.chunk.internal.vanilla;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public class SkylightManager extends NibbleManager {
    @Override
    public String id() {
        return "skylight";
    }

    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage ebs) {
        return ebs.getSkylightArray();
    }

    @Override
    public void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.writeToBuffer(chunk, ebsMask, forceUpdate, data);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.readFromBuffer(chunk, ebsMask, forceUpdate, buffer);
        }
    }
}
