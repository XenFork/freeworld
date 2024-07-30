/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.util.math;

import freeworld.math.Vector3i;
import freeworld.world.chunk.Chunk;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkPos {
    public static int relativeToAbsolute(int chunkPos, int relativePos) {
        return chunkPos * Chunk.SIZE + relativePos;
    }

    public static Vector3i relativeToAbsolute(Vector3i chunkPos, Vector3i relativePos) {
        return chunkPos.mul(Chunk.SIZE).add(relativePos);
    }

    public static int absoluteToRelative(int absolutePos) {
        return Math.floorMod(absolutePos, Chunk.SIZE);
    }

    public static int absoluteToChunk(int absolutePos) {
        return Math.floorDiv(absolutePos, Chunk.SIZE);
    }

    public static Vector3i absoluteToChunk(Vector3i absolutePos) {
        return absolutePos.floorDiv(Chunk.SIZE);
    }
}
