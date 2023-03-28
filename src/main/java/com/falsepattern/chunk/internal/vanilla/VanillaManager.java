package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

import net.minecraft.world.chunk.Chunk;

public abstract class VanillaManager implements ChunkDataManager<Chunk> {
    @Override
    public String domain() {
        return "minecraft";
    }

    @Override
    public void init(Chunk chunk) {

    }
}
