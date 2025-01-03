/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.gl;

import static overrungl.opengl.GL10.GL_LINES;
import static overrungl.opengl.GL10.GL_TRIANGLES;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDrawMode {
    LINES(GL_LINES, 2, 2),
    TRIANGLES(GL_TRIANGLES, 3, 3),
    QUADS(GL_TRIANGLES, 4, 4),
    ;

    private final int value;
    private final int firstCount;
    private final int additionCount;

    GLDrawMode(int value, int firstCount, int additionCount) {
        this.value = value;
        this.firstCount = firstCount;
        this.additionCount = additionCount;
    }

    public int value() {
        return value;
    }

    public int firstCount() {
        return firstCount;
    }

    public int additionCount() {
        return additionCount;
    }

    public int getIndexCount(int vertexCount) {
        return switch (this) {
            case LINES, TRIANGLES -> vertexCount;
            case QUADS -> vertexCount / 4 * 6;
        };
    }
}
