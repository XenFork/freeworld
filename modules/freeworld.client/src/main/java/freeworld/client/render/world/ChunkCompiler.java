/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.world;

import freeworld.client.render.builder.VertexBuilder;
import freeworld.client.render.model.block.BlockModel;
import freeworld.client.render.model.block.BlockModelManager;
import freeworld.core.registry.BuiltinRegistries;
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

    public static ChunkVertexData compile(
        VertexBuilder vertexBuilder,
        BlockRenderer blockRenderer,
        BlockModelManager blockModelManager,
        Chunk chunk) {
        vertexBuilder.reset();
        final int cx = chunk.x();
        final int cy = chunk.y();
        final int cz = chunk.z();

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    int finalX = x;
                    int finalY = y;
                    int finalZ = z;
                    final BlockModel model = blockModelManager.get(BuiltinRegistries.BLOCK_TYPE.getId(chunk.getBlockType(x, y, z)));
                    blockRenderer.renderBlockModel(
                        vertexBuilder,
                        model,
                        ChunkPos.relativeToAbsolute(cx, x),
                        ChunkPos.relativeToAbsolute(cy, y),
                        ChunkPos.relativeToAbsolute(cz, z),
                        direction -> {
                            final int nx = finalX + direction.axisX();
                            final int ny = finalY + direction.axisY();
                            final int nz = finalZ + direction.axisZ();
                            final int absNx = ChunkPos.relativeToAbsolute(cx, nx);
                            final int absNy = ChunkPos.relativeToAbsolute(cy, ny);
                            final int absNz = ChunkPos.relativeToAbsolute(cz, nz);
                            final boolean shouldRender =
                                (chunk.isInBound(nx, ny, nz) &&
                                 chunk.getBlockType(nx, ny, nz).air()) ||
                                (chunk.world().isBlockLoaded(absNx, absNy, absNz) &&
                                 chunk.world().getBlockType(absNx, absNy, absNz).air());
                            return !shouldRender;
                        }
                    );
                }
            }
        }

        final Arena arena = Arena.ofAuto();
        final MemorySegment vertexDataSlice = vertexBuilder.vertexDataSlice();
        final MemorySegment indexDataSlice = vertexBuilder.indexDataSlice();
        return new ChunkVertexData(
            vertexBuilder.vertexLayout(),
            vertexBuilder.indexCount(),
            arena.allocateFrom(ValueLayout.JAVA_BYTE, vertexDataSlice, ValueLayout.JAVA_BYTE, 0L, vertexDataSlice.byteSize()),
            arena.allocateFrom(ValueLayout.JAVA_BYTE, indexDataSlice, ValueLayout.JAVA_BYTE, 0L, indexDataSlice.byteSize()),
            vertexBuilder.shouldReallocateVertexData(),
            vertexBuilder.shouldReallocateIndexData()
        );
    }
}
