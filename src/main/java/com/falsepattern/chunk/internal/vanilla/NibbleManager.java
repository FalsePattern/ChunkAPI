/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.internal.Common;
import lombok.val;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public abstract class NibbleManager extends VanillaManager implements ChunkDataManager.PacketDataManager {
    public static final int BYTES_PER_EBS = Common.BLOCKS_PER_EBS / 2;

    protected abstract NibbleArray getNibbleArray(ExtendedBlockStorage ebs);

    @Override
    public int maxPacketSize() {
        return Common.EBS_PER_CHUNK * BYTES_PER_EBS;
    }

    @Override
    public void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data) {
        val ebsArray = chunk.getBlockStorageArray();
        for (int i = 0; i < ebsArray.length; i++) {
            if ((ebsMask & (1 << i)) != 0) {
                val ebs = ebsArray[i];
                if (ebs != null) {
                    data.put(getNibbleArray(ebs).data, 0, BYTES_PER_EBS);
                }
            }
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer) {
        val ebsArray = chunk.getBlockStorageArray();
        for (int i = 0; i < ebsArray.length; i++) {
            if ((ebsMask & (1 << i)) != 0) {
                val ebs = ebsArray[i];
                if (ebs != null) {
                    buffer.get(getNibbleArray(ebs).data, 0, BYTES_PER_EBS);
                }
            }
        }
    }
}
