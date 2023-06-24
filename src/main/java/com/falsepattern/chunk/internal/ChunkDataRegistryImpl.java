/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataManager;
import lombok.val;
import lombok.var;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkDataRegistryImpl {
    private static final Set<String> managers = new HashSet<>();
    private static final Map<String, ChunkDataManager.PacketDataManager> packetManagers = new HashMap<>();
    private static final Map<String, ChunkDataManager.ChunkNBTDataManager> chunkNBTManagers = new HashMap<>();
    private static final Map<String, ChunkDataManager.SectionNBTDataManager> sectionNBTManagers = new HashMap<>();
    private static final Set<String> disabledManagers = new HashSet<>();
    private static int maxPacketSize = 4;

    public static void registerDataManager(ChunkDataManager manager)
            throws IllegalStateException, IllegalArgumentException {
        if (Loader.instance().getLoaderState() != LoaderState.INITIALIZATION) {
            throw new IllegalStateException("ChunkDataManager registration is not allowed at this time! " +
                                            "Please register your ChunkDataManager in the init phase.");
        }
        var id = manager.domain() + ":" + manager.id();
        if (managers.contains(id)) {
            throw new IllegalArgumentException("ChunkDataManager " + manager + " has a duplicate id!");
        }

        // If the manager was disabled, do not add it to the list of managers.
        if (disabledManagers.contains(id)) {
            return;
        }

        managers.add(id);
        if (manager instanceof ChunkDataManager.PacketDataManager) {
            val packetManager = (ChunkDataManager.PacketDataManager) manager;
            maxPacketSize += 4 + id.getBytes(StandardCharsets.UTF_8).length;
            maxPacketSize += packetManager.maxPacketSize();
            packetManagers.put(id, packetManager);
        }
        if (manager instanceof ChunkDataManager.ChunkNBTDataManager) {
            chunkNBTManagers.put(id, (ChunkDataManager.ChunkNBTDataManager) manager);
        }
        if (manager instanceof ChunkDataManager.SectionNBTDataManager) {
            sectionNBTManagers.put(id, (ChunkDataManager.SectionNBTDataManager) manager);
        }
    }

    public static void disableDataManager(String domain, String id) {
        if (Loader.instance().getLoaderState() != LoaderState.POSTINITIALIZATION) {
            throw new IllegalStateException("ChunkDataManager disabling is not allowed at this time! " +
                                            "Please disable any ChunkDataManagers in the postInit phase.");
        }
        Common.LOG.debug("Disabling ChunkDataManager " + id + " in domain " + domain +
                        ". See the stacktrace for the source of this event.\nThis is NOT an error.", new Throwable());
        val manager = domain + ":" + id;
        //Remove the manager from the list of managers, if it exists
        if (managers.remove(manager)) {
            //Clear the maps
            if (packetManagers.containsKey(manager)) {
                val removed = packetManagers.remove(manager);
                maxPacketSize -= 4 + manager.getBytes(StandardCharsets.UTF_8).length;
                maxPacketSize -= removed.maxPacketSize();
            }
            chunkNBTManagers.remove(manager);
            sectionNBTManagers.remove(manager);
        }

        //Add the manager to the list of disabled managers, in case it gets registered after this disable call.
        disabledManagers.add(manager);
    }

    public static int maxPacketSize() {
        return maxPacketSize;
    }

    private static void writeString(ByteBuffer buffer, String string) {
        val bytes = string.getBytes();
        buffer.putInt(bytes.length);
        buffer.put(bytes);
    }

    private static String readString(ByteBuffer buffer) {
        val length = buffer.getInt();
        val bytes = new byte[length];
        buffer.get(bytes);
        return new String(bytes);
    }

    public static void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int count = buf.getInt();
        for (int i = 0; i < count; i++) {
            val id = readString(buf);
            val length = buf.getInt();
            val manager = packetManagers.get(id);
            if (manager == null) {
                Common.LOG.error("Received data for unknown PacketDataManager " + id + ". Skipping.");
                buf.position(buf.position() + length);
                continue;
            }
            int start = buf.position();
            val slice = createSlice(buf, start, length);
            manager.readFromBuffer(chunk, ebsMask, forceUpdate, slice);
            buf.position(start + length);
        }
    }

    public static int writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(packetManagers.size());
        for (val pair : packetManagers.entrySet()) {
            val id = pair.getKey();
            val manager = pair.getValue();
            writeString(buf, id);
            int start = buf.position() + 4;
            val slice = createSlice(buf, start, manager.maxPacketSize());
            manager.writeToBuffer(chunk, ebsMask, forceUpdate, slice);
            int length = slice.position();
            buf.putInt(length);
            buf.position(start + length);
        }
        return buf.position();
    }

    private static ByteBuffer createSlice(ByteBuffer buffer, int start, int length) {
        int oldLimit = buffer.limit();
        int oldPosition = buffer.position();
        buffer.position(start);
        buffer.limit(start + length);
        ByteBuffer slice = buffer.slice();
        buffer.limit(oldLimit);
        buffer.position(oldPosition);
        return slice;
    }

    private static NBTTagCompound createManagerNBT(boolean privileged, NBTTagCompound root, ChunkDataManager manager) {
        if (privileged) {
            return root;
        }
        val domain = manager.domain();
        val id = manager.id();
        NBTTagCompound domainNBT;
        if (root.hasKey(domain)) {
            domainNBT = root.getCompoundTag(domain);
        } else {
            domainNBT = new NBTTagCompound();
            root.setTag(domain, domainNBT);
        }
        if (domainNBT.hasKey(id)) {
            return domainNBT.getCompoundTag(id);
        } else {
            val subNBT = new NBTTagCompound();
            domainNBT.setTag(id, subNBT);
            return subNBT;
        }
    }

    private static NBTTagCompound getManagerNBT(boolean privileged, NBTTagCompound root, ChunkDataManager manager) {
        if (privileged) {
            return root;
        }
        val domain = manager.domain();
        if (!root.hasKey(domain)) {
            return new NBTTagCompound();
        }
        val domainNBT = root.getCompoundTag(domain);
        if (!domainNBT.hasKey(manager.id())) {
            return new NBTTagCompound();
        }
        return domainNBT.getCompoundTag(manager.id());
    }

    public static void writeSectionToNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound sectionNBT) {
        for (val manager : sectionNBTManagers.values()) {
            manager.writeSectionToNBT(chunk, ebs,
                                      createManagerNBT(manager.sectionPrivilegedAccess(), sectionNBT, manager));
        }
    }

    public static void readSectionFromNBT(Chunk chunk, ExtendedBlockStorage ebs, NBTTagCompound sectionNBT) {
        for (val manager : sectionNBTManagers.values()) {
            manager.readSectionFromNBT(chunk, ebs,
                                       getManagerNBT(manager.sectionPrivilegedAccess(), sectionNBT, manager));
        }
    }

    public static void writeChunkToNBT(Chunk chunk, NBTTagCompound chunkNBT) {
        for (val manager : chunkNBTManagers.values()) {
            manager.writeChunkToNBT(chunk, createManagerNBT(manager.chunkPrivilegedAccess(), chunkNBT, manager));
        }
    }

    public static void readChunkFromNBT(Chunk chunk, NBTTagCompound chunkNBT) {
        for (val manager : chunkNBTManagers.values()) {
            manager.readChunkFromNBT(chunk, getManagerNBT(manager.chunkPrivilegedAccess(), chunkNBT, manager));
        }
    }

    public static Set<String> getRegisteredManagers() {
        return Collections.unmodifiableSet(managers);
    }
}
