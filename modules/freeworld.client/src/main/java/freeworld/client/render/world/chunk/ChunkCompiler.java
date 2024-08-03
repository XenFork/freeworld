/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.world.chunk;

import freeworld.client.render.gl.GLDrawMode;
import freeworld.client.render.model.block.BlockModel;
import freeworld.client.render.model.block.BlockModelManager;
import freeworld.client.render.vertex.BufferBuilder;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.client.render.world.block.BlockRenderer;
import freeworld.math.Vector3i;
import freeworld.registry.Registries;
import freeworld.util.math.ChunkPos;
import freeworld.world.chunk.Chunk;

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

    public static BufferBuilder.BufferData compile(
        BufferBuilder vertexBuilder,
        BlockRenderer blockRenderer,
        BlockModelManager blockModelManager,
        Chunk chunk) {
        vertexBuilder.begin(GLDrawMode.TRIANGLES, VertexLayouts.POSITION_COLOR_TEXTURE);
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
                        ChunkPos.toBlockPosInWorld(cx, x),
                        ChunkPos.toBlockPosInWorld(cy, y),
                        ChunkPos.toBlockPosInWorld(cz, z),
                        direction -> {
                            Vector3i nPos = direction.axis().add(finalPos);
                            Vector3i abs = ChunkPos.toBlockPosInWorld(chunkPos, nPos);
                            final boolean shouldRender =
                                (chunk.isInBound(nPos.x(), nPos.y(), nPos.z()) &&
                                 chunk.getBlockType(nPos.x(), nPos.y(), nPos.z()).hasSidedTransparency()) ||
                                (chunk.world().isBlockLoaded(abs.x(), abs.y(), abs.z()) &&
                                 chunk.world().getBlock(abs).hasSidedTransparency()) ||
                                !chunk.world().isBlockLoaded(abs.x(), abs.y(), abs.z()) /* TODO: add method world::tryLoading() */;
                            return !shouldRender;
                        }
                    );
                }
            }
        }

        BufferBuilder.BufferData buffer = vertexBuilder.end();
        final Arena arena = Arena.ofAuto();
        final MemorySegment vertexData = buffer.vertexData();
        final MemorySegment indexData = buffer.indexData();
        return new BufferBuilder.BufferData(
            arena.allocateFrom(ValueLayout.JAVA_BYTE, vertexData, ValueLayout.JAVA_BYTE, 0L, vertexData.byteSize()),
            arena.allocateFrom(ValueLayout.JAVA_BYTE, indexData, ValueLayout.JAVA_BYTE, 0L, indexData.byteSize()),
            buffer.drawParameter()
        );
    }
}
