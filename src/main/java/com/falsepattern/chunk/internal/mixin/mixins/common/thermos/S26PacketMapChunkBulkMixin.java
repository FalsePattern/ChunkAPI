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

package com.falsepattern.chunk.internal.mixin.mixins.common.thermos;

import com.falsepattern.chunk.internal.mixin.helpers.LockHelper;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.network.play.server.S26PacketMapChunkBulk;

import java.io.IOException;
import java.util.concurrent.Semaphore;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

@Mixin(S26PacketMapChunkBulk.class)
public abstract class S26PacketMapChunkBulkMixin {
    @Shadow(aliases = "field_149268_i",
            remap = false)
    private byte[] inflaterBuffer;
    @Shadow(aliases = "field_149263_e",
            remap = false)
    private byte[] deflatedData;
    @Shadow(aliases = "field_149266_a",
            remap = false)
    private int[] xPositions;
    @Shadow(aliases = "field_149264_b",
            remap = false)
    private int[] zPositions;
    @Shadow(aliases = "field_149265_c",
            remap = false)
    private int[] subChunkMasks;
    @Shadow(aliases = "field_149262_d",
            remap = false)
    private int[] subChunkMSBMasks;
    @Shadow(aliases = "field_149261_g",
            remap = false)
    private int deflatedSize;
    @Shadow(aliases = "field_149267_h",
            remap = false)
    private boolean skylight;
    @Shadow(aliases = "field_149260_f",
            remap = false)
    private byte[][] datas;

    @Shadow(remap = false)
    protected abstract void compress();

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public void readPacketData(PacketBuffer data) throws IOException {
        short chunkCount = data.readShort();
        val sizes = new int[chunkCount];
        for (int i = 0; i < chunkCount; i++) {
            sizes[i] = data.readInt();
        }
        deflatedSize = data.readInt();
        skylight = data.readBoolean();
        xPositions = new int[chunkCount];
        zPositions = new int[chunkCount];
        subChunkMasks = new int[chunkCount];
        subChunkMSBMasks = new int[chunkCount];
        datas = new byte[chunkCount][];

        while (!LockHelper.bufferLockS26PacketMapChunkBulk.tryLock()) {
            Thread.yield();
        }
        byte[] buf;
        try {
            if (inflaterBuffer.length < deflatedSize) {
                inflaterBuffer = new byte[deflatedSize];
            }

            data.readBytes(inflaterBuffer, 0, deflatedSize);
            buf = new byte[S21PacketChunkData.func_149275_c() * chunkCount];
            Inflater inflater = new Inflater();
            inflater.setInput(inflaterBuffer, 0, deflatedSize);

            try {
                inflater.inflate(buf);
            } catch (DataFormatException dataformatexception) {
                throw new IOException("Bad compressed data format");
            } finally {
                inflater.end();
            }
        } finally {
            LockHelper.bufferLockS26PacketMapChunkBulk.unlock();
        }

        int pos = 0;

        for (int i = 0; i < chunkCount; ++i) {
            val size = sizes[i];
            xPositions[i] = data.readInt();
            zPositions[i] = data.readInt();
            subChunkMasks[i] = data.readUnsignedShort();

            datas[i] = new byte[size];
            System.arraycopy(buf, pos, datas[i], 0, size);
            pos += size;
        }
    }

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public void writePacketData(PacketBuffer data) {
        compress();

        data.writeShort(xPositions.length);
        for (byte[] bytes : datas) {
            data.writeInt(bytes.length);
        }
        data.writeInt(deflatedSize);
        data.writeBoolean(skylight);
        data.writeBytes(deflatedData, 0, deflatedSize);

        for (int i = 0; i < xPositions.length; ++i) {
            data.writeInt(xPositions[i]);
            data.writeInt(zPositions[i]);
            data.writeShort((short) (subChunkMasks[i] & 65535));
        }
    }
}
