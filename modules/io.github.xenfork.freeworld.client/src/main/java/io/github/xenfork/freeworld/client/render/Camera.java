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
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Camera {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final VarHandle X2;
    private static final VarHandle Y2;
    private static final VarHandle X3;
    private static final VarHandle Y3;
    private static final VarHandle Z3;

    static {
        try {
            X2 = LOOKUP.findVarHandle(Vector2d.class, "x", double.class);
            Y2 = LOOKUP.findVarHandle(Vector2d.class, "y", double.class);
            X3 = LOOKUP.findVarHandle(Vector3d.class, "x", double.class);
            Y3 = LOOKUP.findVarHandle(Vector3d.class, "y", double.class);
            Z3 = LOOKUP.findVarHandle(Vector3d.class, "z", double.class);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private final Vector3d prevPosition = new Vector3d();
    private final Vector3d position = new Vector3d();
    private final Vector3d lerpPosition = new Vector3d();
    private final Vector2d rotation = new Vector2d();
    private final Matrix4f viewMatrix = new Matrix4f();

    public void setPosition(double x, double y, double z) {
        X3.setOpaque(position, x);
        Y3.setOpaque(position, y);
        Z3.setOpaque(position, z);
    }

    public void move(double x, double y, double z) {
        setPosition(
            (double) X3.getOpaque(position) + x,
            (double) Y3.getOpaque(position) + y,
            (double) Z3.getOpaque(position) + z
        );
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
            X3.setOpaque(position, (double) X3.getOpaque(position) + normalX * cos + normalZ * sin);
            Z3.setOpaque(position, (double) Z3.getOpaque(position) + normalZ * cos - normalX * sin);
        }
        Y3.setOpaque(position, (double) Y3.getOpaque(position) + y * speed);
    }

    public void rotate(double pitch, double yaw) {
        final double updateX = Math.clamp((double) X2.getOpaque(rotation) + pitch, -90.0, 90.0);
        double updateY = (double) Y2.getOpaque(rotation) + yaw;

        if (updateY < 0.0) {
            updateY += 360.0;
        } else if (updateY >= 360.0) {
            updateY -= 360.0;
        }

        X2.setOpaque(rotation, updateX);
        Y2.setOpaque(rotation, updateY);
    }

    public void preUpdate() {
        X3.setOpaque(prevPosition, X3.getOpaque(position));
        Y3.setOpaque(prevPosition, Y3.getOpaque(position));
        Z3.setOpaque(prevPosition, Z3.getOpaque(position));
    }

    public void updateLerp(double partialTick) {
        X3.setOpaque(lerpPosition, org.joml.Math.lerp((double) X3.getOpaque(prevPosition), (double) X3.getOpaque(position), partialTick));
        Y3.setOpaque(lerpPosition, org.joml.Math.lerp((double) Y3.getOpaque(prevPosition), (double) Y3.getOpaque(position), partialTick));
        Z3.setOpaque(lerpPosition, org.joml.Math.lerp((double) Z3.getOpaque(prevPosition), (double) Z3.getOpaque(position), partialTick));
    }

    public void updateViewMatrix() {
        viewMatrix.rotationX((float) -Math.toRadians((double) X2.getOpaque(rotation)))
            .rotateY((float) -Math.toRadians((double) Y2.getOpaque(rotation)))
            .translate((float) -((double) X3.getOpaque(lerpPosition)), (float) -((double) Y3.getOpaque(lerpPosition)), (float) -((double) Z3.getOpaque(lerpPosition)));
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
