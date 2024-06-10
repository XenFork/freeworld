/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render;

import freeworld.math.Matrix4f;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.world.entity.Entity;
import freeworld.world.entity.component.EyeHeightComponent;
import freeworld.world.entity.component.PositionComponent;
import freeworld.world.entity.component.RotationXYComponent;
import freeworld.world.entity.system.EntitySystem;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private Vector3d prevPosition = Vector3d.ZERO;
    private Vector3d position = Vector3d.ZERO;
    private Vector3d lerpPosition = Vector3d.ZERO;
    private Vector2d rotation = Vector2d.ZERO;

    public void moveToEntity(Entity entity) {
        if (EntitySystem.hasAllComponents(entity, PositionComponent.ID, RotationXYComponent.ID)) {
            position = entity.position().value();
            if (entity.hasComponent(EyeHeightComponent.ID)) {
                position = new Vector3d(
                    position.x(),
                    position.y() + entity.eyeHeight().value(),
                    position.z()
                );
            }
            rotation = entity.rotation().value();
        }
    }

    public void preUpdate() {
        prevPosition = position;
    }

    public void updateLerp(double partialTick) {
        lerpPosition = prevPosition.lerp(position, partialTick);
    }

    public Matrix4f updateViewMatrix() {
        return Matrix4f.rotationX((float) -Math.toRadians(rotation.x()))
            .rotateY((float) -Math.toRadians(rotation.y()))
            .translate((float) -lerpPosition.x(), (float) -lerpPosition.y(), (float) -lerpPosition.z());
    }

    public Vector3d prevPosition() {
        return prevPosition;
    }

    public Vector3d position() {
        return position;
    }

    public Vector3d lerpPosition() {
        return lerpPosition;
    }

    public Vector2d rotation() {
        return rotation;
    }
}
