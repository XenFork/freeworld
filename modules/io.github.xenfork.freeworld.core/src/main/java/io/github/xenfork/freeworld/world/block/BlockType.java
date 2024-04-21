/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block;

import io.github.xenfork.freeworld.core.math.AABBox;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockType {
    private final boolean air;
    private final AABBox outlineShape;
    private final AABBox collisionShape;

    private BlockType(boolean air, AABBox outlineShape, AABBox collisionShape) {
        this.air = air;
        this.outlineShape = outlineShape;
        this.collisionShape = collisionShape;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private boolean air = false;
        private AABBox outlineShape = AABBox.FULL_CUBE;
        private AABBox collisionShape = AABBox.FULL_CUBE;

        public Builder air() {
            this.air = true;
            return this;
        }

        public Builder outlineShape(AABBox outlineShape) {
            this.outlineShape = outlineShape;
            return this;
        }

        public Builder collisionShape(AABBox collisionShape) {
            this.collisionShape = collisionShape;
            return this;
        }

        public BlockType build() {
            return new BlockType(air, outlineShape, collisionShape);
        }
    }

    public boolean air() {
        return air;
    }

    public AABBox outlineShape() {
        return outlineShape;
    }

    public AABBox collisionShape() {
        return collisionShape;
    }
}
