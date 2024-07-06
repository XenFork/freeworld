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

import java.lang.foreign.MemorySegment;

import static java.lang.foreign.ValueLayout.JAVA_FLOAT;

/**
 * @author squid233
 * @since 0.1.0
 */
public record Matrix4f(
    int properties,
    float m00, float m01, float m02, float m03,
    float m10, float m11, float m12, float m13,
    float m20, float m21, float m22, float m23,
    float m30, float m31, float m32, float m33
) {
    /**
     * Bit returned by {@link #properties()} to indicate that the matrix represents an unknown transformation.
     */
    public static final byte PROPERTY_UNKNOWN = 0;
    /**
     * Bit returned by {@link #properties()} to indicate that the matrix represents a perspective transformation.
     */
    public static final byte PROPERTY_PERSPECTIVE = 1 << 0;
    /**
     * Bit returned by {@link #properties()} to indicate that the matrix represents an affine transformation.
     */
    public static final byte PROPERTY_AFFINE = 1 << 1;
    /**
     * Bit returned by {@link #properties()} to indicate that the matrix represents the identity transformation.
     * This implies {@link #PROPERTY_AFFINE}, {@link #PROPERTY_TRANSLATION} and {@link #PROPERTY_ORTHONORMAL}.
     */
    public static final byte PROPERTY_IDENTITY = 1 << 2;
    /**
     * Bit returned by {@link #properties()} to indicate that the matrix represents a pure translation transformation.
     * This implies {@link #PROPERTY_AFFINE} and {@link #PROPERTY_ORTHONORMAL}.
     */
    public static final byte PROPERTY_TRANSLATION = 1 << 3;
    /**
     * Bit returned by {@link #properties()} to indicate that the upper-left 3x3 submatrix represents an orthogonal
     * matrix (i.e. orthonormal basis). For practical reasons, this property also always implies
     * {@link #PROPERTY_AFFINE} in this implementation.
     */
    public static final byte PROPERTY_ORTHONORMAL = 1 << 4;
    public static final Matrix4f IDENTITY = new Matrix4f(
        PROPERTY_IDENTITY | PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL,
        1.0f, 0.0f, 0.0f, 0.0f,
        0.0f, 1.0f, 0.0f, 0.0f,
        0.0f, 0.0f, 1.0f, 0.0f,
        0.0f, 0.0f, 0.0f, 1.0f
    );

    public Matrix4f(
        float m00, float m01, float m02, float m03,
        float m10, float m11, float m12, float m13,
        float m20, float m21, float m22, float m23,
        float m30, float m31, float m32, float m33
    ) {
        this(
            determineProperties(m00, m01, m02, m03, m10, m11, m12, m13, m20, m21, m22, m23, m30, m31, m32, m33),
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            m30, m31, m32, m33
        );
    }

    private static int determineProperties(
        float m00, float m01, float m02, float m03,
        float m10, float m11, float m12, float m13,
        float m20, float m21, float m22, float m23,
        float m30, float m31, float m32, float m33
    ) {
        int properties = PROPERTY_UNKNOWN;
        if (m03 == 0.0f && m13 == 0.0f) {
            if (m23 == 0.0f && m33 == 1.0f) {
                properties |= PROPERTY_AFFINE;
                if (m00 == 1.0f && m01 == 0.0f && m02 == 0.0f && m10 == 0.0f && m11 == 1.0f && m12 == 0.0f
                    && m20 == 0.0f && m21 == 0.0f && m22 == 1.0f) {
                    properties |= PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL;
                    if (m30 == 0.0f && m31 == 0.0f && m32 == 0.0f)
                        properties |= PROPERTY_IDENTITY;
                }
                /*
                 * We do not determine orthogonality, since it would require arbitrary epsilons
                 * and is rather expensive (6 dot products) in the worst case.
                 */
            } else if (m01 == 0.0f && m02 == 0.0f && m10 == 0.0f && m12 == 0.0f && m20 == 0.0f && m21 == 0.0f
                       && m30 == 0.0f && m31 == 0.0f && m33 == 0.0f) {
                properties |= PROPERTY_PERSPECTIVE;
            }
        }
        return properties;
    }

    //region get

    public MemorySegment get(MemorySegment dest) {
        dest.set(JAVA_FLOAT, 0L, m00);
        dest.set(JAVA_FLOAT, 4L, m01);
        dest.set(JAVA_FLOAT, 8L, m02);
        dest.set(JAVA_FLOAT, 12L, m03);
        dest.set(JAVA_FLOAT, 16L, m10);
        dest.set(JAVA_FLOAT, 20L, m11);
        dest.set(JAVA_FLOAT, 24L, m12);
        dest.set(JAVA_FLOAT, 28L, m13);
        dest.set(JAVA_FLOAT, 32L, m20);
        dest.set(JAVA_FLOAT, 36L, m21);
        dest.set(JAVA_FLOAT, 40L, m22);
        dest.set(JAVA_FLOAT, 44L, m23);
        dest.set(JAVA_FLOAT, 48L, m30);
        dest.set(JAVA_FLOAT, 52L, m31);
        dest.set(JAVA_FLOAT, 56L, m32);
        dest.set(JAVA_FLOAT, 60L, m33);
        return dest;
    }

    //endregion

    //region mul

    public Matrix4f mul(Matrix4f right) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return right;
        else if ((right.properties() & PROPERTY_IDENTITY) != 0)
            return this;
        else if ((properties & PROPERTY_TRANSLATION) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulTranslationAffine(right);
        else if ((properties & PROPERTY_AFFINE) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulAffine(right);
        else if ((properties & PROPERTY_PERSPECTIVE) != 0 && (right.properties() & PROPERTY_AFFINE) != 0)
            return mulPerspectiveAffine(right);
        else if ((right.properties() & PROPERTY_AFFINE) != 0)
            return mulAffineR(right);
        return mul0(right);
    }

    public Matrix4f mulTranslationAffine(Matrix4f right) {
        return new Matrix4f(
            PROPERTY_AFFINE | (right.properties() & PROPERTY_ORTHONORMAL),
            right.m00(),
            right.m01(),
            right.m02(),
            m03(),
            right.m10(),
            right.m11(),
            right.m12(),
            m13(),
            right.m20(),
            right.m21(),
            right.m22(),
            m23(),
            right.m30() + m30(),
            right.m31() + m31(),
            right.m32() + m32(),
            m33()
        );
    }

    public Matrix4f mulAffine(Matrix4f right) {
        float m00 = this.m00(), m01 = this.m01(), m02 = this.m02();
        float m10 = this.m10(), m11 = this.m11(), m12 = this.m12();
        float m20 = this.m20(), m21 = this.m21(), m22 = this.m22();
        float rm00 = right.m00(), rm01 = right.m01(), rm02 = right.m02();
        float rm10 = right.m10(), rm11 = right.m11(), rm12 = right.m12();
        float rm20 = right.m20(), rm21 = right.m21(), rm22 = right.m22();
        float rm30 = right.m30(), rm31 = right.m31(), rm32 = right.m32();
        return new Matrix4f(
            PROPERTY_AFFINE | (this.properties & right.properties() & PROPERTY_ORTHONORMAL),
            Maths.fma(m00, rm00, Maths.fma(m10, rm01, m20 * rm02)),
            Maths.fma(m01, rm00, Maths.fma(m11, rm01, m21 * rm02)),
            Maths.fma(m02, rm00, Maths.fma(m12, rm01, m22 * rm02)),
            m03(),
            Maths.fma(m00, rm10, Maths.fma(m10, rm11, m20 * rm12)),
            Maths.fma(m01, rm10, Maths.fma(m11, rm11, m21 * rm12)),
            Maths.fma(m02, rm10, Maths.fma(m12, rm11, m22 * rm12)),
            m13(),
            Maths.fma(m00, rm20, Maths.fma(m10, rm21, m20 * rm22)),
            Maths.fma(m01, rm20, Maths.fma(m11, rm21, m21 * rm22)),
            Maths.fma(m02, rm20, Maths.fma(m12, rm21, m22 * rm22)),
            m23(),
            Maths.fma(m00, rm30, Maths.fma(m10, rm31, Maths.fma(m20, rm32, m30()))),
            Maths.fma(m01, rm30, Maths.fma(m11, rm31, Maths.fma(m21, rm32, m31()))),
            Maths.fma(m02, rm30, Maths.fma(m12, rm31, Maths.fma(m22, rm32, m32()))),
            m33()
        );
    }

    public Matrix4f mulPerspectiveAffine(Matrix4f view) {
        return new Matrix4f(PROPERTY_UNKNOWN,
            m00() * view.m00(),
            m11() * view.m01(),
            m22() * view.m02(),
            m23() * view.m02(),
            m00() * view.m10(),
            m11() * view.m11(),
            m22() * view.m12(),
            m23() * view.m12(),
            m00() * view.m20(),
            m11() * view.m21(),
            m22() * view.m22(),
            m23() * view.m22(),
            m00() * view.m30(),
            m11() * view.m31(),
            m22() * view.m32() + m32(),
            m23() * view.m32()
        );
    }

    public Matrix4f mulAffineR(Matrix4f right) {
        return new Matrix4f(
            properties & ~(PROPERTY_IDENTITY | PROPERTY_PERSPECTIVE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL),
            Maths.fma(m00(), right.m00(), Maths.fma(m10(), right.m01(), m20() * right.m02())),
            Maths.fma(m01(), right.m00(), Maths.fma(m11(), right.m01(), m21() * right.m02())),
            Maths.fma(m02(), right.m00(), Maths.fma(m12(), right.m01(), m22() * right.m02())),
            Maths.fma(m03(), right.m00(), Maths.fma(m13(), right.m01(), m23() * right.m02())),
            Maths.fma(m00(), right.m10(), Maths.fma(m10(), right.m11(), m20() * right.m12())),
            Maths.fma(m01(), right.m10(), Maths.fma(m11(), right.m11(), m21() * right.m12())),
            Maths.fma(m02(), right.m10(), Maths.fma(m12(), right.m11(), m22() * right.m12())),
            Maths.fma(m03(), right.m10(), Maths.fma(m13(), right.m11(), m23() * right.m12())),
            Maths.fma(m00(), right.m20(), Maths.fma(m10(), right.m21(), m20() * right.m22())),
            Maths.fma(m01(), right.m20(), Maths.fma(m11(), right.m21(), m21() * right.m22())),
            Maths.fma(m02(), right.m20(), Maths.fma(m12(), right.m21(), m22() * right.m22())),
            Maths.fma(m03(), right.m20(), Maths.fma(m13(), right.m21(), m23() * right.m22())),
            Maths.fma(m00(), right.m30(), Maths.fma(m10(), right.m31(), Maths.fma(m20(), right.m32(), m30()))),
            Maths.fma(m01(), right.m30(), Maths.fma(m11(), right.m31(), Maths.fma(m21(), right.m32(), m31()))),
            Maths.fma(m02(), right.m30(), Maths.fma(m12(), right.m31(), Maths.fma(m22(), right.m32(), m32()))),
            Maths.fma(m03(), right.m30(), Maths.fma(m13(), right.m31(), Maths.fma(m23(), right.m32(), m33())))
        );
    }

    public Matrix4f mul0(Matrix4f right) {
        return new Matrix4f(PROPERTY_UNKNOWN,
            Maths.fma(m00(), right.m00(), Maths.fma(m10(), right.m01(), Maths.fma(m20(), right.m02(), m30() * right.m03()))),
            Maths.fma(m01(), right.m00(), Maths.fma(m11(), right.m01(), Maths.fma(m21(), right.m02(), m31() * right.m03()))),
            Maths.fma(m02(), right.m00(), Maths.fma(m12(), right.m01(), Maths.fma(m22(), right.m02(), m32() * right.m03()))),
            Maths.fma(m03(), right.m00(), Maths.fma(m13(), right.m01(), Maths.fma(m23(), right.m02(), m33() * right.m03()))),
            Maths.fma(m00(), right.m10(), Maths.fma(m10(), right.m11(), Maths.fma(m20(), right.m12(), m30() * right.m13()))),
            Maths.fma(m01(), right.m10(), Maths.fma(m11(), right.m11(), Maths.fma(m21(), right.m12(), m31() * right.m13()))),
            Maths.fma(m02(), right.m10(), Maths.fma(m12(), right.m11(), Maths.fma(m22(), right.m12(), m32() * right.m13()))),
            Maths.fma(m03(), right.m10(), Maths.fma(m13(), right.m11(), Maths.fma(m23(), right.m12(), m33() * right.m13()))),
            Maths.fma(m00(), right.m20(), Maths.fma(m10(), right.m21(), Maths.fma(m20(), right.m22(), m30() * right.m23()))),
            Maths.fma(m01(), right.m20(), Maths.fma(m11(), right.m21(), Maths.fma(m21(), right.m22(), m31() * right.m23()))),
            Maths.fma(m02(), right.m20(), Maths.fma(m12(), right.m21(), Maths.fma(m22(), right.m22(), m32() * right.m23()))),
            Maths.fma(m03(), right.m20(), Maths.fma(m13(), right.m21(), Maths.fma(m23(), right.m22(), m33() * right.m23()))),
            Maths.fma(m00(), right.m30(), Maths.fma(m10(), right.m31(), Maths.fma(m20(), right.m32(), m30() * right.m33()))),
            Maths.fma(m01(), right.m30(), Maths.fma(m11(), right.m31(), Maths.fma(m21(), right.m32(), m31() * right.m33()))),
            Maths.fma(m02(), right.m30(), Maths.fma(m12(), right.m31(), Maths.fma(m22(), right.m32(), m32() * right.m33()))),
            Maths.fma(m03(), right.m30(), Maths.fma(m13(), right.m31(), Maths.fma(m23(), right.m32(), m33() * right.m33())))
        );
    }

    //endregion

    //region translate

    public static Matrix4f translation(float x, float y, float z) {
        return new Matrix4f(
            PROPERTY_AFFINE | PROPERTY_TRANSLATION | PROPERTY_ORTHONORMAL,
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            x, y, z, 1.0f
        );
    }

    public Matrix4f translate(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return translation(x, y, z);
        return translateGeneric(x, y, z);
    }

    private Matrix4f translateGeneric(float x, float y, float z) {
        return new Matrix4f(
            properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY),
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            Maths.fma(m00(), x, Maths.fma(m10(), y, Maths.fma(m20(), z, m30()))),
            Maths.fma(m01(), x, Maths.fma(m11(), y, Maths.fma(m21(), z, m31()))),
            Maths.fma(m02(), x, Maths.fma(m12(), y, Maths.fma(m22(), z, m32()))),
            Maths.fma(m03(), x, Maths.fma(m13(), y, Maths.fma(m23(), z, m33())))
        );
    }

    public Matrix4f setTranslation(float x, float y, float z) {
        return new Matrix4f(
            properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY),
            m00, m01, m02, m03,
            m10, m11, m12, m13,
            m20, m21, m22, m23,
            x, y, z, m33
        );
    }

    //endregion

    //region rotate

    public static Matrix4f rotationX(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = (float) Math.cos(ang);
        return new Matrix4f(
            PROPERTY_AFFINE | PROPERTY_ORTHONORMAL,
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, cos, sin, 0.0f,
            0.0f, -sin, cos, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    public Matrix4f rotateX(float ang) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return rotationX(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30(), y = m31(), z = m32();
            return rotationX(ang).setTranslation(x, y, z);
        }
        return rotateXInternal(ang);
    }

    private Matrix4f rotateXInternal(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = (float) Math.cos(ang);
        float lm10 = m10(), lm11 = m11(), lm12 = m12(), lm13 = m13(), lm20 = m20(), lm21 = m21(), lm22 = m22(), lm23 = m23();
        return new Matrix4f(
            properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION),
            m00(),
            m01(),
            m02(),
            m03(),
            Maths.fma(lm10, cos, lm20 * sin),
            Maths.fma(lm11, cos, lm21 * sin),
            Maths.fma(lm12, cos, lm22 * sin),
            Maths.fma(lm13, cos, lm23 * sin),
            Maths.fma(lm10, -sin, lm20 * cos),
            Maths.fma(lm11, -sin, lm21 * cos),
            Maths.fma(lm12, -sin, lm22 * cos),
            Maths.fma(lm13, -sin, lm23 * cos),
            m30(),
            m31(),
            m32(),
            m33()
        );
    }

    public static Matrix4f rotationY(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = (float) Math.cos(ang);
        return new Matrix4f(
            PROPERTY_AFFINE | PROPERTY_ORTHONORMAL,
            cos, 0.0f, -sin, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            sin, 0.0f, cos, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    public Matrix4f rotateY(float ang) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return rotationY(ang);
        else if ((properties & PROPERTY_TRANSLATION) != 0) {
            float x = m30(), y = m31(), z = m32();
            return rotationY(ang).setTranslation(x, y, z);
        }
        return rotateYInternal(ang);
    }

    private Matrix4f rotateYInternal(float ang) {
        float sin = (float) Math.sin(ang);
        float cos = (float) Math.cos(ang);
        // add temporaries for dependent values
        float nm00 = Maths.fma(m00(), cos, m20() * -sin);
        float nm01 = Maths.fma(m01(), cos, m21() * -sin);
        float nm02 = Maths.fma(m02(), cos, m22() * -sin);
        float nm03 = Maths.fma(m03(), cos, m23() * -sin);
        return new Matrix4f(
            properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION),
            nm00,
            nm01,
            nm02,
            nm03,
            m10(),
            m11(),
            m12(),
            m13(),
            Maths.fma(m00(), sin, m20() * cos),
            Maths.fma(m01(), sin, m21() * cos),
            Maths.fma(m02(), sin, m22() * cos),
            Maths.fma(m03(), sin, m23() * cos),
            m30(),
            m31(),
            m32(),
            m33()
        );
    }

    //endregion

    //region scale
    public static Matrix4f scaling(float x, float y, float z) {
        boolean one = Maths.absEqualsOne(x) && Maths.absEqualsOne(y) && Maths.absEqualsOne(z);
        return new Matrix4f(
            PROPERTY_AFFINE | (one ? PROPERTY_ORTHONORMAL : 0),
            x, 0.0f, 0.0f, 0.0f,
            0.0f, y, 0.0f, 0.0f,
            0.0f, 0.0f, z, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f
        );
    }

    public Matrix4f scale(float x, float y, float z) {
        if ((properties & PROPERTY_IDENTITY) != 0)
            return scaling(x, y, z);
        return scaleGeneric(x, y, z);
    }

    private Matrix4f scaleGeneric(float x, float y, float z) {
        boolean one = Maths.absEqualsOne(x) && Maths.absEqualsOne(y) && Maths.absEqualsOne(z);
        return new Matrix4f(
            properties & ~(PROPERTY_PERSPECTIVE | PROPERTY_IDENTITY | PROPERTY_TRANSLATION
                           | (one ? 0 : PROPERTY_ORTHONORMAL)),
            m00() * x,
            m01() * x,
            m02() * x,
            m03() * x,
            m10() * y,
            m11() * y,
            m12() * y,
            m13() * y,
            m20() * z,
            m21() * z,
            m22() * z,
            m23() * z,
            m30(),
            m31(),
            m32(),
            m33()
        );
    }

    public Matrix4f scale(float xyz) {
        return scale(xyz, xyz, xyz);
    }

    //endregion

    //region perspective

    public static Matrix4f setPerspective(float fovy, float aspect, float zNear, float zFar, boolean zZeroToOne) {
        float h = (float) Math.tan(fovy * 0.5f);
        float m00 = 1.0f / (h * aspect);
        float m11 = 1.0f / h;
        boolean farInf = zFar > 0 && Float.isInfinite(zFar);
        boolean nearInf = zNear > 0 && Float.isInfinite(zNear);
        float m22;
        float m32;
        if (farInf) {
            // See: "Infinite Projection Matrix" (http://www.terathon.com/gdc07_lengyel.pdf)
            float e = 1E-6f;
            m22 = e - 1.0f;
            m32 = (e - (zZeroToOne ? 1.0f : 2.0f)) * zNear;
        } else if (nearInf) {
            float e = 1E-6f;
            m22 = (zZeroToOne ? 0.0f : 1.0f) - e;
            m32 = ((zZeroToOne ? 1.0f : 2.0f) - e) * zFar;
        } else {
            m22 = (zZeroToOne ? zFar : zFar + zNear) / (zNear - zFar);
            m32 = (zZeroToOne ? zFar : zFar + zFar) * zNear / (zNear - zFar);
        }
        return new Matrix4f(PROPERTY_PERSPECTIVE,
            m00, 0.0f, 0.0f, 0.0f,
            0.0f, m11, 0.0f, 0.0f,
            0.0f, 0.0f, m22, -1.0f,
            0.0f, 0.0f, m32, 0.0f
        );
    }

    public static Matrix4f setPerspective(float fovy, float aspect, float zNear, float zFar) {
        return setPerspective(fovy, aspect, zNear, zFar, false);
    }

    //endregion

    //region ortho

    public static Matrix4f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar, boolean zZeroToOne) {
        return new Matrix4f(
            PROPERTY_AFFINE,
            2.0f / (right - left), 0.0f, 0.0f, 0.0f,
            0.0f, 2.0f / (top - bottom), 0.0f, 0.0f,
            0.0f, 0.0f, (zZeroToOne ? 1.0f : 2.0f) / (zNear - zFar), 0.0f,
            (right + left) / (left - right), (top + bottom) / (bottom - top), (zZeroToOne ? zNear : (zFar + zNear)) / (zNear - zFar), 1.0f
        );
    }

    public static Matrix4f setOrtho(float left, float right, float bottom, float top, float zNear, float zFar) {
        return setOrtho(left, right, bottom, top, zNear, zFar, false);
    }

    //endregion
}
