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
        implements DataManager.PacketDataManager, DataManager.SubChunkDataManager {
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
    public void writeToBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer data) {
        val subChunks = chunk.getBlockStorageArray();
        int currentPos = data.position();
        data.putShort((short) 0);
        int msbMask = 0;
        for (int i = 0; i < subChunks.length; i++) {
            if ((subChunkMask & (1 << i)) != 0) {
                val subChunk = subChunks[i];
                val lsb = subChunk.getBlockLSBArray();
                val msb = subChunk.getBlockMSBArray();
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
    public void readFromBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val subChunks = chunk.getBlockStorageArray();
        val msbMask = buffer.getShort() & 0xFFFF;
        for (int i = 0; i < subChunks.length; i++) {
            val subChunk = subChunks[i];
            if ((subChunkMask & (1 << i)) != 0 && subChunk != null) {
                buffer.get(subChunk.getBlockLSBArray());
                if ((msbMask & (1 << i)) != 0) {
                    val msb = subChunk.getBlockMSBArray();
                    if (msb == null) {
                        subChunk.createBlockMSBArray();
                    }
                    buffer.get(subChunk.getBlockMSBArray().data);
                } else {
                    subChunk.setBlockMSBArray(null);
                }
            }
        }
    }

    @Override
    public boolean subChunkPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        nbt.setByteArray("Blocks", subChunk.getBlockLSBArray());

        if (subChunk.getBlockMSBArray() != null) {
            nbt.setByteArray("Add", subChunk.getBlockMSBArray().data);
        }
    }

    @Override
    public void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        subChunk.setBlockLSBArray(nbt.getByteArray("Blocks"));

        if (nbt.hasKey("Add", 7)) {
            subChunk.setBlockMSBArray(new NibbleArray(nbt.getByteArray("Add"), 4));
        }
    }

    @Override
    public void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        to.setBlockLSBArray(ArrayUtil.copyArray(from.getBlockLSBArray(), to.getBlockLSBArray()));
        to.setBlockMSBArray(ArrayUtil.copyArray(from.getBlockMSBArray(), to.getBlockMSBArray()));
    }
}
