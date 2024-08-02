/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.model.block;

import freeworld.util.MapBuilder;
import freeworld.math.Vector3f;
import freeworld.util.Direction;
import freeworld.util.Identifier;

import java.util.List;
import java.util.Map;

import static freeworld.client.render.model.TextureKeys.*;
import static freeworld.math.Vector2f.ONE;
import static freeworld.math.Vector2f.ZERO;
import static freeworld.util.Direction.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class LogBlockModel implements BlockModel {
    private static final List<BlockModelPart> LIST = List.of(
        new BlockModelPart(
            Vector3f.ZERO,
            Vector3f.ONE,
            new MapBuilder<Direction, BlockModelFace>()
                .add(WEST, new BlockModelFace(ZERO, ONE, SIDE, WEST))
                .add(EAST, new BlockModelFace(ZERO, ONE, SIDE, EAST))
                .add(DOWN, new BlockModelFace(ZERO, ONE, TOP, DOWN))
                .add(UP, new BlockModelFace(ZERO, ONE, TOP, UP))
                .add(NORTH, new BlockModelFace(ZERO, ONE, SIDE, NORTH))
                .add(SOUTH, new BlockModelFace(ZERO, ONE, SIDE, SOUTH))
                .build()
        )
    );
    private final Map<Identifier, Identifier> map;

    public LogBlockModel(Identifier topTexture, Identifier sideTexture) {
        this.map = Map.of(TOP, topTexture, SIDE, sideTexture);
    }

    @Override
    public Map<Identifier, Identifier> textureDefinitions() {
        return map;
    }

    @Override
    public List<BlockModelPart> parts() {
        return LIST;
    }
}
