/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util.math;

import freeworld.math.Vector3i;
import freeworld.world.chunk.Chunk;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkPos {
    public static int toBlockPosInWorld(int blockPosInChunk, int chunkPos) {
        return blockPosInChunk * Chunk.SIZE + chunkPos;
    }

    public static Vector3i toBlockPosInWorld(Vector3i blockPosInChunk, Vector3i chunkPos) {
        return blockPosInChunk.mul(Chunk.SIZE).add(chunkPos);
    }

    public static int toBlockPosInChunk(int blockPos) {
        return Math.floorMod(blockPos, Chunk.SIZE);
    }

    public static int toChunkPos(int blockPos) {
        return Math.floorDiv(blockPos, Chunk.SIZE);
    }

    public static Vector3i toChunkPos(Vector3i blockPos) {
        return blockPos.floorDiv(Chunk.SIZE);
    }
}
