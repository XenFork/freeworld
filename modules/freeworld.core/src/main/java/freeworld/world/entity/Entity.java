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

import freeworld.math.Maths;
import freeworld.math.Vector2d;
import freeworld.math.Vector3d;
import freeworld.math.Vector3i;
import freeworld.util.math.AABBox;
import freeworld.util.math.ChunkPos;
import freeworld.world.World;

import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public class Entity {
    private final World world;
    private final EntityType<? extends Entity> type;
    private final UUID uuid = UUID.randomUUID();
    public Vector3d acceleration = Vector3d.ZERO;
    private Vector3i blockPos = Vector3i.ZERO;
    public AABBox boundingBox;
    private EntityChangeListener changeListener = EntityChangeListener.EMPTY;
    private Vector3i chunkPos = Vector3i.ZERO;
    private final Vector3d dimension;
    private final double eyeHeight;
    public boolean flying = false;
    public boolean onGround = false;
    private Vector3d previousPosition = Vector3d.ZERO;
    private Vector3d position = Vector3d.ZERO;
    public Vector2d rotation = Vector2d.ZERO;
    public Vector3d velocity = Vector3d.ZERO;

    public Entity(EntityType<? extends Entity> type, World world) {
        this.world = world;
        this.type = type;
        this.dimension = type.dimension();
        this.eyeHeight = getEyeHeight(dimension);
        setPosition(new Vector3d(0.0, 0.0, 0.0));
    }

    public void setChangeListener(EntityChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    private AABBox calculateBoundingBox() {
        // TODO: use EntityDimension
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

    public double getEyeHeight(Vector3d dimension) {
        return dimension.y() * 0.9;
    }

    public double getEyeHeight() {
        return getEyeHeight(dimension);
    }

    public void updatePreviousPosition() {
        this.previousPosition = position;
    }

    public void setPosition(Vector3d position) {
        setPos(position);
        setBoundingBox(calculateBoundingBox());
    }

    public void setPos(Vector3d pos) {
        if (!this.position.equals(pos)) {
            this.position = pos;
            int x = Maths.floorToInt(this.position.x());
            int y = Maths.floorToInt(this.position.y());
            int z = Maths.floorToInt(this.position.z());
            if (x != this.blockPos.x() || y != this.blockPos.y() || z != this.blockPos.z()) {
                this.blockPos = new Vector3i(x, y, z);
                Vector3i chunkPos = ChunkPos.toChunkPos(this.blockPos);
                if (!this.chunkPos.equals(chunkPos)) {
                    this.chunkPos = chunkPos;
                }
            }
            changeListener.onEntityPositionUpdated();
        }
    }

    public void setBoundingBox(AABBox boundingBox) {
        this.boundingBox = boundingBox;
    }

    public Vector3d interpolatedPosition(double partialTick) {
        return previousPosition.linearInterpolate(position, partialTick);
    }

    public Vector3d getCameraPos(double partialTick) {
        return interpolatedPosition(partialTick).add(0.0, eyeHeight, 0.0);
    }

    public World world() {
        return world;
    }

    public UUID uuid() {
        return uuid;
    }

    public EntityType<?> type() {
        return type;
    }

    public Vector3d acceleration() {
        return acceleration;
    }

    public Vector3i blockPos() {
        return blockPos;
    }

    public AABBox boundingBox() {
        return boundingBox;
    }

    public Vector3i chunkPos() {
        return chunkPos;
    }

    public double eyeHeight() {
        return eyeHeight;
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

    public Vector2d rotation() {
        return rotation;
    }

    public Vector3d velocity() {
        return velocity;
    }
}
