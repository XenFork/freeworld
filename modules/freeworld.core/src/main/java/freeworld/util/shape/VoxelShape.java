/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.util.shape;

import freeworld.math.Vector3d;
import freeworld.util.Direction;
import freeworld.util.math.AABBox;
import freeworld.util.math.HitResult;
import freeworld.util.math.Lined;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VoxelShape {
    static VoxelShape empty() {
        return EmptyVoxelShape.INSTANCE;
    }

    static VoxelShape fullCube() {
        return SingleVoxelShape.FULL_CUBE;
    }

    HitResult rayCast(Vector3d origin, Vector3d dir);

    List<AABBox> toBoxes();

    List<Lined> toLines(List<Direction> directions);
}
