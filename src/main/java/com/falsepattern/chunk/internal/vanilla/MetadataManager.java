package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class MetadataManager extends NibbleManager implements ChunkDataManager.SectionNBTDataManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage ebs) {
        return ebs.getMetadataArray();
    }

    @Override
    public String id() {
        return "metadata";
    }

    @Override
    public boolean sectionPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        section.setByteArray("Data", ebs.getMetadataArray().data);
    }

    @Override
    public void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        ebs.setBlockMetadataArray(new NibbleArray(section.getByteArray("Data"), 4));
    }
}
