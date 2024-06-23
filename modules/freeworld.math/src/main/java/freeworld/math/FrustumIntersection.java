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

import java.util.Arrays;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class FrustumIntersection {
    private static Vector4f[] zeroVec() {
        Vector4f[] arr = new Vector4f[6];
        Arrays.fill(arr, Vector4f.ZERO);
        return arr;
    }

    public static final FrustumIntersection ZERO = new FrustumIntersection(
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 0.0f,
        zeroVec()
    );
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>x=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NX = 0;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>x=1</code> when using the identity frustum.
     */
    public static final int PLANE_PX = 1;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>y=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NY = 2;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>y=1</code> when using the identity frustum.
     */
    public static final int PLANE_PY = 3;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>z=-1</code> when using the identity frustum.
     */
    public static final int PLANE_NZ = 4;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads identifying the plane with equation <code>z=1</code> when using the identity frustum.
     */
    public static final int PLANE_PZ = 5;

    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads indicating that the axis-aligned box intersects the frustum.
     */
    public static final int INTERSECT = -1;
    /**
     * Return value of {@link #intersectAab(float, float, float, float, float, float) intersectAab()}
     * and its different overloads indicating that the axis-aligned box is fully inside of the frustum.
     */
    public static final int INSIDE = -2;
    /**
     * Return value of {@link #intersectSphere(Vector3f, float)} or {@link #intersectSphere(float, float, float, float)}
     * indicating that the sphere is completely outside of the frustum.
     */
    public static final int OUTSIDE = -3;

    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>x=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NX = 1 << PLANE_NX;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>x=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PX = 1 << PLANE_PX;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>y=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NY = 1 << PLANE_NY;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>y=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PY = 1 << PLANE_PY;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>z=-1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_NZ = 1 << PLANE_NZ;
    /**
     * The value in a bitmask for
     * {@link #intersectAab(float, float, float, float, float, float, int) intersectAab()}
     * that identifies the plane with equation <code>z=1</code> when using the identity frustum.
     */
    public static final int PLANE_MASK_PZ = 1 << PLANE_PZ;

    private final float nxX, nxY, nxZ, nxW;
    private final float pxX, pxY, pxZ, pxW;
    private final float nyX, nyY, nyZ, nyW;
    private final float pyX, pyY, pyZ, pyW;
    private final float nzX, nzY, nzZ, nzW;
    private final float pzX, pzY, pzZ, pzW;

    private final Vector4f[] planes = new Vector4f[6];

    public FrustumIntersection(
        float nxX, float nxY, float nxZ, float nxW,
        float pxX, float pxY, float pxZ, float pxW,
        float nyX, float nyY, float nyZ, float nyW,
        float pyX, float pyY, float pyZ, float pyW,
        float nzX, float nzY, float nzZ, float nzW,
        float pzX, float pzY, float pzZ, float pzW,
        Vector4f[] planes
    ) {
        this.nxX = nxX;
        this.nxY = nxY;
        this.nxZ = nxZ;
        this.nxW = nxW;
        this.pxX = pxX;
        this.pxY = pxY;
        this.pxZ = pxZ;
        this.pxW = pxW;
        this.nyX = nyX;
        this.nyY = nyY;
        this.nyZ = nyZ;
        this.nyW = nyW;
        this.pyX = pyX;
        this.pyY = pyY;
        this.pyZ = pyZ;
        this.pyW = pyW;
        this.nzX = nzX;
        this.nzY = nzY;
        this.nzZ = nzZ;
        this.nzW = nzW;
        this.pzX = pzX;
        this.pzY = pzY;
        this.pzZ = pzZ;
        this.pzW = pzW;
        System.arraycopy(planes, 0, this.planes, 0, this.planes.length);
    }

    public FrustumIntersection(Matrix4f m) {
        this(m, true);
    }

    public FrustumIntersection(Matrix4f m, boolean allowTestSpheres) {
        Vector4f[] planes = new Vector4f[6];
        float invl;
        float nxX = m.m03() + m.m00();
        float nxY = m.m13() + m.m10();
        float nxZ = m.m23() + m.m20();
        float nxW = m.m33() + m.m30();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(nxX * nxX + nxY * nxY + nxZ * nxZ));
            nxX *= invl;
            nxY *= invl;
            nxZ *= invl;
            nxW *= invl;
        }
        planes[0] = new Vector4f(nxX, nxY, nxZ, nxW);
        float pxX = m.m03() - m.m00();
        float pxY = m.m13() - m.m10();
        float pxZ = m.m23() - m.m20();
        float pxW = m.m33() - m.m30();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(pxX * pxX + pxY * pxY + pxZ * pxZ));
            pxX *= invl;
            pxY *= invl;
            pxZ *= invl;
            pxW *= invl;
        }
        planes[1] = new Vector4f(pxX, pxY, pxZ, pxW);
        float nyX = m.m03() + m.m01();
        float nyY = m.m13() + m.m11();
        float nyZ = m.m23() + m.m21();
        float nyW = m.m33() + m.m31();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(nyX * nyX + nyY * nyY + nyZ * nyZ));
            nyX *= invl;
            nyY *= invl;
            nyZ *= invl;
            nyW *= invl;
        }
        planes[2] = new Vector4f(nyX, nyY, nyZ, nyW);
        float pyX = m.m03() - m.m01();
        float pyY = m.m13() - m.m11();
        float pyZ = m.m23() - m.m21();
        float pyW = m.m33() - m.m31();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(pyX * pyX + pyY * pyY + pyZ * pyZ));
            pyX *= invl;
            pyY *= invl;
            pyZ *= invl;
            pyW *= invl;
        }
        planes[3] = new Vector4f(pyX, pyY, pyZ, pyW);
        float nzX = m.m03() + m.m02();
        float nzY = m.m13() + m.m12();
        float nzZ = m.m23() + m.m22();
        float nzW = m.m33() + m.m32();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(nzX * nzX + nzY * nzY + nzZ * nzZ));
            nzX *= invl;
            nzY *= invl;
            nzZ *= invl;
            nzW *= invl;
        }
        planes[4] = new Vector4f(nzX, nzY, nzZ, nzW);
        float pzX = m.m03() - m.m02();
        float pzY = m.m13() - m.m12();
        float pzZ = m.m23() - m.m22();
        float pzW = m.m33() - m.m32();
        if (allowTestSpheres) {
            invl = (float) (1.0 / Math.sqrt(pzX * pzX + pzY * pzY + pzZ * pzZ));
            pzX *= invl;
            pzY *= invl;
            pzZ *= invl;
            pzW *= invl;
        }
        planes[5] = new Vector4f(pzX, pzY, pzZ, pzW);
        this(
            nxX, nxY, nxZ, nxW,
            pxX, pxY, pxZ, pxW,
            nyX, nyY, nyZ, nyW,
            pyX, pyY, pyZ, pyW,
            nzX, nzY, nzZ, nzW,
            pzX, pzY, pzZ, pzW,
            planes
        );
    }

    public int intersectSphere(Vector3f center, float radius) {
        return intersectSphere(center.x(), center.y(), center.z(), radius);
    }

    public int intersectSphere(float x, float y, float z, float r) {
        boolean inside = true;
        float dist;
        dist = nxX * x + nxY * y + nxZ * z + nxW;
        if (dist >= -r) {
            inside &= dist >= r;
            dist = pxX * x + pxY * y + pxZ * z + pxW;
            if (dist >= -r) {
                inside &= dist >= r;
                dist = nyX * x + nyY * y + nyZ * z + nyW;
                if (dist >= -r) {
                    inside &= dist >= r;
                    dist = pyX * x + pyY * y + pyZ * z + pyW;
                    if (dist >= -r) {
                        inside &= dist >= r;
                        dist = nzX * x + nzY * y + nzZ * z + nzW;
                        if (dist >= -r) {
                            inside &= dist >= r;
                            dist = pzX * x + pzY * y + pzZ * z + pzW;
                            if (dist >= -r) {
                                inside &= dist >= r;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return OUTSIDE;
    }

    public boolean testAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         * It does not distinguish between partially inside and fully inside, though, so the test with the 'p' vertex is omitted.
         */
        return nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW &&
               pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW &&
               nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW &&
               pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW &&
               nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW &&
               pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW;
    }

    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ) {
        /*
         * This is an implementation of the "2.4 Basic intersection test" of the mentioned site.
         *
         * In addition to the algorithm in the paper, this method also returns the index of the first plane that culled the box.
         */
        int plane = PLANE_NX;
        boolean inside = true;
        if (nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW) {
            plane = PLANE_PX;
            inside &= nxX * (nxX < 0 ? maxX : minX) + nxY * (nxY < 0 ? maxY : minY) + nxZ * (nxZ < 0 ? maxZ : minZ) >= -nxW;
            if (pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW) {
                plane = PLANE_NY;
                inside &= pxX * (pxX < 0 ? maxX : minX) + pxY * (pxY < 0 ? maxY : minY) + pxZ * (pxZ < 0 ? maxZ : minZ) >= -pxW;
                if (nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW) {
                    plane = PLANE_PY;
                    inside &= nyX * (nyX < 0 ? maxX : minX) + nyY * (nyY < 0 ? maxY : minY) + nyZ * (nyZ < 0 ? maxZ : minZ) >= -nyW;
                    if (pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW) {
                        plane = PLANE_NZ;
                        inside &= pyX * (pyX < 0 ? maxX : minX) + pyY * (pyY < 0 ? maxY : minY) + pyZ * (pyZ < 0 ? maxZ : minZ) >= -pyW;
                        if (nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW) {
                            plane = PLANE_PZ;
                            inside &= nzX * (nzX < 0 ? maxX : minX) + nzY * (nzY < 0 ? maxY : minY) + nzZ * (nzZ < 0 ? maxZ : minZ) >= -nzW;
                            if (pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW) {
                                inside &= pzX * (pzX < 0 ? maxX : minX) + pzY * (pzY < 0 ? maxY : minY) + pzZ * (pzZ < 0 ? maxZ : minZ) >= -pzW;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return plane;
    }

    public int intersectAab(float minX, float minY, float minZ, float maxX, float maxY, float maxZ, int mask) {
        /*
         * This is an implementation of the first algorithm in "2.5 Plane masking and coherency" of the mentioned site.
         *
         * In addition to the algorithm in the paper, this method also returns the index of the first plane that culled the box.
         */
        int plane = PLANE_NX;
        boolean inside = true;
        if ((mask & PLANE_MASK_NX) == 0 || nxX * (nxX < 0 ? minX : maxX) + nxY * (nxY < 0 ? minY : maxY) + nxZ * (nxZ < 0 ? minZ : maxZ) >= -nxW) {
            plane = PLANE_PX;
            inside &= nxX * (nxX < 0 ? maxX : minX) + nxY * (nxY < 0 ? maxY : minY) + nxZ * (nxZ < 0 ? maxZ : minZ) >= -nxW;
            if ((mask & PLANE_MASK_PX) == 0 || pxX * (pxX < 0 ? minX : maxX) + pxY * (pxY < 0 ? minY : maxY) + pxZ * (pxZ < 0 ? minZ : maxZ) >= -pxW) {
                plane = PLANE_NY;
                inside &= pxX * (pxX < 0 ? maxX : minX) + pxY * (pxY < 0 ? maxY : minY) + pxZ * (pxZ < 0 ? maxZ : minZ) >= -pxW;
                if ((mask & PLANE_MASK_NY) == 0 || nyX * (nyX < 0 ? minX : maxX) + nyY * (nyY < 0 ? minY : maxY) + nyZ * (nyZ < 0 ? minZ : maxZ) >= -nyW) {
                    plane = PLANE_PY;
                    inside &= nyX * (nyX < 0 ? maxX : minX) + nyY * (nyY < 0 ? maxY : minY) + nyZ * (nyZ < 0 ? maxZ : minZ) >= -nyW;
                    if ((mask & PLANE_MASK_PY) == 0 || pyX * (pyX < 0 ? minX : maxX) + pyY * (pyY < 0 ? minY : maxY) + pyZ * (pyZ < 0 ? minZ : maxZ) >= -pyW) {
                        plane = PLANE_NZ;
                        inside &= pyX * (pyX < 0 ? maxX : minX) + pyY * (pyY < 0 ? maxY : minY) + pyZ * (pyZ < 0 ? maxZ : minZ) >= -pyW;
                        if ((mask & PLANE_MASK_NZ) == 0 || nzX * (nzX < 0 ? minX : maxX) + nzY * (nzY < 0 ? minY : maxY) + nzZ * (nzZ < 0 ? minZ : maxZ) >= -nzW) {
                            plane = PLANE_PZ;
                            inside &= nzX * (nzX < 0 ? maxX : minX) + nzY * (nzY < 0 ? maxY : minY) + nzZ * (nzZ < 0 ? maxZ : minZ) >= -nzW;
                            if ((mask & PLANE_MASK_PZ) == 0 || pzX * (pzX < 0 ? minX : maxX) + pzY * (pzY < 0 ? minY : maxY) + pzZ * (pzZ < 0 ? minZ : maxZ) >= -pzW) {
                                inside &= pzX * (pzX < 0 ? maxX : minX) + pzY * (pzY < 0 ? maxY : minY) + pzZ * (pzZ < 0 ? maxZ : minZ) >= -pzW;
                                return inside ? INSIDE : INTERSECT;
                            }
                        }
                    }
                }
            }
        }
        return plane;
    }
}
