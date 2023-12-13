/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.api;

import com.falsepattern.lib.StableAPI;
import lombok.val;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
         * Only called ONCE, during registration!
         */
        @StableAPI.Expose
        @Contract(pure = true)
        int maxPacketSize();

        /**
         * Serializes your data into a packet.
         *
         * @param chunk The chunk to serialize.
         */
        @Contract(mutates = "param4")
        @StableAPI.Expose
        void writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer data);

        /**
         * Deserializes your data from a packet.
         * Mutates this object.
         *
         * @param chunk  The chunk to deserialize.
         * @param buffer The packet buffer to read from.
         */
        @Contract(mutates = "param1,param4")
        @StableAPI.Expose
        void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, ByteBuffer buffer);
    }

    /**
     * The common superinterface for ChunkNBTDataManager and SectionNBTDataManager.
     * Contains version information and messages for users attempting to upgrade/remove versions.
     */
    @StableAPI(since = "0.4.0")
    interface NBTDataManager extends ChunkDataManager {
        /**
         * @return The current version of the data manager
         */
        @StableAPI.Expose
        @Contract(pure = true)
        @NotNull
        String version();

        /**
         * @return The message to show to users when a world is opened with this mod for the first time.
         *         Return null to show no message, and treat the manager as fully compatible with vanilla.
         */
        @StableAPI.Expose
        @Contract(pure = true)
        @Nullable
        String newInstallDescription();

        /**
         * @return The message to show to users when this mod is removed and they try to load the world (stored in the
         * world's NBT during save)
         */
        @StableAPI.Expose
        @Contract(pure = true)
        @NotNull
        String uninstallMessage();

        /**
         * @param priorVersion The version of the manager this world was saved with.
         * @return A warning message to show to the user when upgrading.\
         *         If null, the manager is treated as fully compatible with the old version, and no warning is shown.
         */
        @StableAPI.Expose
        @Contract(pure = true)
        @Nullable
        String versionChangeMessage(String priorVersion);
    }

    /**
     * Implement this interface if you want to save your data to disk. This is called once per chunk.
     *
     * @since 0.1.0
     * @author FalsePattern
     * @version 0.1.0
     */
    @StableAPI(since = "0.1.0")
    interface ChunkNBTDataManager extends NBTDataManager {
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
        @Contract(mutates = "param2")
        @StableAPI.Expose
        void writeChunkToNBT(Chunk chunk, NBTTagCompound nbt);


        /**
         * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
         * The NBT *may* be null if the chunk was saved before this manager was registered
         * (e.g., loading save before the mod was added), and the manager is not {@link #chunkPrivilegedAccess() privileged}.
         * In this case, you should initialize the data to a sane default.
         */
        @Contract(mutates = "param1")
        @StableAPI.Expose
        void readChunkFromNBT(Chunk chunk, NBTTagCompound nbt);

        /**
         * Directly copies data from one chunk to another chunk.
         * @param from The chunk to copy data from.
         * @param to The chunk to copy data to.
         * @implNote The default implementation uses a very inefficient method of serialize->deserialize, so this should be overridden if possible.
         */
        @Contract(mutates = "param2")
        @StableAPI.Expose(since = "0.5.0")
        default void cloneChunk(Chunk from, Chunk to) {
            val tmp = new NBTTagCompound();
            writeChunkToNBT(from, tmp);
            readChunkFromNBT(to, tmp);
        }
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
    interface SectionNBTDataManager extends NBTDataManager {
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
        @Contract(mutates = "param3")
        @StableAPI.Expose
        void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section);

        /**
         * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
         * The NBT *may* be null if the chunk was saved before this manager was registered
         * (e.g., loading save before the mod was added), and the manager is not {@link #sectionPrivilegedAccess() privileged}.
         * In this case, you should initialize the data to a sane default.
         */
        @Contract(mutates = "param2")
        @StableAPI.Expose
        void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound section);

        /**
         * Directly copies data from one section to another section.
         * @param fromChunk The owner of the section to copy data from.
         * @param from The section to copy data from.
         * @param to The section to copy data to.
         * @implNote The default implementation uses a very inefficient method of serialize->deserialize, so this should be overridden if possible.
         */
        @Contract(mutates = "param3")
        @StableAPI.Expose(since = "0.5.0")
        default void cloneSection(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
            val tmp = new NBTTagCompound();
            writeSectionToNBT(fromChunk, from, tmp);
            readSectionFromNBT(fromChunk, to, tmp);
        }
    }
}
