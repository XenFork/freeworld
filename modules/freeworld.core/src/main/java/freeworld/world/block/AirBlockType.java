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

import freeworld.core.math.AABBox;

/**
 * @author squid233
 * @since 0.1.0
 */
public class AirBlockType extends BlockType {
    public AirBlockType(Settings settings) {
        super(settings);
    }

    @Override
    public AABBox outlineShape() {
        return AABBox.EMPTY;
    }

    @Override
    public AABBox collisionShape() {
        return AABBox.EMPTY;
    }
}
