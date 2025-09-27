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

package com.falsepattern.chunk.api;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@RequiredArgsConstructor
public class OrderedManager implements Comparable<OrderedManager> {
    public final int ordering;
    public final @NotNull String id;
    @Override
    public int compareTo(OrderedManager o) {
        int ord = Integer.compare(ordering, o.ordering);
        if (ord != 0)
            return ord;
        return id.compareTo(o.id);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OrderedManager))
            return false;
        val other = (OrderedManager) obj;
        return id.equals(other.id) && ordering == other.ordering;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ordering, id);
    }
}
