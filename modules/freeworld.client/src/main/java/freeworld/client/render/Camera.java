/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render;

import freeworld.math.Matrix4f;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.world.entity.Entity;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private Vector3d prevPosition = Vector3d.ZERO;
    private Vector3d position = Vector3d.ZERO;
    private Vector3d lerpPosition = Vector3d.ZERO;
    private Vector2d rotation = Vector2d.ZERO;
    private Vector3d eyePosition = Vector3d.ZERO;

    public void moveToEntity(Entity entity) {
        eyePosition = entity.eyePosition();
        position = entity.position().add(0.0, eyePosition.y(), 0.0);
        rotation = entity.rotation();
    }

    public void preUpdate() {
        prevPosition = position;
    }

    public void updateLerp(double partialTick) {
        lerpPosition = prevPosition.lerp(position, partialTick);
    }

    public Matrix4f updateViewMatrix() {
        return Matrix4f.translation((float) -eyePosition.x(), 0.0f, (float) -eyePosition.z())
            .rotateX((float) -Math.toRadians(rotation.x()))
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
