/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.util.shape;

import freeworld.math.Intersectiond;
import freeworld.math.Vector3d;
import freeworld.math.Vector4i;
import freeworld.util.Direction;
import freeworld.util.math.AABBox;
import freeworld.util.math.HitResult;
import freeworld.util.math.Lined;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
final class SingleVoxelShape implements VoxelShape {
    private static final float epsilon = 0.00001f;
    static final VoxelShape FULL_CUBE = new SingleVoxelShape(new AABBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0));
    private final AABBox box;
    private final List<AABBox> list;

    SingleVoxelShape(AABBox box) {
        this.box = box;
        this.list = List.of(box);
    }

    private record RayCastFace(boolean missed, double distance) {
        private static final RayCastFace MISSED = new RayCastFace(true, 0.0);
    }

    private RayCastFace rayCastFace(Direction direction, Vector3d origin, Vector3d dir) {
        Vector4i vertexIndices = direction.vertexIndices();
        Vector3d v0 = box.getPoint(vertexIndices.x());
        Vector3d v1 = box.getPoint(vertexIndices.y());
        Vector3d v2 = box.getPoint(vertexIndices.z());
        Vector3d v3 = box.getPoint(vertexIndices.w());
        double v = Math.max(
            Intersectiond.intersectRayTriangleFront(origin, dir, v0, v1, v2, epsilon),
            Intersectiond.intersectRayTriangleFront(origin, dir, v2, v3, v0, epsilon)
        );
        if (v == -1.0) {
            return RayCastFace.MISSED;
        }
        return new RayCastFace(false, v);
    }

    @Override
    public HitResult rayCast(Vector3d origin, Vector3d dir) {
        Intersectiond.RayAab rayAab = Intersectiond.intersectRayAab(
            origin.x(), origin.y(), origin.z(),
            dir.x(), dir.y(), dir.z(),
            box.minX(), box.minY(), box.minZ(),
            box.maxX(), box.maxY(), box.maxZ()
        );
        if (!rayAab.intersected()) {
            return HitResult.MISSED;
        }
        RayCastFace rayCastFace = RayCastFace.MISSED;
        Direction face = null;
        for (Direction direction : Direction.LIST) {
            RayCastFace face1 = rayCastFace(direction, origin, dir);
            if (!face1.missed()) {
                rayCastFace = face1;
                face = direction;
            }
        }
        if (rayCastFace.missed()) {
            return HitResult.MISSED;
        }
        return new HitResult(false, rayCastFace.distance(), face);
    }

    @Override
    public List<AABBox> toBoxes() {
        return list;
    }

    @Override
    public List<Lined> toLines(List<Direction> directions) {
        List<Lined> lines = new ArrayList<>();
        for (Direction direction : directions) {
            Vector4i vertexIndices = direction.vertexIndices();
            Vector3d v0 = box.getPoint(vertexIndices.x());
            Vector3d v1 = box.getPoint(vertexIndices.y());
            Vector3d v2 = box.getPoint(vertexIndices.z());
            Vector3d v3 = box.getPoint(vertexIndices.w());
            lines.add(new Lined(v0, v1));
            lines.add(new Lined(v1, v2));
            lines.add(new Lined(v2, v3));
            lines.add(new Lined(v3, v0));
        }
        return Collections.unmodifiableList(lines);
    }
}
