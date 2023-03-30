/*
 * -------------------------------------------------------------------------------
 * @author FalsePattern
 *
 * Copyright 2023
 *
 * All rights reserved.
 * Distribution of the software in any form is only allowed with
 * explicit, prior permission from the owner.
 * -------------------------------------------------------------------------------
 */

package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.ChunkDataRegistryImpl;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

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
    private int ebsMask;
    @Shadow(aliases = "field_149281_e")
    private byte[] deflatedData;
    @Shadow(aliases = "field_149278_f")
    private byte[] data;
    @Shadow(aliases = "field_149279_g")
    private boolean forceUpdate;
    @Shadow(aliases = "field_149285_h")
    private int deflatedSize;

    @ModifyConstant(method = {"<clinit>", "func_149275_c"},
                    constant = @Constant(intValue = 196864),
                    require = 1)
    private static int increasePacketSize(int constant) {
        return ChunkDataRegistryImpl.maxPacketSize();
    }

    /**
     * @author FalsePattern
     * @reason Replace functionality
     */
    @Overwrite
    public static S21PacketChunkData.Extracted func_149269_a(Chunk chunk, boolean forceUpdate, int ebsMask) {
        ExtendedBlockStorage[] ebs = chunk.getBlockStorageArray();
        S21PacketChunkData.Extracted extracted = new S21PacketChunkData.Extracted();

        if (buffer.length < ChunkDataRegistryImpl.maxPacketSize()) {
            buffer = new byte[ChunkDataRegistryImpl.maxPacketSize()];
        }

        if (forceUpdate) {
            chunk.sendUpdates = true;
        }

        for (int i = 0; i < ebs.length; ++i) {
            if (ebs[i] != null && (!forceUpdate || !ebs[i].isEmpty()) && (ebsMask & 1 << i) != 0) {
                extracted.field_150280_b |= 1 << i;
            }
        }

        int length = ChunkDataRegistryImpl.writeToBuffer(chunk, extracted.field_150280_b, forceUpdate, buffer);

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
        System.out.println("Writing chunk packet (" + xPosition + ", " + zPosition + ")");
        data.writeBoolean(forceUpdate);
        data.writeShort((short) (ebsMask & 0xFFFF));
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
        System.out.println("Reading chunk packet (" + xPosition + ", " + zPosition + ")");
        forceUpdate = data.readBoolean();
        ebsMask = data.readShort() & 0xFFFF;
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
