/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gl;

import overrungl.opengl.GL;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDataType {
    BYTE("Byte", "b", GL.BYTE, Byte.BYTES),
    UNSIGNED_BYTE("Unsigned Byte", "ub", GL.UNSIGNED_BYTE, Byte.BYTES),
    FLOAT("Float", "f", GL.FLOAT, Float.BYTES);

    private final String stringValue;
    private final String simpleStringValue;
    private final int value;
    private final int byteSize;

    GLDataType(String stringValue, String simpleStringValue, int value, int byteSize) {
        this.stringValue = stringValue;
        this.simpleStringValue = simpleStringValue;
        this.value = value;
        this.byteSize = byteSize;
    }

    public String simpleStringValue() {
        return simpleStringValue;
    }

    public int value() {
        return value;
    }

    public int byteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
