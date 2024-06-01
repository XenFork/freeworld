/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.world;

import freeworld.client.render.builder.DefaultVertexBuilder;
import freeworld.util.Direction;
import freeworld.world.chunk.Chunk;
import freeworld.world.chunk.ChunkPos;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class ChunkCompiler {
    private ChunkCompiler() {
    }

    public static ChunkVertexData compile(WorldRenderer worldRenderer, Chunk chunk) {
        final var pool = worldRenderer.vertexBuilderPool();
        DefaultVertexBuilder vertexBuilder = null;
        try {
            vertexBuilder = pool.borrowObject();
            vertexBuilder.reset();
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
                            final int absNx = ChunkPos.relativeToAbsolute(cx, nx);
                            final int absNy = ChunkPos.relativeToAbsolute(cy, ny);
                            final int absNz = ChunkPos.relativeToAbsolute(cz, nz);
                            if ((chunk.isInBound(nx, ny, nz) &&
                                 chunk.getBlockType(nx, ny, nz).air()) ||
                                (chunk.world().isBlockLoaded(absNx, absNy, absNz) &&
                                 chunk.world().getBlockType(absNx, absNy, absNz).air())) {
                                worldRenderer.gameRenderer().blockRenderer().renderBlockFace(
                                    vertexBuilder,
                                    chunk.getBlockType(x, y, z),
                                    ChunkPos.relativeToAbsolute(cx, x),
                                    ChunkPos.relativeToAbsolute(cy, y),
                                    ChunkPos.relativeToAbsolute(cz, z),
                                    direction
                                );
                            }
                        }
                    }
                }
            }

            final Arena arena = Arena.ofAuto();
            final MemorySegment vertexDataSlice = vertexBuilder.vertexDataSlice();
            final MemorySegment indexDataSlice = vertexBuilder.indexDataSlice();
            final ChunkVertexData data = new ChunkVertexData(
                vertexBuilder.vertexLayout(),
                vertexBuilder.indexCount(),
                arena.allocateFrom(ValueLayout.JAVA_BYTE, vertexDataSlice, ValueLayout.JAVA_BYTE, 0L, vertexDataSlice.byteSize()),
                arena.allocateFrom(ValueLayout.JAVA_BYTE, indexDataSlice, ValueLayout.JAVA_BYTE, 0L, indexDataSlice.byteSize()),
                vertexBuilder.shouldReallocateVertexData(),
                vertexBuilder.shouldReallocateIndexData()
            );
            pool.returnObject(vertexBuilder);
            return data;
        } catch (Exception e) {
            try {
                pool.invalidateObject(vertexBuilder);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
    }
}
