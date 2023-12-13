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

public class MetadataManager extends NibbleManager implements DataManager.SubChunkDataManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage subChunk) {
        return subChunk.getMetadataArray();
    }

    @Override
    public String id() {
        return "metadata";
    }

    @Override
    public boolean subChunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        nbt.setByteArray("Data", subChunk.getMetadataArray().data);
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        subChunk.setBlockMetadataArray(new NibbleArray(nbt.getByteArray("Data"), 4));
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        to.setBlockMetadataArray(ArrayUtil.copyArray(from.getMetadataArray(), to.getMetadataArray()));
    }
}
