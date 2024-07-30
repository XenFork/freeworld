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

import freeworld.client.render.GameRenderer;
import freeworld.client.render.RenderSystem;
import freeworld.client.render.gl.GLResource;
import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.vertex.DefaultVertexBuilder;
import freeworld.client.render.vertex.VertexLayouts;
import freeworld.client.world.chunk.ClientChunk;
import freeworld.math.*;
import freeworld.util.Direction;
import freeworld.util.Logging;
import freeworld.util.math.AABBox;
import freeworld.util.math.ChunkPos;
import freeworld.util.math.HitResult;
import freeworld.world.World;
import freeworld.world.WorldListener;
import freeworld.world.block.BlockType;
import freeworld.world.entity.Entity;
import org.slf4j.Logger;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.pool.Pool;
import reactor.pool.PoolBuilder;

import java.time.Duration;
import java.util.ArrayList;
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
    private final Scheduler scheduler = Schedulers.newParallel("WorldRenderer-Worker");
    private final Pool<DefaultVertexBuilder> vertexBuilderPool = PoolBuilder
        .from(Mono.fromSupplier(WorldRenderer::createVertexBuilder).subscribeOn(scheduler))
        .buildPool();
    private final Map<Vector3i, ClientChunk> chunks = new ConcurrentHashMap<>(RENDER_CHUNK_COUNT);
    private final Disposable chunkGC;
    private int playerChunkX = 0;
    private int playerChunkY = 0;
    private int playerChunkZ = 0;

    public WorldRenderer(GameRenderer gameRenderer, World world) {
        this.gameRenderer = gameRenderer;
        this.world = world;
        world.addListener(this);
        this.chunkGC = Flux.interval(Duration.ofSeconds(45))
            .subscribe(_ -> uninstallChunks());
    }

    private static DefaultVertexBuilder createVertexBuilder() {
        return new DefaultVertexBuilder(VertexLayouts.POSITION_COLOR_TEX, 30000, 45000);
    }

    private void uninstallChunks() {
        final List<Vector3i> list = new ArrayList<>(RENDER_CHUNK_COUNT);
        World.forEachChunk(gameRenderer.client().player(), RENDER_RADIUS, (x, y, z) -> list.add(new Vector3i(x, y, z)));
        final var it = chunks.entrySet().iterator();
        while (it.hasNext()) {
            final var e = it.next();
            if (!list.contains(e.getKey())) {
                e.getValue().close();
                it.remove();
            }
        }
    }

    public List<ClientChunk> renderingChunks(Entity player) {
        final List<ClientChunk> chunks = new ArrayList<>(RENDER_CHUNK_COUNT);
        World.forEachChunk(player, RENDER_RADIUS, (x, y, z) -> chunks.add(getChunkOrCreate(x, y, z)));
        return chunks;
    }

    public void compileChunks(List<ClientChunk> renderingChunks) {
        for (ClientChunk chunk : renderingChunks) {
            chunk.compile();
        }
    }

    public void renderChunks(GLStateMgr gl, List<ClientChunk> renderingChunks) {
        Vector3d playerPos = gameRenderer.client().player().position();
        int playerChunkX = ChunkPos.absoluteToChunk((int) Math.floor(playerPos.x()));
        int playerChunkY = ChunkPos.absoluteToChunk((int) Math.floor(playerPos.y()));
        int playerChunkZ = ChunkPos.absoluteToChunk((int) Math.floor(playerPos.z()));
        if (playerChunkX != this.playerChunkX ||
            playerChunkY != this.playerChunkY ||
            playerChunkZ != this.playerChunkZ) {
            this.playerChunkX = playerChunkX;
            this.playerChunkY = playerChunkY;
            this.playerChunkZ = playerChunkZ;
            uninstallChunks();
        }

        int builtChunkCount = 0;
        FrustumIntersection frustumIntersection = new FrustumIntersection(RenderSystem.projectionViewMatrix());
        for (ClientChunk chunk : renderingChunks) {
            if (frustumIntersection.testAab(
                chunk.fromX(),
                chunk.fromY(),
                chunk.fromZ(),
                chunk.toX(),
                chunk.toY(),
                chunk.toZ()
            )) {
                if (builtChunkCount < 8 && chunk.shouldBuildBuffer()) {
                    builtChunkCount++;
                    chunk.buildBuffer(gl);
                }
                chunk.render(gl);
            }
        }
    }

    public BlockHitResult selectBlock(Entity player) {
        final FrustumRayBuilder frustumRayBuilder = new FrustumRayBuilder(RenderSystem.projectionViewMatrix());
        final Vector3f frustumRayOrigin = frustumRayBuilder.origin();
        final Vector3f frustumRayDir = frustumRayBuilder.dir(0.5f, 0.5f);
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
        final AABBox range = player.boundingBox().grow(radius, radius, radius);
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
                final float vy = y + 0.5f - oy;
                final float ySquared = vy * vy;
                for (int z = z0; z <= z1; z++) {
                    if (!world.isBlockLoaded(x, y, z)) {
                        continue;
                    }
                    final float vz = z + 0.5f - oz;
                    final float zSquared = vz * vz;
                    if ((xSquared + ySquared + zSquared) <= radiusSquared) {
                        final BlockType blockType = world.getBlockType(x, y, z);
                        if (blockType.air()) {
                            continue;
                        }
                        HitResult hitResult = blockType.outlineShape().rayCast(frustumRayOrigin.sub(x, y, z).toVector3d(), frustumRayDir.toVector3d());
                        if (!hitResult.missed() &&
                            hitResult.distance() < nearestBlockDistance) {
                            nearestBlockDistance = hitResult.distance();
                            nearestBlock = blockType;
                            nearestX = x;
                            nearestY = y;
                            nearestZ = z;
                            face = hitResult.face();
                        }
                    }
                }
            }
        }

        return new BlockHitResult(nearestBlock == null, nearestBlock, new Vector3i(nearestX, nearestY, nearestZ), face);
    }

    @Override
    public void onBlockChanged(int x, int y, int z) {
        final ClientChunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            chunk.markDirty();
        }
        for (Direction direction : Direction.LIST) {
            Vector3i axis = direction.axis();
            final ClientChunk chunk1 = getChunkByAbsolutePos(
                x + axis.x(),
                y + axis.y(),
                z + axis.z()
            );
            if (chunk1 != null) {
                chunk1.markDirty();
            }
        }
    }

    private ClientChunk getChunk(int x, int y, int z) {
        return chunks.get(new Vector3i(x, y, z));
    }

    private ClientChunk getChunkOrCreate(int x, int y, int z) {
        return chunks.computeIfAbsent(new Vector3i(x, y, z),
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
        for (ClientChunk chunk : chunks.values()) {
            chunk.close();
        }
        chunks.clear();
        scheduler.dispose();
        vertexBuilderPool.dispose();
        chunkGC.dispose();
    }
}
