/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity;

import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.util.math.AABBox;
import freeworld.world.World;

import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Entity {
    private final World world;
    private final UUID uuid;
    private final EntityType<? extends Entity> entityType;
    public Vector3d acceleration = Vector3d.ZERO;
    public AABBox boundingBox;
    public Vector3d eyePosition;
    public boolean flying = false;
    public boolean onGround = false;
    private Vector3d previousPosition = Vector3d.ZERO;
    private Vector3d position = Vector3d.ZERO;
    private Vector3d interpolatedPosition = Vector3d.ZERO;
    public Vector2d rotation = Vector2d.ZERO;
    public Vector3d velocity = Vector3d.ZERO;

    public Entity(World world, UUID uuid, EntityType<? extends Entity> entityType) {
        this.world = world;
        this.uuid = uuid;
        this.entityType = entityType;
        this.eyePosition = entityType.eyePosition();
    }

    public static AABBox boundingBox(
        Vector3d position,
        Vector3d dimension
    ) {
        final double hw = dimension.x() * 0.5;
        final double hd = dimension.z() * 0.5;
        return new AABBox(
            position.x() - hw,
            position.y(),
            position.z() - hd,
            position.x() + hw,
            position.y() + dimension.y(),
            position.z() + hd
        );
    }

    public final void init(Vector3d position) {
        this.position = position;
        this.boundingBox = boundingBox(position, entityType.dimension());
    }

    public void updatePreviousPosition() {
        this.previousPosition = position;
    }

    public void setPosition(Vector3d position) {
        this.position = position;
    }

    public void interpolatePosition(double partialTick) {
        interpolatedPosition = previousPosition.lerp(position, partialTick);
    }

    public World world() {
        return world;
    }

    public UUID uuid() {
        return uuid;
    }

    public EntityType<? extends Entity> entityType() {
        return entityType;
    }

    public Vector3d acceleration() {
        return acceleration;
    }

    public AABBox boundingBox() {
        return boundingBox;
    }

    public Vector3d eyePosition() {
        return eyePosition;
    }

    public boolean flying() {
        return flying;
    }

    public boolean onGround() {
        return onGround;
    }

    public Vector3d previousPosition() {
        return previousPosition;
    }

    public Vector3d position() {
        return position;
    }

    public Vector3d interpolatedPosition() {
        return interpolatedPosition;
    }

    public Vector2d rotation() {
        return rotation;
    }

    public Vector3d velocity() {
        return velocity;
    }
}
