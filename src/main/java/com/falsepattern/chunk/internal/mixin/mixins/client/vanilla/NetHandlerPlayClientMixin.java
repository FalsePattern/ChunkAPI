package com.falsepattern.chunk.internal.mixin.mixins.client.vanilla;

import com.falsepattern.chunk.internal.DataRegistryImpl;
import com.falsepattern.chunk.internal.impl.CustomPacketBlockChange;
import com.falsepattern.chunk.internal.impl.CustomPacketMultiBlockChange;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.network.play.server.S23PacketBlockChange;

@Mixin(NetHandlerPlayClient.class)
public abstract class NetHandlerPlayClientMixin implements INetHandlerPlayClient {
    @Shadow
    private WorldClient clientWorldController;

    @Inject(method = "handleBlockChange",
            at = @At(value = "RETURN"),
            require = 1)
    private void doHandleBlockChange(S23PacketBlockChange packetIn, CallbackInfo ci) {
        val cPacket = (CustomPacketBlockChange) packetIn;
        val x = cPacket.chunkapi$x();
        val y = cPacket.chunkapi$y();
        val z = cPacket.chunkapi$z();
        val chunk = clientWorldController.getChunkFromBlockCoords(x, z);
        DataRegistryImpl.readBlockFromPacket(chunk, x & 0xf, y, z & 0xf, packetIn);
    }

    /**
     * @author FalsePattern
     * @reason Integrate
     */
    @Overwrite
    public void handleMultiBlockChange(S22PacketMultiBlockChange packetIn) {
        val cX = packetIn.func_148920_c().chunkXPos;
        val cZ = packetIn.func_148920_c().chunkZPos;
        val chunk = clientWorldController.getChunkFromChunkCoords(cX, cZ);
        val bX = cX * 16;
        val bZ = cZ * 16;
        for (val subPacket : ((CustomPacketMultiBlockChange) packetIn).chunkapi$subPackets()) {
            val cSub = (CustomPacketBlockChange) subPacket;
            val x = cSub.chunkapi$x();
            val y = cSub.chunkapi$y();
            val z = cSub.chunkapi$z();
            this.clientWorldController.func_147492_c(x + bX, y, z + bZ, subPacket.func_148880_c(), subPacket.func_148881_g());
            DataRegistryImpl.readBlockFromPacket(chunk, x, y, z, subPacket);
        }
    }
}
