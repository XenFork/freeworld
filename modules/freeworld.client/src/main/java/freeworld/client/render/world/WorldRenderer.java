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

import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.builder.DefaultVertexBuilder;
import freeworld.client.render.gl.GLResource;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.model.VertexLayouts;
import freeworld.client.world.chunk.ClientChunk;
import freeworld.core.math.AABBox;
import freeworld.util.Direction;
import freeworld.util.Logging;
import freeworld.world.World;
import freeworld.world.WorldListener;
import freeworld.world.block.BlockType;
import freeworld.world.chunk.ChunkPos;
import freeworld.world.entity.Entity;
import org.joml.*;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.pool.Pool;
import reactor.pool.PoolBuilder;

import java.lang.Math;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class WorldRenderer implements GLResource, WorldListener {
    private static final Logger logger = Logging.caller();
    public static final int RENDER_RADIUS = 5;
    public static final int RENDER_CHUNK_COUNT_CBRT = RENDER_RADIUS * 2 + 1;
    public static final int RENDER_CHUNK_COUNT = RENDER_CHUNK_COUNT_CBRT * RENDER_CHUNK_COUNT_CBRT * RENDER_CHUNK_COUNT_CBRT;
    private final GameRenderer gameRenderer;
    private final World world;
    private final Scheduler scheduler = Schedulers.newParallel("WorldRenderer");
    private final Pool<DefaultVertexBuilder> vertexBuilderPool = PoolBuilder
        .from(Mono.fromCallable(WorldRenderer::createVertexBuilder).subscribeOn(scheduler))
        .buildPool();
    private final Map<ChunkPos, ClientChunk> chunks = new ConcurrentHashMap<>(RENDER_CHUNK_COUNT);
    private final Disposable chunkGC;
    private final FrustumIntersection frustumIntersection = new FrustumIntersection();
    private final FrustumRayBuilder frustumRayBuilder = new FrustumRayBuilder();
    private final Vector3f frustumRayOrigin = new Vector3f();
    private final Vector3f frustumRayDir = new Vector3f();
    private final Vector2d blockIntersectionResult = new Vector2d();

    public WorldRenderer(GameRenderer gameRenderer, World world) {
        this.gameRenderer = gameRenderer;
        this.world = world;
        world.addListener(this);
        this.chunkGC = Flux.interval(Duration.ofSeconds(60))
            .subscribe(_ -> {
                final List<ChunkPos> list = new ArrayList<>(RENDER_CHUNK_COUNT);
                World.forEachChunk(gameRenderer.client().player(), RENDER_RADIUS, (x, y, z) -> list.add(new ChunkPos(x, y, z)));
                final var it = chunks.entrySet().iterator();
                while (it.hasNext()) {
                    final var e = it.next();
                    if (!list.contains(e.getKey())) {
                        e.getValue().close();
                        it.remove();
                    }
                }
            });
    }

    private static DefaultVertexBuilder createVertexBuilder() {
        return new DefaultVertexBuilder(VertexLayouts.POSITION_COLOR_TEX, 30000, 45000);
    }

    public List<ClientChunk> renderingChunks(Entity player) {
        final List<ClientChunk> chunks = new ArrayList<>(RENDER_CHUNK_COUNT);
        World.forEachChunk(player, RENDER_RADIUS, (x, y, z) -> chunks.add(getChunkOrCreate(x, y, z)));
        chunks.sort(Comparator
            .<ClientChunk>comparingDouble(o -> o.yDistanceToPlayer(player))
            .thenComparingDouble(o -> o.xzDistanceToPlayerSquared(player)));
        return chunks;
    }

    public void compileChunks(List<ClientChunk> renderingChunks) {
        for (ClientChunk chunk : renderingChunks) {
            chunk.compile();
        }
    }

    public void renderChunks(GLStateMgr gl, List<ClientChunk> renderingChunks) {
        frustumIntersection.set(RenderSystem.projectionViewMatrix());
        for (ClientChunk chunk : renderingChunks) {
            if (frustumIntersection.testAab(
                chunk.fromX(),
                chunk.fromY(),
                chunk.fromZ(),
                chunk.toX(),
                chunk.toY(),
                chunk.toZ()
            )) {
                chunk.render(gl);
            }
        }
    }

    public HitResult selectBlock(Entity player) {
        frustumRayBuilder.set(RenderSystem.projectionViewMatrix());
        frustumRayBuilder.origin(frustumRayOrigin);
        frustumRayBuilder.dir(0.5f, 0.5f, frustumRayDir);
        final float ox = frustumRayOrigin.x();
        final float oy = frustumRayOrigin.y();
        final float oz = frustumRayOrigin.z();

        double nearestBlockDistance = Float.POSITIVE_INFINITY;
        BlockType nearestBlock = null;
        int nearestX = 0;
        int nearestY = 0;
        int nearestZ = 0;
        Direction face = Direction.SOUTH;

        final float radius = 5.0f;
        final float radiusSquared = radius * radius;
        final AABBox range = player.boundingBox()
            .value()
            .grow(radius, radius, radius);
        final int x0 = (int) Math.floor(range.minX());
        final int y0 = (int) Math.floor(range.minY());
        final int z0 = (int) Math.floor(range.minZ());
        final int x1 = (int) Math.ceil(range.maxX());
        final int y1 = (int) Math.ceil(range.maxY());
        final int z1 = (int) Math.ceil(range.maxZ());
        for (int x = x0; x <= x1; x++) {
            final float vx = x + 0.5f - ox;
            final float xSquared = vx * vx;
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (!world.isBlockLoaded(x, y, z)) {
                        continue;
                    }
                    final float vz = z + 0.5f - oz;
                    final float zSquared = vz * vz;
                    if ((xSquared + zSquared) <= radiusSquared) {
                        final BlockType blockType = world.getBlockType(x, y, z);
                        if (blockType.air()) {
                            continue;
                        }
                        final AABBox box = blockType.outlineShape().move(x, y, z);
                        if (Intersectiond.intersectRayAab(
                            ox,
                            oy,
                            oz,
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
                            face = detectFace(
                                ox,
                                oy,
                                oz,
                                frustumRayDir.x(),
                                frustumRayDir.y(),
                                frustumRayDir.z(),
                                box
                            );
                        }
                    }
                }
            }
        }

        return new HitResult(nearestBlock == null, nearestBlock, nearestX, nearestY, nearestZ, face);
    }

    private Direction detectFace(
        double originX,
        double originY,
        double originZ,
        double dirX,
        double dirY,
        double dirZ,
        AABBox box
    ) {
        double t = -1.0;
        Direction direction = Direction.SOUTH;
        for (Direction dir : Direction.LIST) {
            final double v = rayFace(dir, originX, originY, originZ, dirX, dirY, dirZ, box);
            if (v > t) {
                t = v;
                direction = dir;
            }
        }
        return direction;
    }

    private double rayFace(
        Direction direction,
        double originX,
        double originY,
        double originZ,
        double dirX,
        double dirY,
        double dirZ,
        AABBox box
    ) {
        final double epsilon = 0.001;
        final double minX = box.minX();
        final double minY = box.minY();
        final double minZ = box.minZ();
        final double maxX = box.maxX();
        final double maxY = box.maxY();
        final double maxZ = box.maxZ();
        return switch (direction) {
            case WEST -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, maxY, minZ,
                    minX, minY, minZ,
                    minX, minY, maxZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, minY, maxZ,
                    minX, maxY, maxZ,
                    minX, maxY, minZ,
                    epsilon
                )
            );
            case EAST -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, maxY, maxZ,
                    maxX, minY, maxZ,
                    maxX, minY, minZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, minY, minZ,
                    maxX, maxY, minZ,
                    maxX, maxY, maxZ,
                    epsilon
                )
            );
            case DOWN -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, minY, maxZ,
                    minX, minY, minZ,
                    maxX, minY, minZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, minY, minZ,
                    maxX, minY, maxZ,
                    minX, minY, maxZ,
                    epsilon
                )
            );
            case UP -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, maxY, minZ,
                    minX, maxY, maxZ,
                    maxX, maxY, maxZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, maxY, maxZ,
                    maxX, maxY, minZ,
                    minX, maxY, minZ,
                    epsilon
                )
            );
            case NORTH -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, maxY, minZ,
                    maxX, minY, minZ,
                    minX, minY, minZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, minY, minZ,
                    minX, maxY, minZ,
                    maxX, maxY, minZ,
                    epsilon
                )
            );
            case SOUTH -> Math.max(
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    minX, maxY, maxZ,
                    minX, minY, maxZ,
                    maxX, minY, maxZ,
                    epsilon
                ),
                Intersectiond.intersectRayTriangleFront(
                    originX, originY, originZ,
                    dirX, dirY, dirZ,
                    maxX, minY, maxZ,
                    maxX, maxY, maxZ,
                    minX, maxY, maxZ,
                    epsilon
                )
            );
        };
    }

    @Override
    public void onBlockChanged(int x, int y, int z) {
        final ClientChunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            chunk.markDirty();
        }
        for (Direction direction : Direction.LIST) {
            final ClientChunk chunk1 = getChunkByAbsolutePos(
                x + direction.axisX(),
                y + direction.axisY(),
                z + direction.axisZ()
            );
            if (chunk1 != null) {
                chunk1.markDirty();
            }
        }
    }

    private ClientChunk getChunk(int x, int y, int z) {
        return chunks.get(new ChunkPos(x, y, z));
    }

    private ClientChunk getChunkOrCreate(int x, int y, int z) {
        return chunks.computeIfAbsent(new ChunkPos(x, y, z),
            chunkPos -> new ClientChunk(world, this, chunkPos.x(), chunkPos.y(), chunkPos.z()));
    }

    private ClientChunk getChunkByAbsolutePos(int x, int y, int z) {
        return getChunk(
            ChunkPos.absoluteToChunk(x),
            ChunkPos.absoluteToChunk(y),
            ChunkPos.absoluteToChunk(z)
        );
    }

    public Pool<DefaultVertexBuilder> vertexBuilderPool() {
        return vertexBuilderPool;
    }

    public Scheduler scheduler() {
        return scheduler;
    }

    public GameRenderer gameRenderer() {
        return gameRenderer;
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing world renderer");
        scheduler.dispose();
        vertexBuilderPool.dispose();
        chunkGC.dispose();
        for (ClientChunk chunk : chunks.values()) {
            chunk.close();
        }
        chunks.clear();
    }
}
