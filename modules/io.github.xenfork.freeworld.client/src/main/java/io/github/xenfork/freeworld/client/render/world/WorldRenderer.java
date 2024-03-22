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

import io.github.xenfork.freeworld.client.Freeworld;
import io.github.xenfork.freeworld.client.render.GameRenderer;
import io.github.xenfork.freeworld.client.render.Tessellator;
import io.github.xenfork.freeworld.client.render.gl.GLDrawMode;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.util.Direction;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.chunk.Chunk;
import io.github.xenfork.freeworld.world.chunk.ChunkPos;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class WorldRenderer implements AutoCloseable {
    private final Freeworld client;
    private final GameRenderer gameRenderer;
    private final World world;
    private final ExecutorService executor;

    public WorldRenderer(Freeworld client, GameRenderer gameRenderer, World world) {
        this.client = client;
        this.gameRenderer = gameRenderer;
        this.world = world;
        final int processors = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(processors,
            processors,
            0L,
            TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<>(),
            new ThreadPoolExecutor.DiscardPolicy());
    }

    public void compileChunks() {
    }

    public void render(GLStateMgr gl, Tessellator tessellator) {
//        final Vector3dc position = client.camera().position();
        final BlockRenderer blockRenderer = gameRenderer.blockRenderer();
        tessellator.begin(GLDrawMode.TRIANGLES);
        for (Chunk chunk : world.chunks) {
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
                                blockRenderer.renderBlockFace(
                                    tessellator,
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
        }
        tessellator.end(gl);
    }

    @Override
    public void close() {
        executor.close();
    }
}
