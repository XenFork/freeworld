/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.entity.system;

import io.github.xenfork.freeworld.core.math.AABBox;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.chunk.ChunkPos;
import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.component.*;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MotionSystem implements EntitySystem {
    private final Vector3d movement = new Vector3d();

    @Override
    public void process(World world, List<Entity> entities) {
        for (Entity entity : entities) {
            if (EntitySystem.hasAllComponents(entity,
                AccelerationComponent.ID,
                PositionComponent.ID,
                VelocityComponent.ID)) {
                final Vector3d acceleration = entity.acceleration().value();
                final Vector3d position = entity.position().value();
                final Vector3d velocity = entity.velocity().value();

                velocity.add(acceleration);
                velocity.y -= 0.08;

                AABBox boundingBox;
                if (entity.hasComponent(BoundingBoxComponent.ID)) {
                    boundingBox = entity.boundingBox().value();

                    final double originVx = velocity.x();
                    final double originVy = velocity.y();
                    final double originVz = velocity.z();
                    double moveX = velocity.x();
                    double moveY = velocity.y();
                    double moveZ = velocity.z();

                    final AABBox range = boundingBox.expand(moveX, moveY, moveZ);
                    final List<AABBox> boxes = new ArrayList<>();
                    final int x0 = (int) Math.floor(range.minX());
                    final int y0 = (int) Math.floor(range.minY());
                    final int z0 = (int) Math.floor(range.minZ());
                    final int x1 = (int) Math.ceil(range.maxX() + 1.0);
                    final int y1 = (int) Math.ceil(range.maxY() + 1.0);
                    final int z1 = (int) Math.ceil(range.maxZ() + 1.0);
                    for (int x = x0; x < x1; x++) {
                        for (int y = y0; y < y1; y++) {
                            for (int z = z0; z < z1; z++) {
                                if (!world.isBlockLoaded(x, y, z)) {
                                    world.getOrCreateChunk(
                                        ChunkPos.absoluteToChunk(x),
                                        ChunkPos.absoluteToChunk(y),
                                        ChunkPos.absoluteToChunk(z)
                                    );
                                    continue;
                                }
                                final BlockType blockType = world.getBlockType(x, y, z);
                                if (blockType.air()) {
                                    continue;
                                }
                                final AABBox box = blockType.collisionShape().move(x, y, z);
                                boxes.add(box);
                            }
                        }
                    }

                    for (AABBox box : boxes) {
                        moveY = box.clipYCollide(boundingBox, moveY);
                    }
                    boundingBox = boundingBox.move(0.0, moveY, 0.0);
                    for (AABBox box : boxes) {
                        moveX = box.clipXCollide(boundingBox, moveX);
                    }
                    boundingBox = boundingBox.move(moveX, 0.0, 0.0);
                    for (AABBox box : boxes) {
                        moveZ = box.clipZCollide(boundingBox, moveZ);
                    }
                    boundingBox = boundingBox.move(0.0, 0.0, moveZ);

                    if (originVy != moveY && originVy < 0.0) {
                        entity.addComponent(OnGroundComponent.INSTANCE);
                    } else {
                        entity.removeComponent(OnGroundComponent.ID);
                    }

                    if (originVx != moveX) {
                        velocity.x = 0.0;
                    }
                    if (originVy != moveY) {
                        velocity.y = 0.0;
                    }
                    if (originVz != moveZ) {
                        velocity.z = 0.0;
                    }

                    position.add(moveX, moveY, moveZ);
                    entity.setComponent(new BoundingBoxComponent(computeBox(boundingBox, position)));
                } else {
                    position.add(velocity);
                }

                velocity.mul(0.91, 0.98, 0.91);
                if (entity.hasComponent(OnGroundComponent.ID)) {
                    final double fiction = 0.7;
                    velocity.x *= fiction;
                    velocity.z *= fiction;
                }
            }
        }
    }

    private AABBox computeBox(AABBox dimension, Vector3d position) {
        final double width = (dimension.maxX() - dimension.minX()) * 0.5;
        final double height = dimension.maxY() - dimension.minY();
        final double depth = (dimension.maxZ() - dimension.minZ()) * 0.5;
        return new AABBox(
            position.x() - width,
            position.y(),
            position.z() - depth,
            position.x() + width,
            position.y() + height,
            position.z() + depth
        );
    }
}
