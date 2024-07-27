/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.block;

import freeworld.util.shape.VoxelShape;

/**
 * @author squid233
 * @since 0.1.0
 */
public class BlockType { // must be an identity class
    private final boolean air;

    public BlockType(Settings settings) {
        this.air = settings.air;
    }

    public static final class Settings {
        private boolean air = false;

        public Settings air() {
            this.air = true;
            return this;
        }
    }

    public static VoxelShape createCuboidShape(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return VoxelShape.cuboid(minX / 16.0, minY / 16.0, minZ / 16.0, maxX / 16.0, maxY / 16.0, maxZ / 16.0);
    }

    public boolean air() {
        return air;
    }

    public boolean hasSidedTransparency() {
        return false;
    }

    public VoxelShape outlineShape() {
        return VoxelShape.fullCube();
    }

    public VoxelShape collisionShape() {
        return outlineShape();
    }
}
