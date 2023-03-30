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

package com.falsepattern.chunk.api;

import com.falsepattern.lib.StableAPI;
import org.jetbrains.annotations.Contract;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.nio.ByteBuffer;

/**
 * Singleton instances that can manage custom in chunks. This class only manages the registration of
 * managers. For the actual networking and data storage, see the internal interfaces of this interface.
 * Note: This interface does nothing by itself, you also need to implement one or more of the internal interfaces.
 *
 * @since 0.1.0
 * @see ChunkDataManager.PacketDataManager
 * @see ChunkDataManager.ChunkNBTDataManager
 * @see ChunkDataManager.SectionNBTDataManager
 * @see ChunkDataRegistry
 * @author FalsePattern
 * @version 0.1.0
 */
@StableAPI(since = "0.1.0")
public interface ChunkDataManager {

    /**
     * @return The domain of this manager. Usually the modid of the mod that owns this manager.
     */
    @StableAPI.Expose
    @Contract(pure = true)
    String domain();

    /**
     * @return The id of this manager. Usually the name of the manager. Unique per domain.
     */
    @StableAPI.Expose
    @Contract(pure = true)
    String id();

    /**
     * Implement this interface if you want to synchronize your data with the client.
     *
     * @since 0.1.0
     * @author FalsePattern
     * @version 0.1.0
     */
    @StableAPI(since = "0.1.0")
    interface PacketDataManager extends ChunkDataManager {
        /**
         * @return The maximum amount of bytes your data can take up in a packet.
         *
         * @implNote This is used to determine the size of the packet compression/decompression buffer.
         */
        @StableAPI.Expose
        @Contract(pure = true)
        int maxPacketSize();

        /**
         * Serializes your data into a packet.
         *
         * @param chunk The chunk to serialize.
         */
        @StableAPI.Expose
        void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data);

        /**
         * Deserializes your data from a packet.
         * Mutates this object.
         *
         * @param chunk  The chunk to deserialize.
         * @param buffer The packet buffer to read from.
         */
        @StableAPI.Expose
        void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer);
    }

    /**
     * Implement this interface if you want to save your data to disk. This is called once per chunk.
     *
     * @since 0.1.0
     * @author FalsePattern
     * @version 0.1.0
     */
    @StableAPI(since = "0.1.0")
    interface ChunkNBTDataManager extends ChunkDataManager {
        /**
         * If false, the given nbt compound will be a freshly created object that gets inserted into the actual
         * level NBT tag under the `domain:id` name.
         * <p>
         * If true, the nbt tag passed in into the write/read methods will be the raw Level NBT tag without filtering.
         *
         * @implNote This is used internally for reimplementing the vanilla logic. Only change this if you know what you're doing.
         */
        @StableAPI.Expose
        @Contract(pure = true)
        default boolean chunkPrivilegedAccess() {
            return false;
        }

        /**
         * Serializes your data into an NBT tag. This is used when saving the chunk to disk.
         */
        @StableAPI.Expose
        void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt);


        /**
         * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
         * The NBT *may* be null if the chunk was saved before this manager was registered
         * (e.g., loading save before the mod was added), and the manager is not {@link #chunkPrivilegedAccess() privileged}.
         * In this case, you should initialize the data to a sane default.
         */
        @StableAPI.Expose
        void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt);
    }

    /**
     * Implement this interface if you want to save your ExtendedBlockStorage data to disk. This is called once per
     * ExtendedBlockStorage per chunk. (16 times per chunk)
     *
     * @since 0.1.0
     * @author FalsePattern
     * @version 0.1.0
     */
    @StableAPI(since = "0.1.0")
    interface SectionNBTDataManager extends ChunkDataManager {
        /**
         * If false, the given nbt compound will be a freshly created object that gets inserted into the actual
         * segment NBT tag under the `domain:id` name.
         * <p>
         * If true, the nbt tag passed in into the write/read methods will be the raw segment NBT tag without filtering.
         *
         * @implNote This is used internally for reimplementing the vanilla logic. Only change this if you know what you're doing.
         */
        @StableAPI.Expose
        @Contract(pure = true)
        default boolean sectionPrivilegedAccess() {
            return false;
        }

        /**
         * Serializes your data into an NBT tag. This is used when saving the chunk to disk.
         */
        @StableAPI.Expose
        void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section);

        /**
         * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
         * The NBT *may* be null if the chunk was saved before this manager was registered
         * (e.g., loading save before the mod was added), and the manager is not {@link #sectionPrivilegedAccess() privileged}.
         * In this case, you should initialize the data to a sane default.
         */
        @StableAPI.Expose
        void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section);
    }
}
