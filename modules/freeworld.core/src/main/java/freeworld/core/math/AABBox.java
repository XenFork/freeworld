/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.core.math;

/**
 * @author squid233
 * @since 0.1.0
 */
public record AABBox(
    double minX,
    double minY,
    double minZ,
    double maxX,
    double maxY,
    double maxZ
) {
    public static final AABBox EMPTY = new AABBox(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
    public static final AABBox FULL_CUBE = new AABBox(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    public AABBox {
        if (minX > maxX) {
            double _minX = minX;
            minX = maxX;
            maxX = _minX;
        }
        if (minY > maxY) {
            double _minY = minY;
            minY = maxY;
            maxY = _minY;
        }
        if (minZ > maxZ) {
            double _minZ = minZ;
            minZ = maxZ;
            maxZ = _minZ;
        }
    }

    public AABBox move(double x, double y, double z) {
        return new AABBox(
            minX() + x,
            minY() + y,
            minZ() + z,
            maxX() + x,
            maxY() + y,
            maxZ() + z
        );
    }

    public AABBox expand(double x, double y, double z) {
        return new AABBox(
            x < 0.0 ? minX() + x : minX(),
            y < 0.0 ? minY() + y : minY(),
            z < 0.0 ? minZ() + z : minZ(),
            x > 0.0 ? maxX() + x : maxX(),
            y > 0.0 ? maxY() + y : maxY(),
            z > 0.0 ? maxZ() + z : maxZ()
        );
    }

    public AABBox grow(double x, double y, double z) {
        return new AABBox(
            minX() - x,
            minY() - y,
            minZ() - z,
            maxX() + x,
            maxY() + y,
            maxZ() + z
        );
    }

    public double clipXCollide(AABBox moving, double movement) {
        if (moving.maxY() <= minY() ||
            moving.minY() >= maxY() ||
            moving.maxZ() <= minZ() ||
            moving.minZ() >= maxZ()) {
            return movement;
        }
        double finalMovement = movement;
        double max;
        if (movement > 0.0 && moving.maxX() <= minX()) {
            max = minX() - moving.maxX();
            if (max < finalMovement) {
                finalMovement = max;
            }
        }
        if (movement < 0.0 && moving.minX() >= maxX()) {
            max = maxX() - moving.minX();
            if (max > finalMovement) {
                finalMovement = max;
            }
        }
        return finalMovement;
    }

    public double clipYCollide(AABBox moving, double movement) {
        if (moving.maxX() <= minX() ||
            moving.minX() >= maxX() ||
            moving.maxZ() <= minZ() ||
            moving.minZ() >= maxZ()) {
            return movement;
        }
        double finalMovement = movement;
        double max;
        if (movement > 0.0 && moving.maxY() <= minY()) {
            max = minY() - moving.maxY();
            if (max < finalMovement) {
                finalMovement = max;
            }
        }
        if (movement < 0.0 && moving.minY() >= maxY()) {
            max = maxY() - moving.minY();
            if (max > finalMovement) {
                finalMovement = max;
            }
        }
        return finalMovement;
    }

    public double clipZCollide(AABBox moving, double movement) {
        if (moving.maxX() <= minX() ||
            moving.minX() >= maxX() ||
            moving.maxY() <= minY() ||
            moving.minY() >= maxY()) {
            return movement;
        }
        double finalMovement = movement;
        double max;
        if (movement > 0.0 && moving.maxZ() <= minZ()) {
            max = minZ() - moving.maxZ();
            if (max < finalMovement) {
                finalMovement = max;
            }
        }
        if (movement < 0.0 && moving.minZ() >= maxZ()) {
            max = maxZ() - moving.minZ();
            if (max > finalMovement) {
                finalMovement = max;
            }
        }
        return finalMovement;
    }
}
