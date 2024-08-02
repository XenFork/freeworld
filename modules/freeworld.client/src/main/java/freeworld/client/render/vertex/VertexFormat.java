/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.vertex;

import freeworld.client.render.gl.GLStateMgr;

import java.lang.foreign.MemorySegment;

/**
 * @author squid233
 * @since 0.1.0
 */
public enum VertexFormat {
    POSITION("Position", (gl, index, size, type, stride, pointer) -> {
        gl.enableVertexAttribArray(index);
        gl.vertexAttribPointer(index, size, type, false, stride, pointer);
    }),
    COLOR("Color", (gl, index, size, type, stride, pointer) -> {
        gl.enableVertexAttribArray(index);
        gl.vertexAttribPointer(index, size, type, true, stride, pointer);
    }),
    UV("UV", (gl, index, size, type, stride, pointer) -> {
        gl.enableVertexAttribArray(index);
        gl.vertexAttribPointer(index, size, type, false, stride, pointer);
    }),
    PADDING("Padding", (_, _, _, _, _, _) -> {
    }),
    ;

    private final String stringName;
    private final Specifier specifier;

    VertexFormat(String stringName, Specifier specifier) {
        this.stringName = stringName;
        this.specifier = specifier;
    }

    @FunctionalInterface
    public interface Specifier {
        void accept(GLStateMgr gl, int index, int size, int type, int stride, MemorySegment pointer);
    }

    public void specify(GLStateMgr gl, int index, int size, int type, int stride, MemorySegment pointer) {
        specifier.accept(gl, index, size, type, stride, pointer);
    }

    @Override
    public String toString() {
        return stringName;
    }
}
