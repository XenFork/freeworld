/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.chunk;

import freeworld.world.World;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;

import java.util.Arrays;
import java.util.StringJoiner;

/**
 * @author baka4n
 * @author squid233
 * @since 0.1.0
 */
public class Chunk {
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
    private final BlockType[] blocks;

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
        this.blocks = new BlockType[width * height * depth];
        Arrays.fill(blocks, BlockTypes.AIR);
    }

    public void generateTerrain() {
        for (int bx = 0; bx < width; bx++) {
            for (int bz = 0; bz < depth; bz++) {
                for (int by = 0; by < height; by++) {
                    final int absY = ChunkPos.relativeToAbsolute(y, by);
                    if (absY < 5) {
                        setBlockType(bx, by, bz, BlockTypes.STONE);
                    } else if (absY < 8) {
                        setBlockType(bx, by, bz, BlockTypes.DIRT);
                    } else if (absY == 8) {
                        setBlockType(bx, by, bz, BlockTypes.GRASS_BLOCK);
                    }
                }
            }
        }
    }

    public boolean isInBound(int x, int y, int z) {
        return x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth;
    }

    public void setBlockType(int x, int y, int z, BlockType blockType) {
        if (isInBound(x, y, z)) {
            blocks[(y * depth + z) * width + x] = blockType;
        }
    }

    public BlockType getBlockType(int x, int y, int z) {
        if (isInBound(x, y, z)) {
            return blocks[(y * depth + z) * width + x];
        }
        return BlockTypes.AIR;
    }

    public void markDirty() {
    }

    public void copyFrom(Chunk chunk) {
        System.arraycopy(chunk.blocks, 0, blocks, 0, blocks.length);
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
        return new StringJoiner(", ", Chunk.class.getSimpleName() + "[", "]")
            .add("world=" + world)
            .add("x=" + x)
            .add("y=" + y)
            .add("z=" + z)
            .toString();
    }
}
