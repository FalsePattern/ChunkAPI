package com.falsepattern.chunk.internal.mixin.mixins.common.vanilla;

import com.falsepattern.chunk.api.ChunkDataManager;
import com.falsepattern.chunk.api.ModdedChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.Map;

/**
 * This mixin injects the {@link ModdedChunk} interface into {@link Chunk}, and also creates the data map.
 */
@Mixin(Chunk.class)
public abstract class ChunkDataMixin implements ModdedChunk {
    private Map<String, Object> data;

    @Inject(method = "<init>(Lnet/minecraft/world/World;II)V",
            at = @At("RETURN"))
    private void init(World p_i1995_1_, int p_i1995_2_, int p_i1995_3_, CallbackInfo ci) {
        data = new HashMap<>();
    }

    @Override
    public <T> T getData(ChunkDataManager manager) {
        return (T) data.get(manager.id());
    }

    @Override
    public <T> void setData(ChunkDataManager manager, T data) {
        this.data.put(manager.id(), data);
    }

    @Override
    public boolean hasData(ChunkDataManager manager) {
        return data.containsKey(manager.id());
    }

    @Override
    public <T> T removeData(ChunkDataManager manager) {
        return (T) data.remove(manager.id());
    }
}
