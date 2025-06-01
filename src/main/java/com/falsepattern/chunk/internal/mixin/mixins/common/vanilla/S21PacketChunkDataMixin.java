/*
 * ChunkAPI
 *
 * Copyright (C) 2023-2025 FalsePattern, The MEGA Team, LegacyModdingMC contributors
 * All Rights Reserved
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.ExtendedBlockStorage;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Mixin(S21PacketChunkData.class)
public abstract class S21PacketChunkDataMixin {

    @Shadow(aliases = "field_149286_i",
            remap = false)
    private static byte[] buffer;
    @Shadow(remap = false)
    private Semaphore deflateGate;
    @Shadow(aliases = "field_149284_a",
            remap = false)
    private int xPosition;
    @Shadow(aliases = "field_149282_b",
            remap = false)
    private int zPosition;
    @Shadow(aliases = "field_149283_c",
            remap = false)
    private int subChunkMask;
    @Shadow(aliases = "field_149281_e",
            remap = false)
    private byte[] deflatedData;
    @Shadow(aliases = "field_149278_f",
            remap = false)
    private byte[] data;
    @Shadow(aliases = "field_149279_g",
            remap = false)
    private boolean forceUpdate;
    @Shadow(aliases = "field_149285_h",
            remap = false)
    private int deflatedSize;

    @Shadow(remap = false)
    protected abstract void deflate();


    /**
     * @author FalsePattern
     * @reason Extend capacity
     */
    @Overwrite
    public static int func_149275_c() {
        return DataRegistryImpl.maxPacketSize();
    }

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public static S21PacketChunkData.Extracted func_149269_a(Chunk chunk, boolean forceUpdate, int subChunkMask) {
        ExtendedBlockStorage[] subChunks = chunk.getBlockStorageArray();
        S21PacketChunkData.Extracted extracted = new S21PacketChunkData.Extracted();

        if (buffer.length < DataRegistryImpl.maxPacketSize()) {
            buffer = new byte[DataRegistryImpl.maxPacketSize()];
        }

        if (forceUpdate) {
            chunk.sendUpdates = true;
        }

        for (int i = 0; i < subChunks.length; ++i) {
            if (subChunks[i] != null && (!forceUpdate || !subChunks[i].isEmpty()) && (subChunkMask & 1 << i) != 0) {
                extracted.field_150280_b |= 1 << i;
            }
        }

        int length = DataRegistryImpl.writeToBuffer(chunk, extracted.field_150280_b, forceUpdate, buffer);

        extracted.field_150282_a = new byte[length];
        System.arraycopy(buffer, 0, extracted.field_150282_a, 0, length);
        return extracted;
    }

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public void writePacketData(PacketBuffer data) {
        if (deflatedData == null) {
            deflateGate.acquireUninterruptibly();
            try {
                if (deflatedData == null) {
                    deflate();
                }
            } finally {
                deflateGate.release();
            }
        }
        data.writeInt(xPosition);
        data.writeInt(zPosition);
        data.writeBoolean(forceUpdate);
        data.writeShort((short) (subChunkMask & 0xFFFF));
        data.writeInt(this.data.length);
        data.writeInt(deflatedSize);
        data.writeBytes(deflatedData, 0, deflatedSize);
    }

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public void readPacketData(PacketBuffer data) throws IOException {
        xPosition = data.readInt();
        zPosition = data.readInt();
        forceUpdate = data.readBoolean();
        subChunkMask = data.readShort() & 0xFFFF;
        this.data = new byte[data.readInt()];
        deflatedSize = data.readInt();
        if (buffer.length < deflatedSize) {
            buffer = new byte[deflatedSize];
        }
        data.readBytes(buffer, 0, deflatedSize);
        val inflater = new Inflater();
        inflater.setInput(buffer, 0, deflatedSize);
        try {
            inflater.inflate(this.data);
        } catch (DataFormatException dfe) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
        }
    }
}
