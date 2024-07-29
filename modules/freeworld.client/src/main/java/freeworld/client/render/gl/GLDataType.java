/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.gl;

import overrungl.opengl.GL;

import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDataType {
    BYTE("Byte", "b", GL.BYTE, ValueLayout.JAVA_BYTE),
    UNSIGNED_BYTE("Unsigned Byte", "ub", GL.UNSIGNED_BYTE, ValueLayout.JAVA_BYTE),
    FLOAT("Float", "f", GL.FLOAT, ValueLayout.JAVA_FLOAT);

    private final String stringValue;
    private final String simpleStringValue;
    private final int value;
    private final ValueLayout layout;
    private final int byteSize;

    GLDataType(String stringValue, String simpleStringValue, int value, ValueLayout layout) {
        this.stringValue = stringValue;
        this.simpleStringValue = simpleStringValue;
        this.value = value;
        this.layout = layout;
        this.byteSize = Math.toIntExact(layout.byteSize());
    }

    public String simpleStringValue() {
        return simpleStringValue;
    }

    public int value() {
        return value;
    }

    public ValueLayout layout() {
        return layout;
    }

    public int byteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        return stringValue;
    }
}
