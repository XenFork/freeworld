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
import freeworld.core.ModelResourcePath;
import freeworld.math.Vector2f;
import freeworld.math.Vector3f;
import freeworld.util.Direction;

import java.util.List;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class SlabBlockModel implements BlockModel {
    private static final Identifier TOP = Identifier.ofBuiltin("top");
    private static final Identifier SIDE = Identifier.ofBuiltin("side");
    private static final Identifier BOTTOM = Identifier.ofBuiltin("bottom");
    private static final ModelResourcePath TOP_MRP = new ModelResourcePath(ModelResourcePath.Type.VARIABLE, TOP);
    private static final ModelResourcePath SIDE_MRP = new ModelResourcePath(ModelResourcePath.Type.VARIABLE, SIDE);
    private static final ModelResourcePath BOTTOM_MRP = new ModelResourcePath(ModelResourcePath.Type.VARIABLE, BOTTOM);
    private static final List<BlockModelPart> LIST = List.of(new BlockModelPart(
        Vector3f.ZERO,
        new Vector3f(1.0f, 0.5f, 1.0f),
        new MapBuilder<Direction, BlockModelFace>()
            .entry(Direction.WEST, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE_MRP, Direction.WEST))
            .entry(Direction.EAST, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE_MRP, Direction.EAST))
            .entry(Direction.DOWN, new BlockModelFace(Vector2f.ZERO, new Vector2f(1.0f), BOTTOM_MRP, Direction.DOWN))
            .entry(Direction.UP, new BlockModelFace(Vector2f.ZERO, new Vector2f(1.0f), TOP_MRP, null))
            .entry(Direction.NORTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE_MRP, Direction.NORTH))
            .entry(Direction.SOUTH, new BlockModelFace(new Vector2f(0.0f, 0.5f), new Vector2f(1.0f), SIDE_MRP, Direction.SOUTH))
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
