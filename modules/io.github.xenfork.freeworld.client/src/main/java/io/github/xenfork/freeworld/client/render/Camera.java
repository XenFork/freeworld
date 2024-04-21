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

import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.component.EyeHeightComponent;
import io.github.xenfork.freeworld.world.entity.component.PositionComponent;
import io.github.xenfork.freeworld.world.entity.component.RotationXYComponent;
import io.github.xenfork.freeworld.world.entity.system.EntitySystem;
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

    public void moveToEntity(Entity entity) {
        if (EntitySystem.hasAllComponents(entity, PositionComponent.ID, RotationXYComponent.ID)) {
            position.set(entity.position().value());
            if (entity.hasComponent(EyeHeightComponent.ID)) {
                position.y += entity.eyeHeight().value();
            }
            rotation.set(entity.rotation().value());
        }
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
