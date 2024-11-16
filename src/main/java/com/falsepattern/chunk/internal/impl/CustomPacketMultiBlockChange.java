package com.falsepattern.chunk.internal.impl;

import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.chunk.Chunk;

public interface CustomPacketMultiBlockChange {
    void chunkapi$init(int count, short[] crammedPositions, Chunk chunk);
    S23PacketBlockChange[] chunkapi$subPackets();
}
