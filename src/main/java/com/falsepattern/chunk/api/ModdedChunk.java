package com.falsepattern.chunk.api;

/**
 * This interface is injected into {@link net.minecraft.world.chunk.Chunk} by ChunkAPI. You can cast any Chunk instance
 * to this to access the API methods.
 */
public interface ModdedChunk {
    /**
     * Gets the data stored in this chunk by the specified manager.
     * @param manager The manager to get the data from.
     * @return The data stored in this chunk by the specified manager.
     * @param <T> The type of the data.
     */
    <T> T getData(ChunkDataManager manager);

    /**
     * Sets the data stored in this chunk by the specified manager.
     * @param manager The manager to set the data for.
     * @param data The data to store in this chunk.
     * @param <T> The type of the data.
     */
    <T> void setData(ChunkDataManager manager, T data);

    /**
     * Checks if this chunk has data stored by the specified manager.
     * @param manager The manager to check for.
     * @return True if this chunk has data stored by the specified manager.
     */
    boolean hasData(ChunkDataManager manager);

    /**
     * Removes the data stored in this chunk by the specified manager.
     * @param manager The manager to remove the data from.
     * @return The data that was stored in this chunk by the specified manager.
     * @param <T> The type of the data.
     */
    <T> T removeData(ChunkDataManager manager);
}
