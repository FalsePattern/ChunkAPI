package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import com.falsepattern.chunk.internal.impl.CustomPacketBlockChange;
import com.falsepattern.chunk.internal.impl.CustomPacketMultiBlockChange;
import io.netty.buffer.AbstractByteBuf;
import io.netty.buffer.Unpooled;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.chunk.Chunk;
import cpw.mods.fml.common.network.ByteBufUtils;

import java.io.IOException;

@Mixin(S22PacketMultiBlockChange.class)
public abstract class S22PacketMultiBlockChangeMixin implements CustomPacketMultiBlockChange {
    @Shadow(aliases = "field_148925_b")
    private ChunkCoordIntPair coord;

    private S23PacketBlockChange[] subPackets;

    @SuppressWarnings("DefaultAnnotationParam")
    @Inject(method = "<init>(I[SLnet/minecraft/world/chunk/Chunk;)V",
            at = @At(value = "FIELD",
                     target = "Lnet/minecraft/network/play/server/S22PacketMultiBlockChange;field_148925_b:Lnet/minecraft/world/ChunkCoordIntPair;",
                     unsafe = true),
            require = 1)
    private void suppressConstructor(int p_i45181_1_, short[] crammedPositions, Chunk chunk, CallbackInfo ci) {
        throw new IllegalStateException(
                "S22PacketMultiBlockChange constructor is not supported by ChunkAPI. Please report this to FalsePattern!");
    }

    @Override
    public void chunkapi$init(int count, short[] crammedPositions, Chunk chunk) {
        coord = new ChunkCoordIntPair(chunk.xPosition, chunk.zPosition);
        subPackets = new S23PacketBlockChange[count];
        for (int i = 0; i < count; ++i) {
            val subPacket = new S23PacketBlockChange();
            int x = crammedPositions[i] >> 12 & 0xf;
            int z = crammedPositions[i] >> 8 & 0xf;
            int y = crammedPositions[i] & 0xff;
            ((CustomPacketBlockChange) subPacket).chunkapi$init(x, y, z, chunk);
            subPackets[i] = subPacket;
        }
    }


    /**
     * Reads the raw packet data from the data stream.
     *
     * @author FalsePattern
     * @reason Integrate
     */
    @Overwrite
    public void readPacketData(PacketBuffer data) throws IOException {
        coord = new ChunkCoordIntPair(data.readInt(), data.readInt());
        int count = data.readVarIntFromBuffer();
        subPackets = new S23PacketBlockChange[count];
        for (int i = 0; i < count; ++i) {
            val subPacket = new S23PacketBlockChange();
            subPackets[i] = subPacket;
            val cSub = (CustomPacketBlockChange) subPacket;
            val pos = data.readUnsignedShort();
            val x = pos >> 12 & 0xf;
            val z = pos >> 8 & 0xf;
            val y = pos & 0xff;
            cSub.chunkapi$x(x);
            cSub.chunkapi$y(y);
            cSub.chunkapi$z(z);
            DataRegistryImpl.readBlockPacketFromBuffer(subPacket, data);
        }
    }

    /**
     * Writes the raw packet data to the data stream.
     *
     * @author FalsePattern
     * @reason Integrate
     */
    @Overwrite
    public void writePacketData(PacketBuffer data) throws IOException {
        data.writeInt(coord.chunkXPos);
        data.writeInt(coord.chunkZPos);

        if (subPackets != null) {
            data.writeVarIntToBuffer(subPackets.length);
            for (val subPacket : subPackets) {
                val cSub = (CustomPacketBlockChange) subPacket;
                int pos = ((cSub.chunkapi$x() & 0xf) << 12) |
                          ((cSub.chunkapi$z() & 0xf) << 8) |
                          (cSub.chunkapi$y() & 0xff);
                data.writeShort(pos);
                DataRegistryImpl.writeBlockPacketToBuffer(subPacket, data);
            }
        } else {
            data.writeInt(0);
        }
    }

    @Override
    public S23PacketBlockChange[] chunkapi$subPackets() {
        return subPackets;
    }
}
