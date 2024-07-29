/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.util.math;

import freeworld.util.Direction;

/**
 * @author squid233
 * @since 0.1.0
 */
public record HitResult(
    boolean missed,
    double distance,
    Direction face
) {
    public static final HitResult MISSED = new HitResult(true, 0.0, Direction.SOUTH);
}
