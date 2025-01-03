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

import freeworld.client.FreeworldClient;
import freeworld.client.render.RenderSystem;
import freeworld.math.Matrix4f;
import freeworld.math.Vector4f;

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

    public void set(Vector4f v) {
        set(v.x(), v.y(), v.z(), v.w());
    }

    public void set(Matrix4f mat) {
        markDirty();
        mat.get(value);
    }

    public void specify(GLStateMgr gl) {
        if (!dirty) {
            return;
        }
        if (FreeworldClient.getInstance().glFlags().GL_ARB_separate_shader_objects) {
            switch (type) {
                case INT -> gl.ProgramUniform1iv(program.id(), location, 1, value);
                case VEC4 -> gl.ProgramUniform4fv(program.id(), location, 1, value);
                case MAT4 -> gl.ProgramUniformMatrix4fv(program.id(), location, 1, false, value);
            }
        } else {
            RenderSystem.useProgram(program);
            switch (type) {
                case INT -> gl.Uniform1iv(location, 1, value);
                case VEC4 -> gl.Uniform4fv(location, 1, value);
                case MAT4 -> gl.UniformMatrix4fv(location, 1, false, value);
            }
        }
        dirty = false;
    }
}
