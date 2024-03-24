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

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDrawMode {
    LINES(GLStateMgr.LINES, 2),
    TRIANGLES(GLStateMgr.TRIANGLES, 3);

    private final int value;
    private final int count;

    GLDrawMode(int value, int count) {
        this.value = value;
        this.count = count;
    }

    public int value() {
        return value;
    }

    public int count() {
        return count;
    }
}
