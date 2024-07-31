/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world;

import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.math.Vector3i;
import freeworld.util.Int3Consumer;
import freeworld.util.math.ChunkPos;
import freeworld.util.math.MathUtil;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.chunk.Chunk;
import freeworld.world.entity.CubeEntity;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.PlayerEntity;
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
    public final Map<Vector3i, Chunk> chunks = new ConcurrentHashMap<>(2048);
    private final List<Entity> entities = new ArrayList<>();
    private final List<PlayerEntity> players = new ArrayList<>();
    private final MotionSystem motionSystem = new MotionSystem();
    private final List<WorldListener> listeners = new ArrayList<>();
    private final long seed;

    public World(String name, long seed) {
        this.seed = seed;
    }

    public static void forInChunkRange(Entity player, int chunkRadius, Int3Consumer consumer) {
        Vector3i chunkPos = ChunkPos.absoluteToChunk(player.position().toVector3iFloor());
        int minX = chunkPos.x() - chunkRadius;
        int maxX = chunkPos.x() + chunkRadius;
        int minY = chunkPos.y() - chunkRadius;
        int maxY = chunkPos.y() + chunkRadius;
        int minZ = chunkPos.z() - chunkRadius;
        int maxZ = chunkPos.z() + chunkRadius;
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    consumer.accept(x, y, z);
                }
            }
        }
    }

    public void addListener(WorldListener listener) {
        listeners.add(listener);
    }

    public void tick() {
        motionSystem.process(this, players);
        motionSystem.process(this, entities);
        // TODO: test
        for (Entity entity : entities) {
            if (entity instanceof CubeEntity cubeEntity) {
                cubeEntity.rotation = new Vector2d(0.0, cubeEntity.rotation().y() + Math.random() * 2 - 1);
                cubeEntity.acceleration = MathUtil.moveRelative(
                    0.0, cubeEntity.onGround() && Math.random() > 0.5 ? 0.5 : 0.0, 1.0,
                    cubeEntity.rotation().y(),
                    cubeEntity.onGround() ? 0.1 : 0.02
                );
            }
        }
    }

    public <T extends Entity> T createEntity(EntityType<T> type, Vector3d position) {
        final T entity = type.factory().create(this, UUID.randomUUID());
        entity.init(position);
        if (entity instanceof PlayerEntity player) {
            players.add(player);
        } else {
            entities.add(entity);
        }
        return entity;
    }

    public boolean isChunkLoaded(int x, int y, int z) {
        return chunks.containsKey(new Vector3i(x, y, z));
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
            new Vector3i(x, y, z),
            chunkPos -> {
                final Chunk chunk = new Chunk(this, chunkPos.x(), chunkPos.y(), chunkPos.z());
                chunk.generateTerrain();
                return chunk;
            }
        );
    }

    public Chunk getChunk(int x, int y, int z) {
        return chunks.get(new Vector3i(x, y, z));
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

    public long seed() {
        return seed;
    }

    public List<Entity> entities() {
        return entities;
    }
}
