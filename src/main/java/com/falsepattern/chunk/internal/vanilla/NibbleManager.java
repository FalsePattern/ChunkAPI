package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.internal.Common;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public abstract class NibbleManager extends VanillaManager {
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

    @Override
    public void writeToNBT(Chunk chunk, @NotNull NBTTagCompound tag) {

    }

    @Override
    public void readFromNBT(Chunk chunk, @NotNull NBTTagCompound tag) {

    }
}
