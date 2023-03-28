package com.falsepattern.chunk.internal.vanilla;

import net.minecraft.world.chunk.NibbleArray;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

public class MetadataManager extends NibbleManager {
    @Override
    protected NibbleArray getNibbleArray(ExtendedBlockStorage ebs) {
        return ebs.getMetadataArray();
    }

    @Override
    public String id() {
        return "metadata";
    }
}
