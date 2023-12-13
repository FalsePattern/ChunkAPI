/*
 * Copyright (c) 2023 FalsePattern
 * This work is licensed under the Creative Commons Attribution-NonCommercial-NoDerivatives 4.0 International License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-nd/4.0/
 * or send a letter to Creative Commons, PO Box 1866, Mountain View, CA 94042, USA.
 */

package com.falsepattern.chunk.api;

import com.falsepattern.lib.StableAPI;

import net.minecraft.world.chunk.NibbleArray;

import java.util.Arrays;

/**
 * Miscellaneous utilities for in-place array transfer (where possible).
 */
@StableAPI(since = "0.5.0")
public class ArrayUtil {
    public static boolean[] copyArray(boolean[] src, boolean[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static byte[] copyArray(byte[] src, byte[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static char[] copyArray(char[] src, char[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static short[] copyArray(short[] src, short[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static int[] copyArray(int[] src, int[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static long[] copyArray(long[] src, long[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static float[] copyArray(float[] src, float[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static double[] copyArray(double[] src, double[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

    public static <T> T[] copyArray(T[] src, T[] dst) {
        if (src == null)
            return null;
        if (dst == null || src.length != dst.length)
            return Arrays.copyOf(src, src.length);
        System.arraycopy(src, 0, dst, 0, src.length);
        return dst;
    }

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
