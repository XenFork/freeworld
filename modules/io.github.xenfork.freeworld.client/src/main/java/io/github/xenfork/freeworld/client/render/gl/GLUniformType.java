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

import java.util.Locale;

/**
 * The type of the GLUniform
 *
 * @author squid233
 * @since 0.1.0
 */
public enum GLUniformType {
    VEC4(4 * 4),
    MAT4(4 * 4 * 4);

    private final long byteSize;

    GLUniformType(long byteSize) {
        this.byteSize = byteSize;
    }

    public static GLUniformType fromString(String name) {
        if (name == null) return null;
        return switch (name.toLowerCase(Locale.ROOT)) {
            case "vec4" -> VEC4;
            case "mat4" -> MAT4;
            default -> null;
        };
    }

    public long byteSize() {
        return byteSize;
    }
}
