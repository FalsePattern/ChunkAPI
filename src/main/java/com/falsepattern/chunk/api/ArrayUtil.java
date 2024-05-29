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

package com.falsepattern.chunk.api;

import com.falsepattern.lib.StableAPI;
import org.jetbrains.annotations.Contract;

import net.minecraft.world.chunk.NibbleArray;

import java.util.Arrays;

/**
 * This is an API class covered by the additional permissions in the license.
 * <p>
 * Miscellaneous utilities for in-place array transfer (where possible).
 */
@StableAPI(since = "0.5.0")
public class ArrayUtil {

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static boolean[] copyArray(boolean[] src, boolean[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static byte[] copyArray(byte[] src, byte[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static char[] copyArray(char[] src, char[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static short[] copyArray(short[] src, short[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static int[] copyArray(int[] src, int[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static long[] copyArray(long[] src, long[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static float[] copyArray(float[] src, float[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static double[] copyArray(double[] src, double[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static <T> T[] copyArray(T[] src, T[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    @StableAPI.Expose
    @Contract(value = "null, _ -> null;" +
                      "!null, null -> new;",
              mutates = "param2")
    public static NibbleArray copyArray(NibbleArray srcArray, NibbleArray dstArray) {
        if (srcArray == null) {
            return null;
        }
        if (dstArray == null) {
            return new NibbleArray(Arrays.copyOf(srcArray.data, srcArray.data.length), srcArray.depthBits);
        }
        dstArray.depthBits = srcArray.depthBits;
        dstArray.depthBitsPlusFour = srcArray.depthBitsPlusFour;
        dstArray.data = copyArray(srcArray.data, dstArray.data);
        return dstArray;
    }
}
