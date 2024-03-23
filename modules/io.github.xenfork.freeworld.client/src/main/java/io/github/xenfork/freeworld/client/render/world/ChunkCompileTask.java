/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.world;

import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.client.render.builder.DefaultVertexBuilder;
import io.github.xenfork.freeworld.util.Direction;
import io.github.xenfork.freeworld.world.chunk.Chunk;
import io.github.xenfork.freeworld.world.chunk.ChunkPos;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.util.concurrent.Callable;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkCompileTask implements Callable<ChunkVertexData> {
    private final GameRenderer gameRenderer;
    private final WorldRenderer worldRenderer;
    private final Chunk chunk;

    public ChunkCompileTask(GameRenderer gameRenderer, WorldRenderer worldRenderer, Chunk chunk) {
        this.gameRenderer = gameRenderer;
        this.worldRenderer = worldRenderer;
        this.chunk = chunk;
    }

    @Override
    public ChunkVertexData call() {
        final var pool = worldRenderer.vertexBuilderPool();
        final DefaultVertexBuilder builder = pool.acquire();
        try {
            final int cx = chunk.x();
            final int cy = chunk.y();
            final int cz = chunk.z();
            for (Direction direction : Direction.LIST) {
                for (int x = 0; x < Chunk.SIZE; x++) {
                    for (int y = 0; y < Chunk.SIZE; y++) {
                        for (int z = 0; z < Chunk.SIZE; z++) {
                            final int nx = x + direction.axisX();
                            final int ny = y + direction.axisY();
                            final int nz = z + direction.axisZ();
                            if (chunk.isInBound(nx, ny, nz) && chunk.getBlockType(nx, ny, nz).air()) {
                                gameRenderer.blockRenderer().renderBlockFace(
                                    builder,
                                    chunk.getBlockType(x, y, z),
                                    ChunkPos.relativeToAbsolute(cx, x),
                                    ChunkPos.relativeToAbsolute(cy, y),
                                    ChunkPos.relativeToAbsolute(cz, z),
                                    direction);
                            }
                        }
                    }
                }
            }

            final Arena arena = Arena.ofShared();
            final MemorySegment vertexDataSlice = builder.vertexDataSlice();
            final MemorySegment indexDataSlice = builder.indexDataSlice();
            return new ChunkVertexData(
                builder.vertexLayout(),
                builder.indexCount(),
                arena,
                arena.allocateFrom(ValueLayout.JAVA_BYTE, vertexDataSlice, ValueLayout.JAVA_BYTE, 0L, vertexDataSlice.byteSize()),
                arena.allocateFrom(ValueLayout.JAVA_BYTE, indexDataSlice, ValueLayout.JAVA_BYTE, 0L, indexDataSlice.byteSize()),
                builder.shouldReallocateVertexData(),
                builder.shouldReallocateIndexData()
            );
        } finally {
            pool.release(builder);
        }
    }
}
