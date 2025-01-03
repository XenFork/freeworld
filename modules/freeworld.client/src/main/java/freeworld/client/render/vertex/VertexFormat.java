/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2025  XenFork Union
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
        gl.EnableVertexAttribArray(index);
        gl.VertexAttribPointer(index, size, type, false, stride, pointer);
    }, GLStateMgr::DisableVertexAttribArray),
    COLOR("Color", (gl, index, size, type, stride, pointer) -> {
        gl.EnableVertexAttribArray(index);
        gl.VertexAttribPointer(index, size, type, true, stride, pointer);
    }, GLStateMgr::DisableVertexAttribArray),
    UV("UV", (gl, index, size, type, stride, pointer) -> {
        gl.EnableVertexAttribArray(index);
        gl.VertexAttribPointer(index, size, type, false, stride, pointer);
    }, GLStateMgr::DisableVertexAttribArray),
    PADDING("Padding", (_, _, _, _, _, _) -> {
    }, (_, _) -> {
    }),
    ;

    private final String stringName;
    private final Specifier specifier;
    private final Disabler disabler;

    VertexFormat(String stringName, Specifier specifier, Disabler disabler) {
        this.stringName = stringName;
        this.specifier = specifier;
        this.disabler = disabler;
    }

    @FunctionalInterface
    public interface Specifier {
        void accept(GLStateMgr gl, int index, int size, int type, int stride, MemorySegment pointer);
    }

    @FunctionalInterface
    public interface Disabler {
        void disable(GLStateMgr gl, int index);
    }

    public void specify(GLStateMgr gl, int index, int size, int type, int stride, MemorySegment pointer) {
        specifier.accept(gl, index, size, type, stride, pointer);
    }

    public void disable(GLStateMgr gl, int index) {
        disabler.disable(gl, index);
    }

    @Override
    public String toString() {
        return stringName;
    }
}
