/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.chunk;

/**
 * @author squid233
 * @since 0.1.0
 */
public record ChunkPos(int x, int y, int z) {
    public static int relativeToAbsolute(int chunkPos, int relativePos) {
        return chunkPos * Chunk.SIZE + relativePos;
    }

    public static int absoluteToRelative(int absolutePos) {
        return Math.floorMod(absolutePos, Chunk.SIZE);
    }

    public static int absoluteToChunk(int absolutePos) {
        return Math.floorDiv(absolutePos, Chunk.SIZE);
    }
}
