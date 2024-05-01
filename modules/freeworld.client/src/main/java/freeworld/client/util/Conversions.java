/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.util;

/**
 * Number conversions
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Conversions {
    private Conversions() {
    }

    // color integer

    public static int packRGBA(int red, int green, int blue, int alpha) {
        return ((red & 0xff) << 24) | ((green & 0xff) << 16) | ((blue & 0xff) << 8) | (alpha & 0xff);
    }

    public static int getRedRGBA(int rgba) {
        return rgba >>> 24;
    }

    public static int getGreenRGBA(int rgba) {
        return (rgba >>> 16) & 0xff;
    }

    public static int getBlueRGBA(int rgba) {
        return (rgba >>> 8) & 0xff;
    }

    public static int getAlphaRGBA(int rgba) {
        return rgba & 0xff;
    }

    // color conversion

    public static float colorToFloat(int color) {
        return (float) color / 255f;
    }

    public static int colorToInt(float color) {
        return Math.round(color * 255f);
    }
}
