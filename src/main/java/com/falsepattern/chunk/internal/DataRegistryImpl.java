/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2024 FalsePattern
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice
 * shall be included in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This program comes with additional permissions according to Section 7 of the
 * GNU Affero General Public License. See the full LICENSE file for details.
 */

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.DataManager;
import lombok.Data;
import lombok.val;
import lombok.var;
import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;
import net.minecraftforge.common.util.Constants;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;
import cpw.mods.fml.common.StartupQuery;
import cpw.mods.fml.common.ZipperUtil;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DataRegistryImpl {
    private static final Set<String> managers = new HashSet<>();
    private static final Map<String, PacketManagerInfo> packetManagers = new HashMap<>();
    private static final Map<String, DataManager.StorageDataManager> NBTManagers = new HashMap<>();
    private static final Map<String, DataManager.ChunkDataManager> chunkNBTManagers = new HashMap<>();
    private static final Map<String, DataManager.SubChunkDataManager> subChunkNBTManagers = new HashMap<>();
    private static final Set<String> disabledManagers = new HashSet<>();
    private static int maxPacketSize = 4;

    @Data
    private static class PacketManagerInfo {
        public final int maxPacketSize;
        public final DataManager.PacketDataManager manager;
    }

    public static void registerDataManager(DataManager manager)
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
        if (manager instanceof DataManager.PacketDataManager) {
            val packetManager = (DataManager.PacketDataManager) manager;
            val maxSize = packetManager.maxPacketSize();
            maxPacketSize += 4 + id.getBytes(StandardCharsets.UTF_8).length + 4 + maxSize;
            packetManagers.put(id, new PacketManagerInfo(maxSize, packetManager));
        }
        if (manager instanceof DataManager.StorageDataManager) {
            NBTManagers.put(id, (DataManager.StorageDataManager) manager);
            if (manager instanceof DataManager.ChunkDataManager) {
                chunkNBTManagers.put(id, (DataManager.ChunkDataManager) manager);
            }
            if (manager instanceof DataManager.SubChunkDataManager) {
                subChunkNBTManagers.put(id, (DataManager.SubChunkDataManager) manager);
            }
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
                maxPacketSize -= 4 + id.getBytes(StandardCharsets.UTF_8).length + 4 + removed.maxPacketSize;
            }
            chunkNBTManagers.remove(manager);
            subChunkNBTManagers.remove(manager);
            NBTManagers.remove(manager);
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

    public static void readFromBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int count = buf.getInt();
        for (int i = 0; i < count; i++) {
            val id = readString(buf);
            val length = buf.getInt();
            val managerInfo = packetManagers.get(id);
            if (managerInfo == null) {
                Common.LOG.error("Received data for unknown PacketDataManager " + id + ". Skipping.");
                buf.position(buf.position() + length);
                continue;
            }
            if (length > managerInfo.maxPacketSize) {
                Common.LOG.error("Received packet larger than max size for PacketDataManager " + id + "! Continuing anyways, things might break!");
            }
            int start = buf.position();
            val slice = createSlice(buf, start, length);
            managerInfo.manager.readFromBuffer(chunk, subChunkMask, forceUpdate, slice);
            buf.position(start + length);
        }
    }

    public static int writeToBuffer(Chunk chunk, int subChunkMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(packetManagers.size());
        for (val pair : packetManagers.entrySet()) {
            val id = pair.getKey();
            val managerInfo = pair.getValue();
            writeString(buf, id);
            int start = buf.position() + 4;
            val slice = createSlice(buf, start, managerInfo.maxPacketSize);
            managerInfo.manager.writeToBuffer(chunk, subChunkMask, forceUpdate, slice);
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

    private static NBTTagCompound createManagerNBT(boolean privileged, NBTTagCompound root, DataManager manager) {
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

    private static NBTTagCompound getManagerNBT(boolean privileged, NBTTagCompound root, DataManager manager) {
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

    public static void writeSubChunkToNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        for (val manager : subChunkNBTManagers.values()) {
            manager.writeSubChunkToNBT(chunk, subChunk,
                                       createManagerNBT(manager.subChunkPrivilegedAccess(), nbt, manager));
        }
    }

    public static void readSubChunkFromNBT(Chunk chunk, ExtendedBlockStorage subChunk, NBTTagCompound nbt) {
        for (val manager : subChunkNBTManagers.values()) {
            manager.readSubChunkFromNBT(chunk, subChunk,
                                        getManagerNBT(manager.subChunkPrivilegedAccess(), nbt, manager));
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

    public static void cloneChunk(Chunk from, Chunk to) {
        for (val manager: chunkNBTManagers.values()) {
            manager.cloneChunk(from, to);
        }
    }

    public static void cloneSubChunk(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        for (val manager: subChunkNBTManagers.values()) {
            manager.cloneSubChunk(fromChunk, from, to);
        }
    }

    public static Set<String> getRegisteredManagers() {
        return Collections.unmodifiableSet(managers);
    }

    public static void readLevelDat(NBTTagCompound tag) {
        boolean triggerScreen = false;
        boolean newlyInstalled = false;
        val builder = new StringBuilder();
        if (!tag.hasKey("version")) {
            newlyInstalled = true;
            builder.append("The world you are trying to load is a vanilla world, or a world that was created with a very old version of ChunkAPI.\n");
        } // Compat code will be added here if needed.
        val managerCompat = verifyManagerCompatibility(tag);
        if (managerCompat != null) {
            //Avoid duplicate messages
            if (!newlyInstalled) {
                builder.append("The world you are trying to load was created with a different version of ChunkAPI data managers.\n" +
                               "Data managers are used by other mods to save extra data in chunks, and changes to these data managers can cause worlds to become corrupted.\n");
            }
            triggerScreen = true;
        }
        if (triggerScreen)
            builder.append("ChunkAPI will attempt to load the world, but it is possible that chunks might get corrupted.\n" +
                           "Read the following text carefully, and make sure you understand the risks before continuing.\n\n");
        if (managerCompat != null)
            builder.append(managerCompat);

        if (triggerScreen) {
            builder.append("\nA world backup will be automatically created in your saves directory.\n\n");
            boolean confirmed = StartupQuery.confirm(builder.toString());
            if (!confirmed) StartupQuery.abort();

            try {
                ZipperUtil.backupWorld();
            } catch (IOException e) {
                StartupQuery.notify("The world backup couldn't be created.\n\n"+e);
                StartupQuery.abort();
            }
        }
    }

    private static String verifyManagerCompatibility(NBTTagCompound tag) {
        boolean compatWarning = false;
        StringBuilder builder = new StringBuilder();
        val saveManagers = readManagers(tag);
        val removedManagers = new HashSet<>(saveManagers.keySet());
        removedManagers.removeAll(NBTManagers.keySet());
        if (!removedManagers.isEmpty()) {
            compatWarning = true;
            builder.append("\nThe following data managers are no longer present:\n");
            for (val manager : removedManagers) {
                val saveManager = saveManagers.get(manager);
                builder.append(manager);
                if (saveManager.version != null)
                    builder.append(' ').append(saveManager.version);
                builder.append("\nUninstall information: ");
                if (saveManager.uninstallMessage != null)
                    builder.append(saveManager.getUninstallMessage());
                else
                    builder.append("No uninstall information available.");
                builder.append('\n');
            }
        }
        val addedManagers = NBTManagers.keySet()
                                       .stream()
                                       .filter(manager -> !manager.startsWith("minecraft:"))
                                       .filter(manager -> !saveManagers.containsKey(manager))
                                       .collect(Collectors.toSet());
        if (!addedManagers.isEmpty()) {
            compatWarning = true;
            builder.append("\nThe following data managers have been newly added:\n");
            for (val manager: addedManagers) {
                val addedManager = NBTManagers.get(manager);
                if (addedManager == null)
                    continue;
                builder.append(manager)
                              .append(' ').append(addedManager.version()).append('\n');
                val installMessage = addedManager.newInstallDescription();
                if (installMessage != null)
                    builder.append("Install information: ").append(addedManager.newInstallDescription()).append('\n');
            }
        }
        boolean changePrompt = false;
        for (val savedManager: saveManagers.entrySet()) {
            val managerName = savedManager.getKey();
            if (NBTManagers.containsKey(managerName)) {
                val manager = savedManager.getValue();
                val currentManager = NBTManagers.get(managerName);
                val extraMessage = currentManager.versionChangeMessage(manager.version);
                if (extraMessage != null) {
                    compatWarning = true;
                    if (!changePrompt) {
                        builder.append("\nThe following data managers have changed versions in an incompatible way:\n");
                        changePrompt = true;
                    }
                    builder.append(managerName)
                                  .append(" has changed versions from ")
                                  .append(manager.version)
                                  .append(" to ")
                                  .append(currentManager.version())
                                  .append(".\n");
                    builder.append("Upgrade information: ").append(extraMessage).append('\n');
                }
            }
        }
        if (!compatWarning)
            return null;
        return "The following managers have changed:\n" +
               builder;
    }

    public static void writeLevelDat(NBTTagCompound tag) {
        val managers = new NBTTagCompound();
        tag.setTag("managers", managers);
        tag.setString("version", Tags.MOD_VERSION);
        for (val manager: NBTManagers.entrySet()) {
            val name = manager.getKey();
            if (name.startsWith("minecraft:"))
                continue;
            val value = manager.getValue();
            managers.setTag(name, SaveManagerInfo.fromManager(value).toNBT());
        }
    }

    private static Map<String, SaveManagerInfo> readManagers(NBTTagCompound tag) {
        val managers = new HashMap<String, SaveManagerInfo>();
        if (!tag.hasKey("managers")) return managers;
        if (tag.func_150299_b("managers") != Constants.NBT.TAG_COMPOUND ) return managers;
        val managerTag = tag.getCompoundTag("managers");
        //noinspection unchecked
        for (val key : (Set<String>)managerTag.func_150296_c()) {
            managers.put(key, SaveManagerInfo.fromNBT(managerTag.getCompoundTag(key)));
        }
        return managers;
    }

    @Data
    private static class SaveManagerInfo {
        @Nullable
        public final String version;
        @Nullable
        public final String uninstallMessage;

        public NBTTagCompound toNBT() {
            val tag = new NBTTagCompound();
            if (version != null)
                tag.setString("version", version);
            if (uninstallMessage != null)
                tag.setString("uninstallMessage", uninstallMessage);
            return tag;
        }

        public static SaveManagerInfo fromNBT(NBTTagCompound tag) {
            return new SaveManagerInfo(getNBTStringNullable(tag, "version"), getNBTStringNullable(tag, "uninstallMessage"));
        }

        private static String getNBTStringNullable(NBTTagCompound tag, String key) {
            if (tag.hasKey(key) && tag.func_150299_b(key) == Constants.NBT.TAG_STRING) {
                return tag.getString(key);
            } else {
                return null;
            }
        }

        public static SaveManagerInfo fromManager(DataManager.StorageDataManager manager) {
            return new SaveManagerInfo(manager.version(), manager.uninstallMessage());
        }
    }
}
