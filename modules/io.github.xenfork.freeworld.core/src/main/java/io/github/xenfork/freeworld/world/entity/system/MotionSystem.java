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
import io.github.xenfork.freeworld.util.MathUtil;
import io.github.xenfork.freeworld.world.World;
import io.github.xenfork.freeworld.world.block.BlockType;
import io.github.xenfork.freeworld.world.entity.Entity;
import io.github.xenfork.freeworld.world.entity.component.*;
import org.joml.Vector2d;
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
                RotationXYComponent.ID,
                VelocityComponent.ID)) {
                final Vector3d acceleration = entity.acceleration().value();
                final Vector3d position = entity.position().value();
                final Vector2d rotation = entity.rotation().value();
                final Vector3d velocity = entity.velocity().value();

                velocity.add(acceleration);
                MathUtil.moveRelative(velocity.x(), velocity.y(), velocity.z(), rotation.y(), movement);

                AABBox boundingBox;
                if (entity.hasComponent(BoundingBoxComponent.ID)) {
                    boundingBox = entity.boundingBox().value();

                    final AABBox range = boundingBox.expand(movement.x(), movement.y(), movement.z());
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
                                if (!world.isInBound(x, y, z)) {
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

                    final double originMovementY = movement.y();
                    for (AABBox box : boxes) {
                        movement.y = box.clipYCollide(boundingBox, movement.y());
                    }
                    if (originMovementY != movement.y && originMovementY < 0.0) {
                        velocity.y = 0.0;
                        entity.addComponent(OnGroundComponent.INSTANCE);
                    } else {
                        entity.removeComponent(OnGroundComponent.ID);
                    }
                    position.y += movement.y();
                    boundingBox = computeBox(boundingBox, position);

                    for (AABBox box : boxes) {
                        movement.x = box.clipXCollide(boundingBox, movement.x());
                    }
                    position.x += movement.x();
                    boundingBox = computeBox(boundingBox, position);

                    for (AABBox box : boxes) {
                        movement.z = box.clipZCollide(boundingBox, movement.z());
                    }
                    position.z += movement.z();

                    entity.setComponent(new BoundingBoxComponent(computeBox(boundingBox, position)));
                } else {
                    position.add(movement);
                }

                velocity.y -= 0.08;
                velocity.mul(0.8, 0.98, 0.8);
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
