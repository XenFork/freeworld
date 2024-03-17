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

import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.block.BlockTypes;

import java.util.Arrays;

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
        if (y != 0) return;
        for (int bx = 0; bx < width; bx++) {
            for (int bz = 0; bz < depth; bz++) {
                for (int by = 0; by < 8; by++) {
                    setBlockState(bx, by, bz, BlockTypes.STONE);
                }
                for (int by = 8; by < 11; by++) {
                    setBlockState(bx, by, bz, BlockTypes.DIRT);
                }
                setBlockState(bx, 11, bz, BlockTypes.GRASS_BLOCK);
            }
        }
    }

    public void setBlockState(int x, int y, int z, BlockType blockType) {
        blocks[(y * depth + z) * width + x] = blockType;
    }

    public BlockType getBlockType(int x, int y, int z) {
        if (x >= 0 && x < width && y >= 0 && y < height && z >= 0 && z < depth) {
            return blocks[(y * depth + z) * width + x];
        }
        return BlockTypes.AIR;
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
