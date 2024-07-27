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
import freeworld.core.registry.Registries;
import freeworld.math.Vector3i;
import freeworld.world.chunk.Chunk;
import freeworld.util.math.ChunkPos;

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
        Vector3i chunkPos = new Vector3i(cx, cy, cz);

        for (int x = 0; x < Chunk.SIZE; x++) {
            for (int y = 0; y < Chunk.SIZE; y++) {
                for (int z = 0; z < Chunk.SIZE; z++) {
                    Vector3i finalPos = new Vector3i(x, y, z);
                    final BlockModel model = blockModelManager.get(Registries.BLOCK_TYPE.getId(chunk.getBlockType(x, y, z)));
                    blockRenderer.renderBlockModel(
                        vertexBuilder,
                        model,
                        ChunkPos.relativeToAbsolute(cx, x),
                        ChunkPos.relativeToAbsolute(cy, y),
                        ChunkPos.relativeToAbsolute(cz, z),
                        direction -> {
                            Vector3i nPos = direction.axis().add(finalPos);
                            Vector3i abs = ChunkPos.relativeToAbsolute(chunkPos, nPos);
                            final boolean shouldRender =
                                (chunk.isInBound(nPos.x(), nPos.y(), nPos.z()) &&
                                 chunk.getBlockType(nPos.x(), nPos.y(), nPos.z()).hasSidedTransparency()) ||
                                (chunk.world().isBlockLoaded(abs.x(), abs.y(), abs.z()) &&
                                 chunk.world().getBlockType(abs.x(), abs.y(), abs.z()).hasSidedTransparency()) ||
                                !chunk.world().isBlockLoaded(abs.x(), abs.y(), abs.z()) /* TODO: add method world::tryLoading() */;
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
