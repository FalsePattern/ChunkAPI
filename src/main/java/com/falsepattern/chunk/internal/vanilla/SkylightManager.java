/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.chunk.api.DataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public class SkylightManager extends NibbleManager implements DataManager.SubChunkDataManager {
    @Override
    public String id() {
        return "skylight";
    }

    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage subChunk) {
        return subChunk.getSkylightArray();
    }

    @Override
    public void writeToBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer data) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.writeToBuffer(chunk, subChunkMask, forceUpdate, data);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.readFromBuffer(chunk, subChunkMask, forceUpdate, buffer);
        }
    }

    @Override
    public boolean subChunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        if (!chunk.worldObj.provider.hasNoSky) {
            nbt.setByteArray("SkyLight", subChunk.getSkylightArray().data);
        } else {
            nbt.setByteArray("SkyLight", new byte[subChunk.getBlocklightArray().data.length]);
        }
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        if (!chunk.worldObj.provider.hasNoSky) {
            subChunk.setSkylightArray(new NibbleArray(nbt.getByteArray("SkyLight"), 4));
        }
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        if (!fromChunk.worldObj.provider.hasNoSky) {
            to.setSkylightArray(ArrayUtil.copyArray(from.getSkylightArray(), to.getSkylightArray()));
        } else {
            to.setSkylightArray(null);
        }
    }
}
