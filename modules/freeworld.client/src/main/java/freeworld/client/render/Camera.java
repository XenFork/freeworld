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
import freeworld.world.entity.component.EntityComponentKeys;
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
        if (EntitySystem.hasAllComponents(entity, EntityComponentKeys.POSITION, EntityComponentKeys.ROTATION)) {
            final Vector3d ePos = entity.getComponent(EntityComponentKeys.POSITION);
            if (entity.hasComponent(EntityComponentKeys.EYE_HEIGHT)) {
                position = new Vector3d(
                    ePos.x(),
                    ePos.y() + entity.getComponent(EntityComponentKeys.EYE_HEIGHT),
                    ePos.z()
                );
            } else {
                position = ePos;
            }
            rotation = entity.getComponent(EntityComponentKeys.ROTATION);
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
