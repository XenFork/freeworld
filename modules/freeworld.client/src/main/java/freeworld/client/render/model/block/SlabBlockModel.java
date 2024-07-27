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
import freeworld.core.Identifier;
import freeworld.math.Vector2f;
import freeworld.math.Vector3f;
import freeworld.util.Direction;

import java.util.List;
import java.util.Map;

import static freeworld.client.render.model.TextureKeys.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class SlabBlockModel implements BlockModel {
    private static final List<BlockModelPart> LIST = List.of(new BlockModelPart(
        Vector3f.ZERO,
        new Vector3f(1.0f, 0.5f, 1.0f),
        new MapBuilder<Direction, BlockModelFace>()
            .entry(Direction.WEST, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE, Direction.WEST))
            .entry(Direction.EAST, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE, Direction.EAST))
            .entry(Direction.DOWN, new BlockModelFace(Vector2f.ZERO, new Vector2f(1.0f), BOTTOM, Direction.DOWN))
            .entry(Direction.UP, new BlockModelFace(Vector2f.ZERO, new Vector2f(1.0f), TOP, null))
            .entry(Direction.NORTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE, Direction.NORTH))
            .entry(Direction.SOUTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE, Direction.SOUTH))
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
