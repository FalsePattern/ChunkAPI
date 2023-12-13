/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.DataManager;
import com.falsepattern.chunk.api.ArrayUtil;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

import static com.falsepattern.chunk.internal.Common.BLOCKS_PER_SUBCHUNK;
import static com.falsepattern.chunk.internal.Common.SUBCHUNKS_PER_CHUNK;

public class BlockIDManager extends VanillaManager
        implements DataManager.PacketDataManager, DataManager.SubchunkDataManager {
    private static final int LSB_BYTES_PER_SUBCHUNK = BLOCKS_PER_SUBCHUNK;
    private static final int MSB_BYTES_PER_SUBCHUNK = BLOCKS_PER_SUBCHUNK / 2;
    private static final int HEADER_SIZE = 2;

    @Override
    public String id() {
        return "blockid";
    }

    @Override
    public int maxPacketSize() {
        return HEADER_SIZE + SUBCHUNKS_PER_CHUNK * (LSB_BYTES_PER_SUBCHUNK + MSB_BYTES_PER_SUBCHUNK);
    }

    @Override
    public void writeToBuffer(Chunk chunk, int subchunkMask, boolean forceUpdate, ByteBuffer data) {
        val subchunks = chunk.getBlockStorageArray();
        int currentPos = data.position();
        data.putShort((short) 0);
        int msbMask = 0;
        for (int i = 0; i < subchunks.length; i++) {
            if ((subchunkMask & (1 << i)) != 0) {
                val subchunk = subchunks[i];
                val lsb = subchunk.getBlockLSBArray();
                val msb = subchunk.getBlockMSBArray();
                data.put(lsb);
                if (msb != null) {
                    msbMask |= 1 << i;
                    data.put(msb.data);
                }
            }
        }
        int endPos = data.position();
        data.position(currentPos);
        data.putShort((short) msbMask);
        data.position(endPos);
    }

    @Override
    public void readFromBuffer(Chunk chunk, int subchunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val subchunks = chunk.getBlockStorageArray();
        val msbMask = buffer.getShort() & 0xFFFF;
        for (int i = 0; i < subchunks.length; i++) {
            val subchunk = subchunks[i];
            if ((subchunkMask & (1 << i)) != 0 && subchunk != null) {
                buffer.get(subchunk.getBlockLSBArray());
                if ((msbMask & (1 << i)) != 0) {
                    val msb = subchunk.getBlockMSBArray();
                    if (msb == null) {
                        subchunk.createBlockMSBArray();
                    }
                    buffer.get(subchunk.getBlockMSBArray().data);
                } else {
                    subchunk.setBlockMSBArray(null);
                }
            }
        }
    }

    @Override
    public boolean subchunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubchunkToNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        nbt.setByteArray("Blocks", subchunk.getBlockLSBArray());

        if (subchunk.getBlockMSBArray() != null) {
            nbt.setByteArray("Add", subchunk.getBlockMSBArray().data);
        }
    }

    @Override
    public void readSubchunkFromNBT(Chunk chunk, ExtendedBlockStorage subchunk, NBTTagCompound nbt) {
        subchunk.setBlockLSBArray(nbt.getByteArray("Blocks"));

        if (nbt.hasKey("Add", 7)) {
            subchunk.setBlockMSBArray(new NibbleArray(nbt.getByteArray("Add"), 4));
        }
    }

    @Override
    public void cloneSubchunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        to.setBlockLSBArray(ArrayUtil.copyArray(from.getBlockLSBArray(), to.getBlockLSBArray()));
        to.setBlockMSBArray(ArrayUtil.copyArray(from.getBlockMSBArray(), to.getBlockMSBArray()));
    }
}
