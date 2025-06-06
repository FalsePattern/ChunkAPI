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

package com.falsepattern.chunk.internal.mixin.mixins.common.lookingglass;

import com.xcompwiz.lookingglass.log.LoggerUtils;
import com.xcompwiz.lookingglass.network.LookingGlassPacketManager;
import com.xcompwiz.lookingglass.network.packet.PacketChunkInfo;
import com.xcompwiz.lookingglass.network.packet.PacketHandlerBase;
import com.xcompwiz.lookingglass.network.packet.PacketRequestChunk;
import io.netty.buffer.ByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.S21PacketChunkData;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.network.internal.FMLProxyPacket;

import java.util.concurrent.Semaphore;

@Mixin(PacketChunkInfo.class)
public abstract class PacketChunkInfoMixin extends PacketHandlerBase {
    @Shadow(remap = false) private static Semaphore deflateGate;

    @Shadow(remap = false)
    private static int deflate(byte[] chunkData, byte[] compressedChunkData) {
        return 0;
    }

    @Shadow(remap = false) public abstract void handle(EntityPlayer player, byte[] chunkData, int dim, int xPos, int zPos, boolean reqinit, short yPos, short yMSBPos);

    @Shadow(remap = false) protected abstract byte[] inflateChunkData(ByteBuf in, int compressedsize, int uncompressedsize);

    /**
     * @author FalsePattern
     * @reason This class was just copied vanilla code.
     */
    @Overwrite(remap = false)
    public static S21PacketChunkData.Extracted getMapChunkData(Chunk chunk, boolean forceUpdate, int subChunkMask) {
        return S21PacketChunkData.func_149269_a(chunk, forceUpdate, subChunkMask);
    }

    /**
     * @author FalsePattern
     * @reason Wired up to ChunkAPI
     */
    @Overwrite(remap = false)
    public static FMLProxyPacket createPacket(Chunk chunk, boolean forceUpdate, int subChunkMask, int dim) {
        int xPos = chunk.xPosition;
        int zPos = chunk.zPosition;
        S21PacketChunkData.Extracted extracted = getMapChunkData(chunk, forceUpdate, subChunkMask);
        int realSubChunkMask = extracted.field_150280_b;
        byte[] chunkData = extracted.field_150282_a;
        deflateGate.acquireUninterruptibly();
        byte[] compressedChunkData = new byte[chunkData.length];
        int len = deflate(chunkData, compressedChunkData);
        deflateGate.release();
        ByteBuf data = PacketHandlerBase.createDataBuffer(PacketChunkInfo.class);
        data.writeInt(dim);
        data.writeInt(xPos);
        data.writeInt(zPos);
        data.writeBoolean(forceUpdate);
        data.writeShort((short)(realSubChunkMask & 0xFFFF));
        data.writeInt(len);
        data.writeInt(chunkData.length);
        data.ensureWritable(len);
        data.writeBytes(compressedChunkData, 0, len);
        return buildPacket(data);
    }

    /**
     * @author FalsePattern
     * @reason Wired up to ChunkAPI
     */
    @Overwrite(remap = false)
    public void handle(ByteBuf in, EntityPlayer player) {
        int dim = in.readInt();
        int xPos = in.readInt();
        int zPos = in.readInt();
        boolean forceUpdate = in.readBoolean();
        int subChunkMask = in.readShort() & 0xFFFF;
        int compressedSize = in.readInt();
        int uncompressedSize = in.readInt();
        byte[] chunkData = this.inflateChunkData(in, compressedSize, uncompressedSize);
        if (chunkData == null) {
            LookingGlassPacketManager.bus.sendToServer(PacketRequestChunk.createPacket(xPos, subChunkMask, zPos, dim));
            LoggerUtils.error("Chunk decompression failed: \t%d\t\t%d : %d\n", subChunkMask, compressedSize, uncompressedSize);
        } else {
            this.handle(player, chunkData, dim, xPos, zPos, forceUpdate, (short) subChunkMask, (short)0);
        }
    }
}
