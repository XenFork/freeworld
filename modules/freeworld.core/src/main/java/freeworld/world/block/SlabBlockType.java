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
public class SlabBlockType extends BlockType {
    private static final VoxelShape SHAPE = createCuboidShape(0.0, 0.0, 0.0, 16.0, 8.0, 16.0);

    public SlabBlockType(Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasSidedTransparency() {
        return true;
    }

    @Override
    public VoxelShape outlineShape() {
        return SHAPE;
    }
}
