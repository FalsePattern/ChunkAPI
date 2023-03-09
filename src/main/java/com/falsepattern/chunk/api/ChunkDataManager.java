package com.falsepattern.chunk.api;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.world.chunk.Chunk;

/**
 * Singleton instances that can manage custom in chunks.
 * Note: All the methods should be thread-safe and stateless unless otherwise specified.
 */
public interface ChunkDataManager {
    /**
     * The unique identifier of your data. The common convention is <code>modid:dataid</code>.
     * @return The unique identifier of your data. Should be unique across all mods.
     * @apiNote Ideally, this should be an interned string for maximum performance. (see {@link String#intern()}).
     * @implNote This is used to identify your data in packets and NBT tags.
     */
    String id();
    /**
     * Initializes your data in a chunk. This is called when the chunk is first created.
     * @param chunk The chunk to initialize.
     */
    void init(ModdedChunk chunk);
    /**
     * The amount of bytes your data will take up in a packet.
     * @param chunk The chunk that the data is in.
     * @return The amount of bytes your data will take up in a packet. -1 if the data is not present.
     */
    int getPacketSize(ModdedChunk chunk);
    /**
     * Serializes your data into a packet. Not called if {@link #getPacketSize(ModdedChunk)} returns -1.
     * @param chunk The chunk that the data is in.
     * @param buffer The packet buffer to write to.
     */
    void writeToPacket(ModdedChunk chunk, PacketBuffer buffer);
    /**
     * Deserializes your data from a packet. Not called if {@link #getPacketSize(ModdedChunk)} returned -1 on the other end.
     * @param chunk The chunk that the data is in.
     * @param buffer The packet buffer to read from.
     * @return The deserialized data.
     */
    void readFromPacket(ModdedChunk chunk, PacketBuffer buffer);
    /**
     * Serializes your data into an NBT tag. This is used when saving the chunk to disk.
     * @param chunk The chunk that the data is in.
     * @param tag The tag to write to.
     */
    void writeToNBT(ModdedChunk chunk, NBTTagCompound tag);
    /**
     * Deserializes your data from an NBT tag. This is used when loading the chunk from disk.
     * @param chunk The chunk that the data gets loaded into.
     * @param tag The tag to read from.
     */
    void readFromNBT(ModdedChunk chunk, NBTTagCompound tag);
}
