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

import freeworld.core.Identifier;
import freeworld.core.ModelResourcePath;
import freeworld.math.Vector2f;
import freeworld.math.Vector3f;
import freeworld.util.Direction;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class CubeAllBlockModel implements BlockModel {
    private static final Identifier ALL = Identifier.ofBuiltin("all");
    private static final ModelResourcePath MRP = new ModelResourcePath(ModelResourcePath.Type.VARIABLE, ALL);
    private static final List<BlockModelPart> LIST;
    private final Map<Identifier, Identifier> textureDef;

    public CubeAllBlockModel(Identifier texture) {
        this.textureDef = Map.of(ALL, texture);
    }

    static {
        final Map<Direction, BlockModelFace> map = HashMap.newHashMap(6);
        for (Direction direction : Direction.LIST) {
            map.put(direction, new BlockModelFace(Vector2f.ZERO, new Vector2f(1.0f), MRP, direction));
        }
        LIST = List.of(new BlockModelPart(Vector3f.ZERO, new Vector3f(1.0f), map));
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
