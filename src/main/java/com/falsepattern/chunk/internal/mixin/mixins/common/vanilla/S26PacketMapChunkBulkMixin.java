/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
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
    private int[] ebsMasks;
    @Shadow(aliases = "field_149262_d")
    private int[] ebsMSBMasks;
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
        ebsMasks = new int[chunkCount];
        ebsMSBMasks = new int[chunkCount];
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
            ebsMasks[i] = data.readUnsignedShort();

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
            data.writeShort((short) (ebsMasks[i] & 65535));
        }
    }
}
