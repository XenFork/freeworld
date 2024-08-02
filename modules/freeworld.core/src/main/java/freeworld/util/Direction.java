/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util;

import freeworld.math.Vector3i;
import freeworld.math.Vector4i;

import java.util.List;

import static freeworld.util.math.AABBox.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum Direction {
    WEST(0, 1, new Vector3i(-1, 0, 0), new Vector4i(NX_PY_NZ, NX_NY_NZ, NX_NY_PZ, NX_PY_PZ)),
    EAST(1, 0, new Vector3i(1, 0, 0), new Vector4i(PX_PY_PZ, PX_NY_PZ, PX_NY_NZ, PX_PY_NZ)),
    DOWN(2, 3, new Vector3i(0, -1, 0), new Vector4i(NX_NY_PZ, NX_NY_NZ, PX_NY_NZ, PX_NY_PZ)),
    UP(3, 2, new Vector3i(0, 1, 0), new Vector4i(NX_PY_NZ, NX_PY_PZ, PX_PY_PZ, PX_PY_NZ)),
    NORTH(4, 5, new Vector3i(0, 0, -1), new Vector4i(PX_PY_NZ, PX_NY_NZ, NX_NY_NZ, NX_PY_NZ)),
    SOUTH(5, 4, new Vector3i(0, 0, 1), new Vector4i(NX_PY_PZ, NX_NY_PZ, PX_NY_PZ, PX_PY_PZ));

    public static final List<Direction> LIST = List.of(values());
    private final int id;
    private final int oppositeId;
    private final Vector3i axis;
    private final Vector4i vertexIndices;

    Direction(int id, int oppositeId, Vector3i axis, Vector4i vertexIndices) {
        this.id = id;
        this.oppositeId = oppositeId;
        this.axis = axis;
        this.vertexIndices = vertexIndices;
    }

    public static Direction fromId(int id) {
        return switch (id) {
            case 0 -> WEST;
            case 1 -> EAST;
            case 2 -> DOWN;
            case 3 -> UP;
            case 4 -> NORTH;
            case 5 -> SOUTH;
            default -> throw new IllegalArgumentException("Invalid id for direction: " + id);
        };
    }

    public Direction opposite() {
        return fromId(oppositeId());
    }

    public int id() {
        return id;
    }

    public int oppositeId() {
        return oppositeId;
    }

    public Vector3i axis() {
        return axis;
    }

    public Vector4i vertexIndices() {
        return vertexIndices;
    }
}
