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

import freeworld.math.Vector3f;
import freeworld.util.Direction;

import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public record BlockModelPart(Vector3f from, Vector3f to, Map<Direction, BlockModelFace> faces) {
    public BlockModelPart {
        faces = Map.copyOf(faces);
    }
}
