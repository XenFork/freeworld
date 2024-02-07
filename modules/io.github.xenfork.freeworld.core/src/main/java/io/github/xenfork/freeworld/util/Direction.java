/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.util;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum Direction {
    WEST(0, 1, -1, 0, 0),
    EAST(1, 0, 1, 0, 0),
    DOWN(2, 3, 0, -1, 0),
    UP(3, 2, 0, 1, 0),
    NORTH(4, 5, 0, 0, -1),
    SOUTH(5, 4, 0, 0, 1);

    private final int id;
    private final int oppositeId;
    private final int axisX;
    private final int axisY;
    private final int axisZ;

    Direction(int id, int oppositeId, int axisX, int axisY, int axisZ) {
        this.id = id;
        this.oppositeId = oppositeId;
        this.axisX = axisX;
        this.axisY = axisY;
        this.axisZ = axisZ;
    }

    public static Direction fromId(int id) {
        return switch (id) {
            case 0 -> WEST;
            case 1 -> EAST;
            case 2 -> DOWN;
            case 3 -> UP;
            case 4 -> NORTH;
            case 5 -> SOUTH;
            default -> throw new IllegalArgumentException(STR."Invalid id for direction: \{id}");
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

    public int axisX() {
        return axisX;
    }

    public int axisY() {
        return axisY;
    }

    public int axisZ() {
        return axisZ;
    }
}
