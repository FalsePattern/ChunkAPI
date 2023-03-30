package com.falsepattern.chunk.internal.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;

public abstract class VanillaManager implements ChunkDataManager {
    @Override
    public String domain() {
        return "minecraft";
    }
}
