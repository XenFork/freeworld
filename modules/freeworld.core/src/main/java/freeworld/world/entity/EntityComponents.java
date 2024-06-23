/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity;

import freeworld.core.Identifier;
import freeworld.core.math.AABBox;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.world.component.ComponentKey;

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityComponents {
    private static final Supplier<Vector3d> zeroVec3 = () -> Vector3d.ZERO;
    public static final ComponentKey<Vector3d> ACCELERATION = of("acceleration", zeroVec3);
    public static final ComponentKey<AABBox> BOUNDING_BOX = of("bounding_box", () -> AABBox.EMPTY);
    public static final ComponentKey<Double> EYE_HEIGHT = of("eye_height", () -> 0.5);
    public static final ComponentKey<Object> ON_GROUND = of("on_ground", () -> Object.class);
    public static final ComponentKey<Vector3d> POSITION = of("position", zeroVec3);
    public static final ComponentKey<Vector2d> ROTATION = of("rotation", () -> Vector2d.ZERO);
    public static final ComponentKey<Vector3d> VELOCITY = of("velocity", zeroVec3);

    private EntityComponents() {
    }

    private static <T> ComponentKey<T> of(String name, Supplier<T> defaultValue) {
        return new ComponentKey<>(Identifier.ofBuiltin(name), defaultValue);
    }
}
