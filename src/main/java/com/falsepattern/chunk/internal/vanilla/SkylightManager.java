package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

public class SkylightManager extends NibbleManager implements ChunkDataManager.SectionNBTDataManager {
    @Override
    public String id() {
        return "skylight";
    }

    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage ebs) {
        return ebs.getSkylightArray();
    }

    @Override
    public void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.writeToBuffer(chunk, ebsMask, forceUpdate, data);
        }
    }

    @Override
    public void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer) {
        if (!chunk.worldObj.provider.hasNoSky) {
            super.readFromBuffer(chunk, ebsMask, forceUpdate, buffer);
        }
    }

    @Override
    public boolean sectionPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        if (!chunk.worldObj.provider.hasNoSky) {
            section.setByteArray("SkyLight", ebs.getSkylightArray().data);
        } else {
            section.setByteArray("SkyLight", new byte[ebs.getBlocklightArray().data.length]);
        }
    }

    @Override
    public void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        if (!chunk.worldObj.provider.hasNoSky) {
            ebs.setSkylightArray(new NibbleArray(section.getByteArray("SkyLight"), 4));
        }
    }
}
