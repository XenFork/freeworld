/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.block;

import freeworld.core.Identifier;
import freeworld.core.math.AABBox;

/**
 * @author squid233
 * @since 0.1.0
 */
public record BlockType(
    boolean air,
    AABBox outlineShape,
    AABBox collisionShape,
    Identifier textureId
) {
    public BlockType() {
        this(false, AABBox.FULL_CUBE, AABBox.FULL_CUBE, Identifier.ofBuiltin(""));
    }

    public BlockType withAir(boolean air) {
        return new BlockType(air, outlineShape, collisionShape, textureId);
    }

    public BlockType withOutlineShape(AABBox outlineShape) {
        return new BlockType(air, outlineShape, collisionShape, textureId);
    }

    public BlockType withCollisionShape(AABBox collisionShape) {
        return new BlockType(air, outlineShape, collisionShape, textureId);
    }

    public BlockType withTextureId(Identifier textureId) {
        return new BlockType(air, outlineShape, collisionShape, textureId);
    }
}
