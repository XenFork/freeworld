/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world;

import freeworld.core.math.AABBox;
import freeworld.math.Vector3d;
import freeworld.util.Int3Consumer;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.chunk.Chunk;
import freeworld.world.chunk.ChunkPos;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.component.PositionComponent;
import freeworld.world.entity.system.MotionSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    public static final int TICKING_RADIUS = 5;
    public static final int TICKING_CHUNK_COUNT_CBRT = TICKING_RADIUS * 2 + 1;
    public static final int TICKING_CHUNK_COUNT = TICKING_CHUNK_COUNT_CBRT * TICKING_CHUNK_COUNT_CBRT * TICKING_CHUNK_COUNT_CBRT;
    public final Map<ChunkPos, Chunk> chunks = new ConcurrentHashMap<>(TICKING_CHUNK_COUNT);
    private final List<Entity> entities = new ArrayList<>();
    private final MotionSystem motionSystem = new MotionSystem();
    private final List<WorldListener> listeners = new ArrayList<>();

    public World(String name) {
    }

    public static void forEachChunk(Entity player, int chunkRadius, Int3Consumer consumer) {
        final int radius = chunkRadius * Chunk.SIZE;
        final AABBox box = player.boundingBox().value().grow(radius, radius, radius);
        final int minX = ChunkPos.absoluteToChunk((int) Math.floor(box.minX()));
        final int minY = ChunkPos.absoluteToChunk((int) Math.floor(box.minY()));
        final int minZ = ChunkPos.absoluteToChunk((int) Math.floor(box.minZ()));
        final int maxX = ChunkPos.absoluteToChunk((int) Math.ceil(box.maxX())) + 1;
        final int maxY = ChunkPos.absoluteToChunk((int) Math.ceil(box.maxY())) + 1;
        final int maxZ = ChunkPos.absoluteToChunk((int) Math.ceil(box.maxZ())) + 1;
        for (int x = minX; x < maxX; x++) {
            for (int y = minY; y < maxY; y++) {
                for (int z = minZ; z < maxZ; z++) {
                    consumer.accept(x, y, z);
                }
            }
        }
    }

    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    public void tick() {
        motionSystem.process(this, entities);
    }

    public Entity createEntity(EntityType type, double x, double y, double z) {
        final Entity entity = new Entity(this, UUID.randomUUID(), type);
        if (entity.hasComponent(PositionComponent.ID)) {
            entity.setComponent(new PositionComponent(new Vector3d(x, y, z)));
        }
        entities.add(entity);
        return entity;
    }

    public boolean isChunkLoaded(int x, int y, int z) {
        return chunks.containsKey(new ChunkPos(x, y, z));
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return isChunkLoaded(
            ChunkPos.absoluteToChunk(x),
            ChunkPos.absoluteToChunk(y),
            ChunkPos.absoluteToChunk(z)
        );
    }

    public Chunk getOrCreateChunk(int x, int y, int z) {
        return chunks.computeIfAbsent(
            new ChunkPos(x, y, z),
            chunkPos -> {
                final Chunk chunk = new Chunk(this, chunkPos.x(), chunkPos.y(), chunkPos.z());
                chunk.generateTerrain();
                return chunk;
            }
        );
    }

    public Chunk getChunk(int x, int y, int z) {
        return chunks.get(new ChunkPos(x, y, z));
    }

    public Chunk getChunkByAbsolutePos(int x, int y, int z) {
        return getChunk(
            ChunkPos.absoluteToChunk(x),
            ChunkPos.absoluteToChunk(y),
            ChunkPos.absoluteToChunk(z)
        );
    }

    public BlockType getBlockType(int x, int y, int z) {
        final Chunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            return chunk.getBlockType(
                ChunkPos.absoluteToRelative(x),
                ChunkPos.absoluteToRelative(y),
                ChunkPos.absoluteToRelative(z)
            );
        }
        return BlockTypes.AIR;
    }

    public void setBlockType(int x, int y, int z, BlockType blockType) {
        final Chunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            chunk.setBlockType(
                ChunkPos.absoluteToRelative(x),
                ChunkPos.absoluteToRelative(y),
                ChunkPos.absoluteToRelative(z),
                blockType
            );
            for (WorldListener listener : listeners) {
                listener.onBlockChanged(x, y, z);
            }
        }
    }
}
