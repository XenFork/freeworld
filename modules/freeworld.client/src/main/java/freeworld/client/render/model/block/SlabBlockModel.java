/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.model.block;

import freeworld.client.util.MapBuilder;
import freeworld.util.Identifier;
import freeworld.math.Vector2f;
import freeworld.math.Vector3f;
import freeworld.util.Direction;

import java.util.List;
import java.util.Map;

import static freeworld.client.render.model.TextureKeys.*;
import static freeworld.math.Vector2f.*;
import static freeworld.util.Direction.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class SlabBlockModel implements BlockModel {
    private static final List<BlockModelPart> LIST = List.of(new BlockModelPart(
        Vector3f.ZERO,
        new Vector3f(1.0f, 0.5f, 1.0f),
        new MapBuilder<Direction, BlockModelFace>()
            .entry(WEST, new BlockModelFace(new Vector2f(0.0f, 0.5f), ONE, SIDE, WEST))
            .entry(EAST, new BlockModelFace(new Vector2f(0.0f, 0.5f), ONE, SIDE, EAST))
            .entry(DOWN, new BlockModelFace(ZERO, ONE, BOTTOM, DOWN))
            .entry(UP, new BlockModelFace(ZERO, ONE, TOP, null))
            .entry(NORTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), ONE, SIDE, NORTH))
            .entry(SOUTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), ONE, SIDE, SOUTH))
            .build()
    ));
    private final Map<Identifier, Identifier> textureDef;

    public SlabBlockModel(Identifier topTexture, Identifier sideTexture, Identifier bottomTexture) {
        this.textureDef = Map.of(TOP, topTexture, SIDE, sideTexture, BOTTOM, bottomTexture);
    }

    @Override
    public Map<Identifier, Identifier> textureDefinitions() {
        return textureDef;
    }

    @Override
    public List<BlockModelPart> parts() {
        return LIST;
    }
}
