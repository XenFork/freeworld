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
public final class Maths {
    private Maths() {
    }

    static boolean absEqualsOne(float r) {
        return (Float.floatToRawIntBits(r) & 0x7FFFFFFF) == 0x3F800000;
    }

    public static double fma(double a, double b, double c) {
        return a * b + c;
    }

    public static float fma(float a, float b, float c) {
        return a * b + c;
    }
}