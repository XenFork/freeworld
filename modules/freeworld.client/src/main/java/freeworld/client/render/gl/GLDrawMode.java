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

/**
 * @author squid233
 * @since 0.1.0
 */
public enum GLDrawMode {
    LINES(GLStateMgr.LINES, 2, 2),
    TRIANGLES(GLStateMgr.TRIANGLES, 3, 3),
    QUADS(GLStateMgr.TRIANGLES, 4, 4),
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
