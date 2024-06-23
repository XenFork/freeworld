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

import freeworld.client.render.model.vertex.VertexLayout;

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
public record ChunkVertexData(
    VertexLayout vertexLayout,
    int indexCount,
    MemorySegment vertexData,
    MemorySegment indexData,
    boolean shouldReallocateVertexData,
    boolean shouldReallocateIndexData
) {
}
