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
import io.github.xenfork.freeworld.client.render.gl.GLResource;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.client.render.model.VertexLayouts;
import io.github.xenfork.freeworld.client.world.chunk.ClientChunk;
import io.github.xenfork.freeworld.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class WorldRenderer implements GLResource {
    private final GameRenderer gameRenderer;
    private final World world;
    private final ExecutorService executor;
    private final VertexBuilderPool<DefaultVertexBuilder> vertexBuilderPool = new VertexBuilderPool<>(() -> new DefaultVertexBuilder(VertexLayouts.POSITION_COLOR_TEX, 30000, 60000));
    private final ClientChunk[] chunks;

    public WorldRenderer(GameRenderer gameRenderer, World world) {
        this.gameRenderer = gameRenderer;
        this.world = world;
        final int processors = Runtime.getRuntime().availableProcessors();
        this.executor = new ThreadPoolExecutor(processors,
            processors,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingDeque<>(),
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);

                @Override
                public Thread newThread(@NotNull Runnable r) {
                    return new Thread(r, STR."ChunkCompiler-thread-\{threadNumber.getAndIncrement()}");
                }
            },
            new ThreadPoolExecutor.DiscardPolicy());
        this.chunks = new ClientChunk[world.xChunks * world.yChunks * world.zChunks];
        for (int x = 0; x < world.xChunks; x++) {
            for (int y = 0; y < world.yChunks; y++) {
                for (int z = 0; z < world.zChunks; z++) {
                    this.chunks[(y * world.zChunks + z) * world.xChunks + x] = new ClientChunk(world, x, y, z);
                }
            }
        }
    }

    public void compileChunks() {
        for (ClientChunk chunk : chunks) {
            if (chunk.shouldRecompile.get() && !chunk.submitted.get()) {
                chunk.future.set(executor.submit(new ChunkCompileTask(gameRenderer, this, world.getChunk(chunk.x(), chunk.y(), chunk.z()))));
                chunk.submitted.set(true);
            }
        }
    }

    public void renderChunks(GLStateMgr gl) {
        for (ClientChunk chunk : chunks) {
            chunk.render(gl);
        }
    }

    public VertexBuilderPool<DefaultVertexBuilder> vertexBuilderPool() {
        return vertexBuilderPool;
    }

    @Override
    public void close(GLStateMgr gl) {
        executor.close();
        for (ClientChunk chunk : chunks) {
            chunk.close(gl);
        }
    }
}
