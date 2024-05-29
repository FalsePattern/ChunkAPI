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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * -------------------------------------------------------------------------------
 * According to section 7 of the GNU AGPL, this software comes with the following additional permissions:
 *
 * You may link this mod against proprietary code developed by Mojang Studios (Minecraft and its dependencies), as well
 * as distribute binary builds of it as part of a "Minecraft modpack" without the AGPL restrictions spreading to any other
 * files in said modpack.
 *
 * You may also develop any mods that depend on classes the API packages (`com.falsepattern.chunk.api.*`) and distribute that
 * mod under terms of your own choice. Note that this permissions does not apply to anything outside the internal package,
 * as those are internal implementations of ChunkAPI and are not meant to be used in any external mod, thus the full force
 * of the AGPL applies in such cases.
 *
 * These additional permissions get removed if you modify, extend, rename, remove, or in any other way alter
 * any part of the public API code, save data binary formats, or the network protocol in such a way that makes
 * it not perfectly compatible with the official versions of ChunkAPI.
 *
 * If you wish to make modifications to the previously mentioned features, please create a pull request on the official
 * release of ChunkAPI with full reasoning and specifications behind the requested change, and once it's merged and
 * published in an official release, your modified version may once again inherit this additional permission.
 *
 * To avoid abuse caused by upstream API changes, these permissions are valid as long as your modified version of ChunkAPI
 * is perfectly compatible with any public release of the official ChunkAPI.
 */

package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

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
    @Shadow(aliases = "field_149268_i")
    private static byte[] inflaterBuffer;
    @Shadow(aliases = "field_149263_e")
    private byte[] deflatedData;
    @Shadow
    private Semaphore deflateGate;
    @Shadow(aliases = "field_149266_a")
    private int[] xPositions;
    @Shadow(aliases = "field_149264_b")
    private int[] zPositions;
    @Shadow(aliases = "field_149265_c")
    private int[] subChunkMasks;
    @Shadow(aliases = "field_149262_d")
    private int[] subChunkMSBMasks;
    @Shadow(aliases = "field_149261_g")
    private int deflatedSize;
    @Shadow(aliases = "field_149267_h")
    private boolean skylight;
    @Shadow(aliases = "field_149260_f")
    private byte[][] datas;

    @Shadow
    protected abstract void deflate();

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

        if (inflaterBuffer.length < deflatedSize) {
            inflaterBuffer = new byte[deflatedSize];
        }

        data.readBytes(inflaterBuffer, 0, deflatedSize);
        byte[] buf = new byte[S21PacketChunkData.func_149275_c() * chunkCount];
        Inflater inflater = new Inflater();
        inflater.setInput(inflaterBuffer, 0, deflatedSize);

        try {
            inflater.inflate(buf);
        } catch (DataFormatException dataformatexception) {
            throw new IOException("Bad compressed data format");
        } finally {
            inflater.end();
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
        if (deflatedData == null) {
            deflateGate.acquireUninterruptibly();
            if (deflatedData == null) {
                deflate();
            }
            deflateGate.release();
        }

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
