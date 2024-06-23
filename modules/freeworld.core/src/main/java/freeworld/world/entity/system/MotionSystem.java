/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity.system;

import freeworld.core.math.AABBox;
import freeworld.math.Vector3d;
import freeworld.world.World;
import freeworld.world.block.BlockType;
import freeworld.world.chunk.ChunkPos;
import freeworld.world.entity.Entity;
import freeworld.world.entity.EntityComponents;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MotionSystem implements EntitySystem {
    @Override
    public void process(World world, List<Entity> entities) {
        for (Entity entity : entities) {
            if (EntitySystem.hasAllComponents(entity,
                EntityComponents.ACCELERATION,
                EntityComponents.BOUNDING_BOX,
                EntityComponents.POSITION,
                EntityComponents.VELOCITY)) {
                final Vector3d acceleration = entity.getComponent(EntityComponents.ACCELERATION);
                Vector3d position = entity.getComponent(EntityComponents.POSITION);
                Vector3d velocity = entity.getComponent(EntityComponents.VELOCITY);

                velocity = velocity.add(acceleration.x(), acceleration.y() - 0.08, acceleration.z());

                AABBox boundingBox = entity.getComponent(EntityComponents.BOUNDING_BOX);

                final Vector3d originV = velocity;
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

                if (originV.y() != moveY && originV.y() < 0.0) {
                    entity.addComponent(EntityComponents.ON_GROUND);
                } else {
                    entity.removeComponent(EntityComponents.ON_GROUND);
                }

                double fvx = velocity.x();
                double fvy = velocity.y();
                double fvz = velocity.z();
                if (originV.x() != moveX) {
                    fvx = 0.0;
                }
                if (originV.y() != moveY) {
                    fvy = 0.0;
                }
                if (originV.z() != moveZ) {
                    fvz = 0.0;
                }
                velocity = new Vector3d(fvx, fvy, fvz);

                position = position.add(moveX, moveY, moveZ);
                entity.setComponent(EntityComponents.POSITION, position);
                entity.setComponent(EntityComponents.BOUNDING_BOX, computeBox(boundingBox, position));

                velocity = velocity.mul(0.91, 0.98, 0.91);
                if (entity.hasComponent(EntityComponents.ON_GROUND)) {
                    final double fiction = 0.7;
                    velocity = velocity.mul(fiction, 1.0, fiction);
                }
                entity.setComponent(EntityComponents.VELOCITY, velocity);
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
