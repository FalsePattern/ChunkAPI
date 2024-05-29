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

    @Shadow(aliases = "field_149286_i")
    private static byte[] buffer;
    @Shadow
    private Semaphore deflateGate;
    @Shadow(aliases = "field_149284_a")
    private int xPosition;
    @Shadow(aliases = "field_149282_b")
    private int zPosition;
    @Shadow(aliases = "field_149283_c")
    private int subChunkMask;
    @Shadow(aliases = "field_149281_e")
    private byte[] deflatedData;
    @Shadow(aliases = "field_149278_f")
    private byte[] data;
    @Shadow(aliases = "field_149279_g")
    private boolean forceUpdate;
    @Shadow(aliases = "field_149285_h")
    private int deflatedSize;


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

    @Shadow
    protected abstract void deflate();

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
