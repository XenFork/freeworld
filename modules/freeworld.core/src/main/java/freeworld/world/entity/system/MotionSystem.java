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

import freeworld.math.Maths;
import freeworld.math.Vector3d;
import freeworld.util.math.AABBox;
import freeworld.util.math.ChunkPos;
import freeworld.world.World;
import freeworld.world.block.BlockType;
import freeworld.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class MotionSystem implements EntitySystem {
    @Override
    public void process(World world, List<? extends Entity> entities) {
        for (Entity entity : entities) {
            double g = 0.08;
            if (entity.flying()) {
                g = 0.0;
            }
            entity.velocity = entity.velocity().add(entity.acceleration().x(), entity.acceleration().y() - g, entity.acceleration().z());

            final Vector3d originV = entity.velocity();
            double moveX = entity.velocity().x();
            double moveY = entity.velocity().y();
            double moveZ = entity.velocity().z();

            final AABBox range = entity.boundingBox().expand(moveX, moveY, moveZ);
            final List<AABBox> boxes = new ArrayList<>();
            final int x0 = Maths.floorToInt(range.minX());
            final int y0 = Maths.floorToInt(range.minY());
            final int z0 = Maths.floorToInt(range.minZ());
            final int x1 = Maths.ceilToInt(range.maxX() + 1.0);
            final int y1 = Maths.ceilToInt(range.maxY() + 1.0);
            final int z1 = Maths.ceilToInt(range.maxZ() + 1.0);
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
                        for (AABBox box : blockType.collisionShape().toBoxes()) {
                            boxes.add(box.move(x, y, z));
                        }
                    }
                }
            }

            for (AABBox box : boxes) {
                moveY = box.clipYCollide(entity.boundingBox(), moveY);
            }
            entity.boundingBox = entity.boundingBox().move(0.0, moveY, 0.0);
            for (AABBox box : boxes) {
                moveX = box.clipXCollide(entity.boundingBox(), moveX);
            }
            entity.boundingBox = entity.boundingBox().move(moveX, 0.0, 0.0);
            for (AABBox box : boxes) {
                moveZ = box.clipZCollide(entity.boundingBox(), moveZ);
            }
            entity.boundingBox = entity.boundingBox().move(0.0, 0.0, moveZ);

            entity.onGround = originV.y() != moveY && originV.y() < 0.0;

            double fvx = entity.velocity().x();
            double fvy = entity.velocity().y();
            double fvz = entity.velocity().z();
            if (originV.x() != moveX) {
                fvx = 0.0;
            }
            if (originV.y() != moveY) {
                fvy = 0.0;
            }
            if (originV.z() != moveZ) {
                fvz = 0.0;
            }
            entity.velocity = new Vector3d(fvx, fvy, fvz);

            entity.position = entity.position().add(moveX, moveY, moveZ);
            entity.boundingBox = Entity.boundingBox(entity.position(), entity.boundingBox().dimension());

            if (entity.flying()) {
                entity.velocity = Vector3d.ZERO;
            } else {
                entity.velocity = entity.velocity().mul(0.91, 0.98, 0.91);
                if (entity.onGround()) {
                    final double fiction = 0.7;
                    entity.velocity = entity.velocity().mul(fiction, 1.0, fiction);
                }
            }
        }
    }
}
