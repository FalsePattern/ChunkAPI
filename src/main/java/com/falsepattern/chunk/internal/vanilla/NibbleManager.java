/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.DataManager;
import com.falsepattern.chunk.internal.Common;
import lombok.val;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public abstract class NibbleManager extends VanillaManager implements DataManager.PacketDataManager {
    public static final int BYTES_PER_SUBCHUNK = Common.BLOCKS_PER_SUBCHUNK / 2;

    protected abstract NibbleArray getNibbleArray(ExtendedBlockStorage subChunk);

    @Override
    public int maxPacketSize() {
        return Common.SUBCHUNKS_PER_CHUNK * BYTES_PER_SUBCHUNK;
    }

    @Override
    public void writeToBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer data) {
        val subChunks = chunk.getBlockStorageArray();
        for (int i = 0; i < subChunks.length; i++) {
            if ((subChunkMask & (1 << i)) != 0) {
                val subChunk = subChunks[i];
                if (subChunk != null) {
                    data.put(getNibbleArray(subChunk).data, 0, BYTES_PER_SUBCHUNK);
                }
            }
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, ByteBuffer buffer) {
        val subChunks = chunk.getBlockStorageArray();
        for (int i = 0; i < subChunks.length; i++) {
            if ((subChunkMask & (1 << i)) != 0) {
                val subChunk = subChunks[i];
                if (subChunk != null) {
                    buffer.get(getNibbleArray(subChunk).data, 0, BYTES_PER_SUBCHUNK);
                }
            }
        }
    }
}
