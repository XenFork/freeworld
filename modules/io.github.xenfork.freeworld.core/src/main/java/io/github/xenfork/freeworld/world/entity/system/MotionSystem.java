/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.entity.system;

import io.github.xenfork.freeworld.util.MathUtil;
import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.component.PositionComponent;
import io.github.xenfork.freeworld.world.entity.component.RotationXYComponent;
import io.github.xenfork.freeworld.world.entity.component.VelocityComponent;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MotionSystem implements EntitySystem {
    private final Vector3d movement = new Vector3d();

    @Override
    public void process(List<Entity> entities) {
        for (Entity entity : entities) {
            if (EntitySystem.filter(entity, PositionComponent.ID, RotationXYComponent.ID, VelocityComponent.ID)) {
                final Vector3d position = entity.position().position();
                final Vector2d rotation = entity.rotation().rotation();
                final Vector3d velocity = entity.velocity().velocity();
                MathUtil.moveRelative(velocity.x(), velocity.y(), velocity.z(), rotation.y(), movement);
                position.add(movement);
                velocity.mul(0.5, 0.5, 0.5);
            }
        }
    }
}
