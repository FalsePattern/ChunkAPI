/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This program comes with additional permissions according to Section 7 of the
 * GNU Affero General Public License. See the full LICENSE file for details.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ArrayUtil;
import com.falsepattern.chunk.api.DataManager;
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
