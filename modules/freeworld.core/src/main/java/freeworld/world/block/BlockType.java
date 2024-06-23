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

    public boolean air() {
        return air;
    }

    public AABBox outlineShape() {
        return AABBox.FULL_CUBE;
    }

    public AABBox collisionShape() {
        return AABBox.FULL_CUBE;
    }
}
