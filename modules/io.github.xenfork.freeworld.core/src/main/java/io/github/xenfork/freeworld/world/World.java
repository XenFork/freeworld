/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world;

import io.github.xenfork.freeworld.world.chunk.Chunk;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    public final Chunk c0 = new Chunk(this, 0, 0, 0);
    public final Chunk c1 = new Chunk(this, 1, 0, 0);
    public final Chunk c2 = new Chunk(this, 0, 0, 1);
    public final Chunk c3 = new Chunk(this, 1, 0, 1);
    public final List<Chunk> chunks = List.of(c0, c1, c2, c3);

    public World(String name) {
        chunks.forEach(Chunk::generateTerrain);
    }

    public Chunk createChunk(int x, int y, int z) {
        return new Chunk(this, x, y, z);
    }
}
