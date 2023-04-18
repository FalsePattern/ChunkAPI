/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;
import lombok.val;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

import static com.falsepattern.chunk.internal.Common.BLOCKS_PER_EBS;
import static com.falsepattern.chunk.internal.Common.EBS_PER_CHUNK;

public class BlockIDManager extends VanillaManager
        implements ChunkDataManager.PacketDataManager, ChunkDataManager.SectionNBTDataManager {
    private static final int LSB_BYTES_PER_EBS = BLOCKS_PER_EBS;
    private static final int MSB_BYTES_PER_EBS = BLOCKS_PER_EBS / 2;
    private static final int HEADER_SIZE = 2;

    @Override
    public String id() {
        return "blockid";
    }

    @Override
    public int maxPacketSize() {
        return HEADER_SIZE + EBS_PER_CHUNK * (LSB_BYTES_PER_EBS + MSB_BYTES_PER_EBS);
    }

    @Override
    public void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data) {
        val ebsArray = chunk.getBlockStorageArray();
        int currentPos = data.position();
        data.putShort((short) 0);
        int msbMask = 0;
        for (int i = 0; i < ebsArray.length; i++) {
            if ((ebsMask & (1 << i)) != 0) {
                val ebs = ebsArray[i];
                val lsb = ebs.getBlockLSBArray();
                val msb = ebs.getBlockMSBArray();
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
    public void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer) {
        val ebsArray = chunk.getBlockStorageArray();
        val msbMask = buffer.getShort() & 0xFFFF;
        for (int i = 0; i < ebsArray.length; i++) {
            val ebs = ebsArray[i];
            if ((ebsMask & (1 << i)) != 0 && ebs != null) {
                buffer.get(ebs.getBlockLSBArray());
                if ((msbMask & (1 << i)) != 0) {
                    val msb = ebs.getBlockMSBArray();
                    if (msb == null) {
                        ebs.createBlockMSBArray();
                    }
                    buffer.get(ebs.getBlockMSBArray().data);
                } else {
                    ebs.setBlockMSBArray(null);
                }
            }
        }
    }

    @Override
    public boolean sectionPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        section.setByteArray("Blocks", ebs.getBlockLSBArray());

        if (ebs.getBlockMSBArray() != null) {
            section.setByteArray("Add", ebs.getBlockMSBArray().data);
        }
    }

    @Override
    public void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        ebs.setBlockLSBArray(section.getByteArray("Blocks"));

        if (section.hasKey("Add", 7)) {
            ebs.setBlockMSBArray(new NibbleArray(section.getByteArray("Add"), 4));
        }
    }
}
