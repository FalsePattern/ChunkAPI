package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.internal.impl.CustomPacketMultiBlockChange;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.S22PacketMultiBlockChange;
import net.minecraft.server.management.PlayerManager;
import net.minecraft.world.chunk.Chunk;

@Mixin(PlayerManager.PlayerInstance.class)
public abstract class PlayerInstanceMixin {
    /**
     * S22PacketMultiBlockChange is an absolute, utter pain in the ass to properly integrate with.
     */
    @Redirect(method = "sendChunkUpdate",
              at = @At(value = "NEW",
                       target = "(I[SLnet/minecraft/world/chunk/Chunk;)Lnet/minecraft/network/play/server/S22PacketMultiBlockChange;"),
              require = 1)
    private S22PacketMultiBlockChange hijackPacket(int count, short[] positions, Chunk chunk) {
        val packet = new S22PacketMultiBlockChange();
        ((CustomPacketMultiBlockChange)packet).chunkapi$init(count, positions, chunk);
        return packet;
    }
}
