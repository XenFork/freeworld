/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private final Vector3d lerpPosition = new Vector3d();
    private final Matrix4f viewMatrix = new Matrix4f();

    public void move(double x, double y, double z) {
        position.add(x, y, z);
    }

    public void preUpdate() {
        prevPosition.set(position);
    }

    public void updateLerp(double partialTick) {
        prevPosition.lerp(position, partialTick, lerpPosition);
    }

    public void updateViewMatrix() {
        viewMatrix.translation((float) -lerpPosition.x(), (float) -lerpPosition.y(), (float) -lerpPosition.z());
    }

    public Vector3dc prevPosition() {
        return prevPosition;
    }

    public Vector3dc position() {
        return position;
    }

    public Vector3dc lerpPosition() {
        return lerpPosition;
    }

    public Matrix4fc viewMatrix() {
        return viewMatrix;
    }
}
