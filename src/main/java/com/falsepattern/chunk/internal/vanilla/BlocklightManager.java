/*
 * -------------------------------------------------------------------------------
 * @author FalsePattern
 *
 * Copyright 2023
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 * -------------------------------------------------------------------------------
 */

package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class BlocklightManager extends NibbleManager implements ChunkDataManager.SectionNBTDataManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage ebs) {
        return ebs.getBlocklightArray();
    }

    @Override
    public String id() {
        return "blocklight";
    }

    @Override
    public boolean sectionPrivilegedAccess() {
        return true;
    }

    @Override
    public void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        section.setByteArray("BlockLight", ebs.getBlocklightArray().data);
    }

    @Override
    public void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section) {
        ebs.setBlocklightArray(new NibbleArray(section.getByteArray("BlockLight"), 4));
    }
}
