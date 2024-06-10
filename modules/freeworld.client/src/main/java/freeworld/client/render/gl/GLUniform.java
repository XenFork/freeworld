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

import freeworld.math.Matrix4f;

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
    private final int programId;
    private final GLUniformType type;
    private final int location;
    final MemorySegment value;
    private boolean dirty = true;

    public GLUniform(int programId, GLUniformType type, int location, Arena arena) {
        this.programId = programId;
        this.type = type;
        this.location = location;
        this.value = arena.allocate(type.byteSize());
    }

    private void markDirty() {
        dirty = true;
    }

    public void set(int v) {
        markDirty();
        value.set(ValueLayout.JAVA_INT, 0L, v);
    }

    public void set(float x, float y, float z, float w) {
        markDirty();
        value.set(ValueLayout.JAVA_FLOAT, 0L, x);
        value.set(ValueLayout.JAVA_FLOAT, 4L, y);
        value.set(ValueLayout.JAVA_FLOAT, 8L, z);
        value.set(ValueLayout.JAVA_FLOAT, 12L, w);
    }

    public void set(Matrix4f mat) {
        markDirty();
        mat.get(value);
    }

    public void upload(GLStateMgr gl) {
        if (!dirty) {
            return;
        }
        if (gl.flags().GL_ARB_separate_shader_objects) {
            switch (type) {
                case INT -> gl.programUniform1iv(programId, location, 1, value);
                case VEC4 -> gl.programUniform4fv(programId, location, 1, value);
                case MAT4 -> gl.programUniformMatrix4fv(programId, location, 1, false, value);
            }
        } else {
            gl.setCurrentProgram(programId);
            switch (type) {
                case INT -> gl.uniform1iv(location, 1, value);
                case VEC4 -> gl.uniform4fv(location, 1, value);
                case MAT4 -> gl.uniformMatrix4fv(location, 1, false, value);
            }
        }
        dirty = false;
    }
}
