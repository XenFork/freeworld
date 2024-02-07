/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.chunk;

import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.core.registry.BuiltinRegistries;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockState;
import io.github.xenfork.freeworld.world.block.BlockTypes;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author baka4n
 * @author squid233
 * @since 0.1.0
 */
public class Chunk {
    public static final int CHUNK_VERSION = 0;
    public static final int SIZE = 32;
    private final World world;
    private final int x;
    private final int y;
    private final int z;
    private final int fromX;
    private final int fromY;
    private final int fromZ;
    private final int toX;
    private final int toY;
    private final int toZ;
    private final int width;
    private final int height;
    private final int depth;
    private final BlockState[] states;

    public Chunk(World world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.fromX = x * SIZE;
        this.fromY = y * SIZE;
        this.fromZ = z * SIZE;
        this.toX = fromX + SIZE;
        this.toY = fromY + SIZE;
        this.toZ = fromZ + SIZE;
        this.width = toX - fromX;
        this.height = toY - fromY;
        this.depth = toZ - fromZ;
        this.states = new BlockState[width * height * depth];
        Arrays.fill(states, BlockTypes.AIR.defaultBlockState());
    }

    public void loadFromFile(Path path) {
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            final int version = stream.readInt();
            if (version != 0) {
                throw new IllegalStateException(STR."Invalid version: \{version}");
            }
            final int width = stream.readInt();
            final int height = stream.readInt();
            final int depth = stream.readInt();

            // registry
            final int registrySize = stream.readInt();
            final Map<Integer, Identifier> blockIdMap = HashMap.newHashMap(registrySize);
            for (int i = 0; i < registrySize; i++) {
                final Identifier identifier = Identifier.ofSafe(stream.readUTF());
                if (identifier == null) {
                    continue;
                }
                blockIdMap.put(stream.readInt(), identifier);
            }

            // block state
            for (int bx = 0; bx < width; bx++) {
                for (int by = 0; by < height; by++) {
                    for (int bz = 0; bz < depth; bz++) {
                        setBlockState(bx, by, bz, BuiltinRegistries.BLOCK_TYPE.get(blockIdMap.get(stream.readInt())).defaultBlockState());
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveToFile(Path path) {
        try (DataOutputStream stream = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(path)))) {
            stream.writeInt(CHUNK_VERSION);
            stream.writeInt(width);
            stream.writeInt(height);
            stream.writeInt(depth);

            // registry
            final Map<Identifier, Integer> blockIdMap = new HashMap<>();
            int blockId = 0;
            for (BlockState state : states) {
                final Identifier id = BuiltinRegistries.BLOCK_TYPE.getId(state.blockType());
                if (!blockIdMap.containsKey(id)) {
                    blockIdMap.put(id, blockId);
                    blockId++;
                }
            }
            stream.writeInt(blockIdMap.size());
            for (var entry : blockIdMap.entrySet()) {
                stream.writeUTF(entry.getKey().toString());
                stream.writeInt(entry.getValue());
            }

            // block states
            for (BlockState state : states) {
                stream.writeInt(blockIdMap.get(BuiltinRegistries.BLOCK_TYPE.getId(state.blockType())));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void generateTerrain() {
        for (int bx = 0; bx < width; bx++) {
            for (int bz = 0; bz < depth; bz++) {
                for (int by = 0; by < 8; by++) {
                    setBlockState(bx, by, bz, BlockTypes.STONE.defaultBlockState());
                }
                for (int by = 8; by < 11; by++) {
                    setBlockState(bx, by, bz, BlockTypes.DIRT.defaultBlockState());
                }
                setBlockState(bx, 11, bz, BlockTypes.GRASS_BLOCK.defaultBlockState());
            }
        }
    }

    public void setBlockState(int x, int y, int z, BlockState blockState) {
        states[(y * depth + z) * width + x] = blockState;
    }

    public BlockState getBlockState(int x, int y, int z) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            return states[(y * depth + z) * width + x];
        }
        return BlockTypes.AIR.defaultBlockState();
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    public int fromX() {
        return fromX;
    }

    public int fromY() {
        return fromY;
    }

    public int fromZ() {
        return fromZ;
    }

    public int toX() {
        return toX;
    }

    public int toY() {
        return toY;
    }

    public int toZ() {
        return toZ;
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

    @Override
    public String toString() {
        return STR."Chunk[world=\{world},x=\{x},y=\{y},z=\{z}]";
    }
}
