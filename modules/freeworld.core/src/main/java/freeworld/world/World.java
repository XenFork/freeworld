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

import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;
import freeworld.world.chunk.Chunk;
import freeworld.world.chunk.ChunkPos;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityType;
import freeworld.world.entity.component.PositionComponent;
import freeworld.world.entity.system.MotionSystem;

import java.util.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    public final Map<ChunkPos, Chunk> chunks = HashMap.newHashMap(5 * 5 * 5);
    private final List<Entity> entities = new ArrayList<>();
    private final MotionSystem motionSystem = new MotionSystem();
    private final List<WorldListener> listeners = new ArrayList<>();

    public World(String name) {
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
            entity.position().value().set(x, y, z);
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
