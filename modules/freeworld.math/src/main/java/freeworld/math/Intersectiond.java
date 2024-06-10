/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.math;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class Intersectiond {
    private Intersectiond() {
    }

    public record RayAab(boolean intersected, Vector2d result) {
        public static final RayAab ZERO = new RayAab(false, Vector2d.ZERO);
    }

    public static RayAab intersectRayAab(
        double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
        double minX, double minY, double minZ, double maxX, double maxY, double maxZ
    ) {
        double invDirX = 1.0 / dirX, invDirY = 1.0 / dirY, invDirZ = 1.0 / dirZ;
        double tNear, tFar, tymin, tymax, tzmin, tzmax;
        if (invDirX >= 0.0) {
            tNear = (minX - originX) * invDirX;
            tFar = (maxX - originX) * invDirX;
        } else {
            tNear = (maxX - originX) * invDirX;
            tFar = (minX - originX) * invDirX;
        }
        if (invDirY >= 0.0) {
            tymin = (minY - originY) * invDirY;
            tymax = (maxY - originY) * invDirY;
        } else {
            tymin = (maxY - originY) * invDirY;
            tymax = (minY - originY) * invDirY;
        }
        if (tNear > tymax || tymin > tFar)
            return RayAab.ZERO;
        if (invDirZ >= 0.0) {
            tzmin = (minZ - originZ) * invDirZ;
            tzmax = (maxZ - originZ) * invDirZ;
        } else {
            tzmin = (maxZ - originZ) * invDirZ;
            tzmax = (minZ - originZ) * invDirZ;
        }
        if (tNear > tzmax || tzmin > tFar)
            return RayAab.ZERO;
        tNear = tymin > tNear || Double.isNaN(tNear) ? tymin : tNear;
        tFar = tymax < tFar || Double.isNaN(tFar) ? tymax : tFar;
        tNear = Math.max(tzmin, tNear);
        tFar = Math.min(tzmax, tFar);
        if (tNear < tFar && tFar >= 0.0) {
            return new RayAab(true, new Vector2d(tNear, tFar));
        }
        return RayAab.ZERO;
    }

    public static double intersectRayTriangleFront(
        double originX, double originY, double originZ, double dirX, double dirY, double dirZ,
        double v0X, double v0Y, double v0Z, double v1X, double v1Y, double v1Z, double v2X, double v2Y, double v2Z,
        double epsilon
    ) {
        double edge1X = v1X - v0X;
        double edge1Y = v1Y - v0Y;
        double edge1Z = v1Z - v0Z;
        double edge2X = v2X - v0X;
        double edge2Y = v2Y - v0Y;
        double edge2Z = v2Z - v0Z;
        double pvecX = dirY * edge2Z - dirZ * edge2Y;
        double pvecY = dirZ * edge2X - dirX * edge2Z;
        double pvecZ = dirX * edge2Y - dirY * edge2X;
        double det = edge1X * pvecX + edge1Y * pvecY + edge1Z * pvecZ;
        if (det <= epsilon)
            return -1.0;
        double tvecX = originX - v0X;
        double tvecY = originY - v0Y;
        double tvecZ = originZ - v0Z;
        double u = tvecX * pvecX + tvecY * pvecY + tvecZ * pvecZ;
        if (u < 0.0 || u > det)
            return -1.0;
        double qvecX = tvecY * edge1Z - tvecZ * edge1Y;
        double qvecY = tvecZ * edge1X - tvecX * edge1Z;
        double qvecZ = tvecX * edge1Y - tvecY * edge1X;
        double v = dirX * qvecX + dirY * qvecY + dirZ * qvecZ;
        if (v < 0.0 || u + v > det)
            return -1.0;
        double invDet = 1.0 / det;
        return (edge2X * qvecX + edge2Y * qvecY + edge2Z * qvecZ) * invDet;
    }
}
