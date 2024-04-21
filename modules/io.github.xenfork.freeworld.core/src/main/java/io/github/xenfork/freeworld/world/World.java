/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world;

import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.block.BlockTypes;
import io.github.xenfork.freeworld.world.chunk.Chunk;
import io.github.xenfork.freeworld.world.chunk.ChunkPos;
import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.EntityType;
import io.github.xenfork.freeworld.world.entity.component.PositionComponent;
import io.github.xenfork.freeworld.world.entity.system.MotionSystem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    private final int width = 256;
    private final int height = 64;
    private final int depth = 256;
    public final int xChunks = width / Chunk.SIZE;
    public final int yChunks = height / Chunk.SIZE;
    public final int zChunks = depth / Chunk.SIZE;
    public final Chunk[] chunks = new Chunk[xChunks * yChunks * zChunks];
    private final List<Entity> entities = new ArrayList<>();
    private final MotionSystem motionSystem = new MotionSystem();
    private final List<WorldListener> listeners = new ArrayList<>();

    public World(String name) {
        for (int x = 0; x < xChunks; x++) {
            for (int y = 0; y < yChunks; y++) {
                for (int z = 0; z < zChunks; z++) {
                    final Chunk chunk = new Chunk(this, x, y, z);
                    chunks[(y * zChunks + z) * xChunks + x] = chunk;
                    chunk.generateTerrain();
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
            entity.position().value().set(x, y, z);
        }
        entities.add(entity);
        return entity;
    }

    public Chunk getChunk(int x, int y, int z) {
        if (x >= 0 && x < xChunks && y >= 0 && y < yChunks && z >= 0 && z < zChunks) {
            return chunks[(y * zChunks + z) * xChunks + x];
        }
        return null;
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

    public boolean isInBound(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public Chunk createChunk(int x, int y, int z) {
        return new Chunk(this, x, y, z);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public int depth() {
        return depth;
    }
}
