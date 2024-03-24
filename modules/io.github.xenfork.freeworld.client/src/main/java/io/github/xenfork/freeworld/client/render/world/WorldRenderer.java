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
import io.github.xenfork.freeworld.core.math.AABBox;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.chunk.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.joml.*;

import java.lang.Runtime;
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
    private final VertexBuilderPool<DefaultVertexBuilder> vertexBuilderPool = new VertexBuilderPool<>(WorldRenderer::createVertexBuilder);
    private final ClientChunk[] chunks;
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();
    private final FrustumRayBuilder frustumRayBuilder = new FrustumRayBuilder();
    private final Vector3f frustumRayOrigin = new Vector3f();
    private final Vector3f frustumRayDir = new Vector3f();
    private final Vector2f frustumIntersectionResult = new Vector2f();
    private final Vector2d blockIntersectionResult = new Vector2d();
    private HitResult hitResult = new HitResult(null, 0, 0, 0, true);

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
                    this.chunks[(y * world.zChunks + z) * world.xChunks + x] = new ClientChunk(world, x, y, z, world.getChunk(x, y, z));
                }
            }
        }
    }

    private static DefaultVertexBuilder createVertexBuilder() {
        return new DefaultVertexBuilder(VertexLayouts.POSITION_COLOR_TEX, 30000, 60000);
    }

    public void compileChunks() {
        for (ClientChunk chunk : chunks) {
            if (chunk.shouldRecompile.get() && !chunk.submitted.get()) {
                chunk.future.set(executor.submit(new ChunkCompileTask(gameRenderer, this, chunk.chunk())));
                chunk.submitted.set(true);
            }
        }
    }

    public void renderChunks(GLStateMgr gl) {
        final Matrix4f matrix = gameRenderer.projectionViewMatrix();
        frustumIntersection.set(matrix);
        frustumRayBuilder.set(matrix);
        frustumRayBuilder.origin(frustumRayOrigin);
        frustumRayBuilder.dir(0.5f, 0.5f, frustumRayDir);
        frustumIntersectionResult.zero();
        float nearestChunkDistance = Float.POSITIVE_INFINITY;
        ClientChunk nearestChunk = null;
        for (ClientChunk chunk : chunks) {
            if (frustumIntersection.testAab(
                chunk.fromX(),
                chunk.fromY(),
                chunk.fromZ(),
                chunk.toX(),
                chunk.toY(),
                chunk.toZ()
            )) {
                chunk.render(gl);
                if (Intersectionf.intersectRayAab(
                    frustumRayOrigin.x(),
                    frustumRayOrigin.y(),
                    frustumRayOrigin.z(),
                    frustumRayDir.x(),
                    frustumRayDir.y(),
                    frustumRayDir.z(),
                    chunk.fromX(),
                    chunk.fromY(),
                    chunk.fromZ(),
                    chunk.toX(),
                    chunk.toY(),
                    chunk.toZ(),
                    frustumIntersectionResult
                ) && frustumIntersectionResult.x() < nearestChunkDistance) {
                    nearestChunkDistance = frustumIntersectionResult.x();
                    nearestChunk = chunk;
                }
            }
        }
        // TODO: 2024/3/24 squid233: This is buggy
        if (nearestChunk != null) {
            double nearestBlockDistance = Float.POSITIVE_INFINITY;
            BlockType nearestBlock = null;
            int nearestX = 0;
            int nearestY = 0;
            int nearestZ = 0;
            for (int x = nearestChunk.fromX(); x < nearestChunk.toX(); x++) {
                for (int y = nearestChunk.fromY(); y < nearestChunk.toY(); y++) {
                    for (int z = nearestChunk.fromZ(); z < nearestChunk.toZ(); z++) {
                        final BlockType blockType = nearestChunk.chunk().getBlockType(
                            ChunkPos.absoluteToRelative(x),
                            ChunkPos.absoluteToRelative(y),
                            ChunkPos.absoluteToRelative(z)
                        );
                        if (blockType.air()) {
                            continue;
                        }
                        final AABBox box = blockType.outlineShape().move(x, y, z);
                        if (Intersectiond.intersectRayAab(
                            frustumRayOrigin.x(),
                            frustumRayOrigin.y(),
                            frustumRayOrigin.z(),
                            frustumRayDir.x(),
                            frustumRayDir.y(),
                            frustumRayDir.z(),
                            box.minX(),
                            box.minY(),
                            box.minZ(),
                            box.maxX(),
                            box.maxY(),
                            box.maxZ(),
                            blockIntersectionResult
                        ) && blockIntersectionResult.x() < nearestBlockDistance) {
                            nearestBlockDistance = blockIntersectionResult.x();
                            nearestBlock = blockType;
                            nearestX = x;
                            nearestY = y;
                            nearestZ = z;
                        }
                    }
                }
            }
            hitResult = new HitResult(nearestBlock, nearestX, nearestY, nearestZ, nearestBlock == null);
        }
    }

    public VertexBuilderPool<DefaultVertexBuilder> vertexBuilderPool() {
        return vertexBuilderPool;
    }

    public HitResult hitResult() {
        return hitResult;
    }

    @Override
    public void close(GLStateMgr gl) {
        executor.close();
        for (ClientChunk chunk : chunks) {
            chunk.close(gl);
        }
    }
}
