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

import com.falsepattern.chunk.internal.impl.CustomPacketMultiBlockChange;
import lombok.val;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

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
        ((CustomPacketMultiBlockChange) packet).chunkapi$init(count, positions, chunk);
        return packet;
    }
}
