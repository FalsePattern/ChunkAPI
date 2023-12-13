/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.DataManager;
import com.falsepattern.chunk.api.ArrayUtil;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public class SkylightManager extends NibbleManager implements DataManager.SubchunkDataManager {
    @Override
    public String id() {
        return "skylight";
    }

    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage subchunk) {
        return subchunk.getSkylightArray();
    }

    @Override
    public void writeToBuffer(Chunk chunk, int subchunkMask, boolean forceUpdate, ByteBuffer data) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.writeToBuffer(chunk, subchunkMask, forceUpdate, data);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int subchunkMask, boolean forceUpdate, ByteBuffer buffer) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.readFromBuffer(chunk, subchunkMask, forceUpdate, buffer);
        }
    }

    @Override
    public boolean subchunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubchunkToNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        if (!chunk.worldObj.provider.hasNoSky) {
            nbt.setByteArray("SkyLight", subchunk.getSkylightArray().data);
        } else {
            nbt.setByteArray("SkyLight", new byte[subchunk.getBlocklightArray().data.length]);
        }
    }

    @Override
    public void readSubchunkFromNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        if (!chunk.worldObj.provider.hasNoSky) {
            subchunk.setSkylightArray(new NibbleArray(nbt.getByteArray("SkyLight"), 4));
        }
    }

    @Override
    public void cloneSubchunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        if (!fromChunk.worldObj.provider.hasNoSky) {
            to.setSkylightArray(ArrayUtil.copyArray(from.getSkylightArray(), to.getSkylightArray()));
        } else {
            to.setSkylightArray(null);
        }
    }
}
