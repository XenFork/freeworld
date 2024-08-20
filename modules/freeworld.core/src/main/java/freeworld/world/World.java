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

import freeworld.math.Vector3d;
import freeworld.math.Vector3i;
import freeworld.util.Int3Consumer;
import freeworld.util.math.ChunkPos;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.chunk.Chunk;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.player.PlayerEntity;
import freeworld.world.entity.system.MotionSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    public static final int TICKING_RADIUS = 5;
    public final Map<Vector3i, Chunk> chunks = new ConcurrentHashMap<>(2048);
    private final List<PlayerEntity> players = new ArrayList<>();
    private final MotionSystem motionSystem = new MotionSystem();
    private final List<WorldListener> listeners = new ArrayList<>();
    private final long seed;

    public World(String name, long seed) {
        this.seed = seed;
    }

    public static void forChunksInRange(Entity player, int chunkRadius, Int3Consumer consumer) {
        Vector3i chunkPos = player.chunkPos();
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
        for (PlayerEntity player : players) {
            forChunksInRange(player, TICKING_RADIUS, (x, y, z) -> getOrCreateChunk(x, y, z).tick());
        }
    }

    public <T extends Entity> T createEntity(EntityType<T> type, Vector3d position) {
        final T entity = type.factory().create(this);
        entity.setPosition(position);
        if (entity instanceof PlayerEntity player) {
            players.add(player);
        } else {
            Vector3i chunkPos = entity.chunkPos();
            getOrCreateChunk(chunkPos.x(), chunkPos.y(), chunkPos.z()).addEntity(entity);
        }
        return entity;
    }

    public boolean isChunkLoaded(int x, int y, int z) {
        return chunks.containsKey(new Vector3i(x, y, z));
    }

    public boolean isBlockLoaded(int x, int y, int z) {
        return isChunkLoaded(
            ChunkPos.toChunkPos(x),
            ChunkPos.toChunkPos(y),
            ChunkPos.toChunkPos(z)
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
            ChunkPos.toChunkPos(x),
            ChunkPos.toChunkPos(y),
            ChunkPos.toChunkPos(z)
        );
    }

    public BlockType getBlock(int x, int y, int z) {
        final Chunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            return chunk.getBlockType(
                ChunkPos.toBlockPosInChunk(x),
                ChunkPos.toBlockPosInChunk(y),
                ChunkPos.toBlockPosInChunk(z)
            );
        }
        return BlockTypes.AIR;
    }

    public BlockType getBlock(Vector3i pos) {
        return getBlock(pos.x(), pos.y(), pos.z());
    }

    public void setBlock(int x, int y, int z, BlockType blockType) {
        final Chunk chunk = getChunkByAbsolutePos(x, y, z);
        if (chunk != null) {
            chunk.setBlockType(
                ChunkPos.toBlockPosInChunk(x),
                ChunkPos.toBlockPosInChunk(y),
                ChunkPos.toBlockPosInChunk(z),
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

    public MotionSystem motionSystem() {
        return motionSystem;
    }
}
