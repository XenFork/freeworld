/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.entity.component;

import freeworld.core.Identifier;
import freeworld.core.math.AABBox;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class EntityComponentKeys {
    private static final Supplier<Vector3d> zeroVec3 = () -> Vector3d.ZERO;
    public static final EntityComponentKey<Vector3d> ACCELERATION = of("acceleration", zeroVec3);
    public static final EntityComponentKey<AABBox> BOUNDING_BOX = of("bounding_box", () -> AABBox.EMPTY);
    public static final EntityComponentKey<Double> EYE_HEIGHT = of("eye_height", () -> 0.5);
    public static final EntityComponentKey<OnGroundComponent> ON_GROUND = of("on_ground", () -> OnGroundComponent.INSTANCE);
    public static final EntityComponentKey<Vector3d> POSITION = of("position", zeroVec3);
    public static final EntityComponentKey<Vector2d> ROTATION = of("rotation", () -> Vector2d.ZERO);
    public static final EntityComponentKey<Vector3d> VELOCITY = of("velocity", zeroVec3);

    private EntityComponentKeys() {
    }

    private static <T> EntityComponentKey<T> of(String name, Supplier<T> defaultValue) {
        return new EntityComponentKey<>(Identifier.ofBuiltin(name), defaultValue);
    }
}
