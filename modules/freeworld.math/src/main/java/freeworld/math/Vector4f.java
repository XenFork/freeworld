/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.math;

/**
 * @author squid233
 * @since 0.1.0
 */
public record Vector4f(float x, float y, float z, float w) {
    public static final Vector4f ZERO = new Vector4f(0.0f);

    public Vector4f(float d) {
        this(d, d, d, d);
    }

    public Vector4f mul(Matrix4f mat) {
        int prop = mat.properties();
        if ((prop & Matrix4f.PROPERTY_IDENTITY) != 0)
            return this;
        if ((prop & Matrix4f.PROPERTY_TRANSLATION) != 0)
            return mulTranslation(mat);
        if ((prop & Matrix4f.PROPERTY_AFFINE) != 0)
            return mulAffine(mat);
        return mulGeneric(mat);
    }

    public Vector4f mulTranslation(Matrix4f mat) {
        return new Vector4f(
            Maths.fma(mat.m30(), w, x),
            Maths.fma(mat.m31(), w, y),
            Maths.fma(mat.m32(), w, z),
            w
        );
    }

    public Vector4f mulAffine(Matrix4f mat) {
        return new Vector4f(
            Maths.fma(mat.m00(), x, Maths.fma(mat.m10(), y, Maths.fma(mat.m20(), z, mat.m30() * w))),
            Maths.fma(mat.m01(), x, Maths.fma(mat.m11(), y, Maths.fma(mat.m21(), z, mat.m31() * w))),
            Maths.fma(mat.m02(), x, Maths.fma(mat.m12(), y, Maths.fma(mat.m22(), z, mat.m32() * w))),
            w
        );
    }

    public Vector4f mulGeneric(Matrix4f mat) {
        return new Vector4f(
            Maths.fma(mat.m00(), x, Maths.fma(mat.m10(), y, Maths.fma(mat.m20(), z, mat.m30() * w))),
            Maths.fma(mat.m01(), x, Maths.fma(mat.m11(), y, Maths.fma(mat.m21(), z, mat.m31() * w))),
            Maths.fma(mat.m02(), x, Maths.fma(mat.m12(), y, Maths.fma(mat.m22(), z, mat.m32() * w))),
            Maths.fma(mat.m03(), x, Maths.fma(mat.m13(), y, Maths.fma(mat.m23(), z, mat.m33() * w)))
        );
    }
}
