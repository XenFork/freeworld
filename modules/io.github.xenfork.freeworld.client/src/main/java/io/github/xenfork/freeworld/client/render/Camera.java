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

import org.joml.*;

import java.lang.Math;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private final Vector3d lerpPosition = new Vector3d();
    private final Vector2d rotation = new Vector2d();
    private final Matrix4f viewMatrix = new Matrix4f();

    public void setPosition(double x, double y, double z) {
        position.set(x, y, z);
    }

    public void move(double x, double y, double z) {
        position.add(x, y, z);
    }

    public void moveRelative(double x, double y, double z, double speed) {
        final double dest = x * x + z * z;
        if (dest > 0.001) {
            final double sqrt = Math.sqrt(dest);
            final double invSqrt = 1.0 / sqrt;
            final double yaw = Math.toRadians(rotation.y());

            final double normalX = x * invSqrt * speed;
            final double normalZ = z * invSqrt * speed;
            final double sin = Math.sin(yaw);
            final double cos = Math.cos(yaw);
            position.x += normalX * cos + normalZ * sin;
            position.z += normalZ * cos - normalX * sin;
        }
        position.y += y * speed;
    }

    public void rotate(double pitch, double yaw) {
        final double updateX = Math.clamp(rotation.x() + pitch, -90.0, 90.0);
        double updateY = rotation.y() + yaw;

        if (updateY < 0.0) {
            updateY += 360.0;
        } else if (updateY >= 360.0) {
            updateY -= 360.0;
        }

        rotation.set(updateX, updateY);
    }

    public void preUpdate() {
        prevPosition.set(position);
    }

    public void updateLerp(double partialTick) {
        prevPosition.lerp(position, partialTick, lerpPosition);
    }

    public void updateViewMatrix() {
        viewMatrix.rotationX((float) -Math.toRadians(rotation.x()))
            .rotateY((float) -Math.toRadians(rotation.y()))
            .translate((float) -lerpPosition.x(), (float) -lerpPosition.y(), (float) -lerpPosition.z());
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

    public Vector2dc rotation() {
        return rotation;
    }

    public Matrix4fc viewMatrix() {
        return viewMatrix;
    }
}
