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

package com.falsepattern.chunk.internal;

import com.falsepattern.chunk.api.OrderedManager;
import lombok.val;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class DualMap<T> {
    private final Map<String, T> unordered = new HashMap<>();
    private final SortedMap<OrderedManager, T> ordered = new TreeMap<>();

    public void put(OrderedManager man, T value) {
        val a = unordered.put(man.id, value);
        val b = ordered.put(man, value);
        if (a != b) {
            throw new AssertionError();
        }
    }

    public int size() {
        return unordered.size();
    }

    public T get(OrderedManager ord) {
        return unordered.get(ord.id);
    }

    public T get(String id) {
        return unordered.get(id);
    }

    public T remove(OrderedManager ord) {
        val a = unordered.remove(ord.id);
        val b = ordered.remove(ord);
        if (a != b) {
            throw new AssertionError();
        }
        return b;
    }

    public boolean containsKey(String id) {
        return unordered.containsKey(id);
    }

    public boolean containsKey(OrderedManager ord) {
        return unordered.containsKey(ord.id);
    }

    public Set<OrderedManager> keySet() {
        return ordered.keySet();
    }

    public Set<Map.Entry<OrderedManager, T>> entrySet() {
        return ordered.entrySet();
    }

    public Iterable<T> values() {
        return ordered.values();
    }
}
