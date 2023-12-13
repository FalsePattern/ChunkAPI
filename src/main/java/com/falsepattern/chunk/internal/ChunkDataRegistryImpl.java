/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataManager;
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

public class ChunkDataRegistryImpl {
    private static final Set<String> managers = new HashSet<>();
    private static final Map<String, PacketManagerInfo> packetManagers = new HashMap<>();
    private static final Map<String, ChunkDataManager.NBTDataManager> NBTManagers = new HashMap<>();
    private static final Map<String, ChunkDataManager.ChunkNBTDataManager> chunkNBTManagers = new HashMap<>();
    private static final Map<String, ChunkDataManager.SectionNBTDataManager> sectionNBTManagers = new HashMap<>();
    private static final Set<String> disabledManagers = new HashSet<>();
    private static int maxPacketSize = 4;

    @Data
    private static class PacketManagerInfo {
        public final int maxPacketSize;
        public final ChunkDataManager.PacketDataManager manager;
    }

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
            val maxSize = packetManager.maxPacketSize();
            maxPacketSize += 4 + id.getBytes(StandardCharsets.UTF_8).length + 4 + maxSize;
            packetManagers.put(id, new PacketManagerInfo(maxSize, packetManager));
        }
        if (manager instanceof ChunkDataManager.NBTDataManager) {
            NBTManagers.put(id, (ChunkDataManager.NBTDataManager) manager);
            if (manager instanceof ChunkDataManager.ChunkNBTDataManager) {
                chunkNBTManagers.put(id, (ChunkDataManager.ChunkNBTDataManager) manager);
            }
            if (manager instanceof ChunkDataManager.SectionNBTDataManager) {
                sectionNBTManagers.put(id, (ChunkDataManager.SectionNBTDataManager) manager);
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
            sectionNBTManagers.remove(manager);
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

    public static void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
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
            managerInfo.manager.readFromBuffer(chunk, ebsMask, forceUpdate, slice);
            buf.position(start + length);
        }
    }

    public static int writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(packetManagers.size());
        for (val pair : packetManagers.entrySet()) {
            val id = pair.getKey();
            val managerInfo = pair.getValue();
            writeString(buf, id);
            int start = buf.position() + 4;
            val slice = createSlice(buf, start, managerInfo.maxPacketSize);
            managerInfo.manager.writeToBuffer(chunk, ebsMask, forceUpdate, slice);
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

    public static void cloneChunk(Chunk from, Chunk to) {
        for (val manager: chunkNBTManagers.values()) {
            manager.cloneChunk(from, to);
        }
    }

    public static void cloneSection(Chunk fromChunk, ExtendedBlockStorage from, ExtendedBlockStorage to) {
        for (val manager: sectionNBTManagers.values()) {
            manager.cloneSection(fromChunk, from, to);
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
        tag.setString("version", Tags.VERSION);
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

        public static SaveManagerInfo fromManager(ChunkDataManager.NBTDataManager manager) {
            return new SaveManagerInfo(manager.version(), manager.uninstallMessage());
        }
    }
}
