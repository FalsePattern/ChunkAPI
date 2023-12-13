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

public class BlocklightManager extends NibbleManager implements DataManager.SubchunkDataManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage subchunk) {
        return subchunk.getBlocklightArray();
    }

    @Override
    public String id() {
        return "blocklight";
    }

    @Override
    public boolean subchunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubchunkToNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        nbt.setByteArray("BlockLight", subchunk.getBlocklightArray().data);
    }

    @Override
    public void readSubchunkFromNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        subchunk.setBlocklightArray(new NibbleArray(nbt.getByteArray("BlockLight"), 4));
    }

    @Override
    public void cloneSubchunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        from.setBlocklightArray(ArrayUtil.copyArray(from.getBlocklightArray(), to.getBlocklightArray()));
    }
}
