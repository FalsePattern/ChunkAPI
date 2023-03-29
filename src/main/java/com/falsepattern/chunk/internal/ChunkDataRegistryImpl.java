package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.ChunkDataManager;
import lombok.val;
import lombok.var;

import net.minecraft.world.chunk.Chunk;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChunkDataRegistryImpl {
    @SuppressWarnings("rawtypes")
    private static final Map<String, ChunkDataManager> managers = new HashMap<>();
    private static final Set<String> disabledManagers = new HashSet<>();
    private static int maxPacketSize = 0;
    public static void registerDataManager(ChunkDataManager<?> manager) throws IllegalStateException, IllegalArgumentException {
        if (!ChunkAPI.isRegistrationStage()) {
            throw new IllegalStateException("ChunkDataManager registration is not allowed at this time! " +
                                                   "Please register your ChunkDataManager in the preInit phase of your mod.");
        }
        var id = manager.domain() + ":" + manager.id();
        if (managers.containsKey(id)) {
            throw new IllegalArgumentException("ChunkDataManager " + manager + " has a duplicate id!");
        }

        // If the manager was disabled, do not add it to the list of managers.
        if (disabledManagers.contains(id)) {
            return;
        }

        managers.put(id, manager);
        maxPacketSize += 4 + id.getBytes(StandardCharsets.UTF_8).length;
        maxPacketSize += manager.maxPacketSize();
    }

    public static void disableDataManager(String domain, String id) {
        if (!ChunkAPI.isDisableStage()) {
            throw new IllegalStateException("ChunkDataManager disabling is not allowed at this time! " +
                                            "Please disable any ChunkDataManagers in the preInit/init phases.");
        }
        id = domain + ":" + id;
        Common.LOG.warn("Disabling ChunkDataManager " + id + ". See the stacktrace for the source of this event.", new Throwable());
        //Remove the manager from the list of managers, if it exists
        val removed = managers.remove(id);
        if (removed != null) {
            maxPacketSize -= 4 + id.getBytes(StandardCharsets.UTF_8).length;
            maxPacketSize -= removed.maxPacketSize();
        }

        //Add the manager to the list of disabled managers, in case it gets registered after this disable call.
        disabledManagers.add(id);
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

    @SuppressWarnings("unchecked")
    public static void readFromBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        int count = buf.getInt();
        for (int i = 0; i < count; i++) {
            val id = readString(buf);
            val length = buf.getInt();
            val manager = managers.get(id);
            if (manager == null) {
                Common.LOG.error("Received data for unknown ChunkDataManager " + id + ". Skipping.");
                buf.position(buf.position() + length);
                continue;
            }
            int start = buf.position();
            val slice = createSlice(buf, start, length);
            manager.readFromBuffer(chunk, ebsMask, forceUpdate, slice);
            buf.position(start + length);
        }
    }

    @SuppressWarnings("unchecked")
    public static int writeToBuffer(Chunk chunk, int ebsMask, boolean forceUpdate, byte[] data) {
        val buf = ByteBuffer.wrap(data);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        buf.putInt(managers.size());
        for (val pair: managers.entrySet()) {
            val manager = pair.getValue();
            writeString(buf, pair.getKey());
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
}
