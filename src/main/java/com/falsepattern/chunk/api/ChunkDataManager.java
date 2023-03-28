package com.falsepattern.chunk.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.nbt.NBTTagCompound;

import java.nio.ByteBuffer;

/**
 * Singleton instances that can manage custom in chunks.
 * For performance reasons, you should store any data in the chunk itself using a mixin. The type T is the type
 * of the interface that you are mixing into the chunk.
 * Note: All the methods should be thread-safe and stateless unless otherwise specified.
 */
public interface ChunkDataManager<T> {

    /**
     * @return The domain of this manager. Usually the modid of the mod that owns this manager.
     */
    String domain();

    /**
     * @return The id of this manager. Usually the name of the manager. Unique per domain.
     */
    String id();

    /**
     * Used for sorting the managers. Managers with a lower order index are processed first.
     * If unsure, return 0, and let the implementation decide.
     */
    default int orderIndex() {
        return 0;
    }

    /**
     * Initializes your data in a chunk. This is called at the end of the chunk constructor. One less mixin per manager!
     * @param chunk The chunk to initialize.
     */
    void init(T chunk);

    /**
     * @implNote This is used to determine the size of the packet compression/decompression buffer.
     * @return The maximum amount of bytes your data can take up in a packet.
     */
    int maxPacketSize();

    /**
     * Serializes your data into a packet.
     *
     * @param chunk The chunk to serialize.
     */
    void writeToBuffer(T chunk, int ebsMask, boolean forceUpdate, ByteBuffer data);

    /**
     * Deserializes your data from a packet.
     * Mutates this object.
     *
     * @param chunk  The chunk to deserialize.
     * @param buffer The packet buffer to read from.
     */
    void readFromBuffer(T chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer);

    /**
     * Serializes your data into an NBT tag. This is used when saving the chunk to disk.
     * @param tag The tag to write to.
     */
    void writeToNBT(T chunk, @NotNull NBTTagCompound tag);

    /**
     * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
     * Mutates this object.
     * @param tag The tag to read from.
     */
    void readFromNBT(T chunk, @NotNull NBTTagCompound tag);
}
