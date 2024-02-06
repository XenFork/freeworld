/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.gl;

import overrungl.opengl.GL;

import java.lang.foreign.ValueLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDataType {
    UNSIGNED_BYTE(GL.UNSIGNED_BYTE, ValueLayout.JAVA_BYTE),
    FLOAT(GL.FLOAT, ValueLayout.JAVA_FLOAT);

    private final int value;
    private final ValueLayout layout;

    GLDataType(int value, ValueLayout layout) {
        this.value = value;
        this.layout = layout;
    }

    public int value() {
        return value;
    }

    public ValueLayout layout() {
        return layout;
    }
}
