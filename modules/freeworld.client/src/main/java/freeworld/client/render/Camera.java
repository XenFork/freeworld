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
    private Vector3d position = Vector3d.ZERO;
    private Vector2d rotation = Vector2d.ZERO;

    public void moveToEntity(Entity entity, double partialTick) {
        position = entity.getCameraPos(partialTick);
        rotation = entity.rotation();
    }

    public Matrix4f updateViewMatrix() {
        return Matrix4f.rotationX((float) -Math.toRadians(rotation.x()))
            .rotateY((float) -Math.toRadians(rotation.y()))
            .translate((float) -position.x(), (float) -position.y(), (float) -position.z());
    }
}
