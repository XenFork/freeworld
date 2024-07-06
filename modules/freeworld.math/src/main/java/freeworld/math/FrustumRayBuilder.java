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
public record FrustumRayBuilder(
    float nxnyX, float nxnyY, float nxnyZ,
    float pxnyX, float pxnyY, float pxnyZ,
    float pxpyX, float pxpyY, float pxpyZ,
    float nxpyX, float nxpyY, float nxpyZ,
    float cx, float cy, float cz
) {
    public static final FrustumRayBuilder ZERO = new FrustumRayBuilder(
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f
    );

    public FrustumRayBuilder(Matrix4f m) {
        float nxX = m.m03() + m.m00(), nxY = m.m13() + m.m10(), nxZ = m.m23() + m.m20(), d1 = m.m33() + m.m30();
        float pxX = m.m03() - m.m00(), pxY = m.m13() - m.m10(), pxZ = m.m23() - m.m20(), d2 = m.m33() - m.m30();
        float nyX = m.m03() + m.m01(), nyY = m.m13() + m.m11(), nyZ = m.m23() + m.m21();
        float pyX = m.m03() - m.m01(), pyY = m.m13() - m.m11(), pyZ = m.m23() - m.m21(), d3 = m.m33() - m.m31();
        // bottom left
        float nxnyX = nyY * nxZ - nyZ * nxY;
        float nxnyY = nyZ * nxX - nyX * nxZ;
        float nxnyZ = nyX * nxY - nyY * nxX;
        // bottom right
        float pxnyX = pxY * nyZ - pxZ * nyY;
        float pxnyY = pxZ * nyX - pxX * nyZ;
        float pxnyZ = pxX * nyY - pxY * nyX;
        // top left
        float nxpyX = nxY * pyZ - nxZ * pyY;
        float nxpyY = nxZ * pyX - nxX * pyZ;
        float nxpyZ = nxX * pyY - nxY * pyX;
        // top right
        float pxpyX = pyY * pxZ - pyZ * pxY;
        float pxpyY = pyZ * pxX - pyX * pxZ;
        float pxpyZ = pyX * pxY - pyY * pxX;
        // compute origin
        float pxnxX, pxnxY, pxnxZ;
        pxnxX = pxY * nxZ - pxZ * nxY;
        pxnxY = pxZ * nxX - pxX * nxZ;
        pxnxZ = pxX * nxY - pxY * nxX;
        float invDot = 1.0f / (nxX * pxpyX + nxY * pxpyY + nxZ * pxpyZ);
        float cx = (-pxpyX * d1 - nxpyX * d2 - pxnxX * d3) * invDot;
        float cy = (-pxpyY * d1 - nxpyY * d2 - pxnxY * d3) * invDot;
        float cz = (-pxpyZ * d1 - nxpyZ * d2 - pxnxZ * d3) * invDot;
        this(nxnyX, nxnyY, nxnyZ, pxnyX, pxnyY, pxnyZ, pxpyX, pxpyY, pxpyZ, nxpyX, nxpyY, nxpyZ, cx, cy, cz);
    }

    public Vector3f origin() {
        return new Vector3f(cx, cy, cz);
    }

    public Vector3f dir(float x, float y) {
        float y1x = nxnyX + (nxpyX - nxnyX) * y;
        float y1y = nxnyY + (nxpyY - nxnyY) * y;
        float y1z = nxnyZ + (nxpyZ - nxnyZ) * y;
        float y2x = pxnyX + (pxpyX - pxnyX) * y;
        float y2y = pxnyY + (pxpyY - pxnyY) * y;
        float y2z = pxnyZ + (pxpyZ - pxnyZ) * y;
        float dx = y1x + (y2x - y1x) * x;
        float dy = y1y + (y2y - y1y) * x;
        float dz = y1z + (y2z - y1z) * x;
        // normalize the vector
        float invLen = (float) (1.0 / Math.sqrt(dx * dx + dy * dy + dz * dz));
        return new Vector3f(
            dx * invLen,
            dy * invLen,
            dz * invLen
        );
    }
}
