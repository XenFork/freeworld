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
import io.github.xenfork.freeworld.client.world.chunk.ClientChunk;
import io.github.xenfork.freeworld.util.Direction;
import io.github.xenfork.freeworld.world.World;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingDeque;
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
    private final ClientChunk chunk;

    public WorldRenderer(Freeworld client, GameRenderer gameRenderer, World world) {
        this.client = client;
        this.gameRenderer = gameRenderer;
        this.world = world;
        final int processors = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(processors,
            processors + 1,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(),
            new ThreadPoolExecutor.DiscardPolicy());
        chunk = new ClientChunk(world, 0, 0, 0);
        chunk.generateTerrain();
    }

    public void compileChunks() {
    }

    public void render() {
//        final Vector3dc position = client.camera().position();
        final BlockRenderer blockRenderer = gameRenderer.blockRenderer();
        final Tessellator t = Tessellator.getInstance();
        t.begin(GLDrawMode.TRIANGLES);
        for (Direction direction : Direction.LIST) {
            for (int x = chunk.fromX(), x1 = chunk.toX(); x < x1; x++) {
                for (int y = chunk.fromY(), y1 = chunk.toY(); y < y1; y++) {
                    for (int z = chunk.fromZ(), z1 = chunk.toZ(); z < z1; z++) {
                        if (chunk.getBlockState(x + direction.axisX(), y + direction.axisY(), z + direction.axisZ()).blockType().air()) {
                            blockRenderer.renderBlockFace(t, chunk.getBlockState(x, y, z), x, y, z, direction);
                        }
                    }
                }
            }
        }
        t.end();
    }

    @Override
    public void close() {
        executor.close();
    }
}
