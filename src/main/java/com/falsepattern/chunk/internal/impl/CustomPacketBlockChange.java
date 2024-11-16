package com.falsepattern.chunk.internal.impl;

import net.minecraft.world.chunk.Chunk;

public interface CustomPacketBlockChange {
    void chunkapi$init(int x, int y, int z, Chunk chunk);
    void chunkapi$x(int value);
    void chunkapi$y(int value);
    void chunkapi$z(int value);
    int chunkapi$x();
    int chunkapi$y();
    int chunkapi$z();
}
