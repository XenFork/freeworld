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

import io.github.xenfork.freeworld.client.render.GameRenderer;
import org.joml.Matrix4fc;
import overrungl.joml.Matrixn;
import overrungl.opengl.GL;
import overrungl.opengl.GLFlags;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

/**
 * The OpenGL uniform
 *
 * @author squid233
 * @since 0.1.0
 */
public final class GLUniform {
    private final GLProgram program;
    private final GLUniformType type;
    private final int location;
    final MemorySegment value;
    private boolean dirty = true;

    public GLUniform(GLProgram program, GLUniformType type, int location, Arena arena) {
        this.program = program;
        this.type = type;
        this.location = location;
        this.value = arena.allocate(type.byteSize());
    }

    private void markDirty() {
        dirty = true;
    }

    public void set(float x, float y, float z, float w) {
        markDirty();
        value.set(ValueLayout.JAVA_FLOAT, 0L, x);
        value.set(ValueLayout.JAVA_FLOAT, 4L, y);
        value.set(ValueLayout.JAVA_FLOAT, 8L, z);
        value.set(ValueLayout.JAVA_FLOAT, 12L, w);
    }

    public void set(Matrix4fc mat) {
        markDirty();
        Matrixn.put(mat, value);
    }

    public void upload() {
        if (!dirty) {
            return;
        }
        final GLFlags glFlags = GameRenderer.OpenGLFlags.get();
        final GL gl = GameRenderer.OpenGL.get();
        if (glFlags.GL_ARB_separate_shader_objects) {
            switch (type) {
                case VEC4 -> gl.programUniform4fv(program.id(), location, 1, value);
                case MAT4 -> gl.programUniformMatrix4fv(program.id(), location, 1, false, value);
            }
        } else {
            program.use();
            switch (type) {
                case VEC4 -> gl.uniform4fv(location, 1, value);
                case MAT4 -> gl.uniformMatrix4fv(location, 1, false, value);
            }
            gl.useProgram(0);
        }
        dirty = false;
    }
}
