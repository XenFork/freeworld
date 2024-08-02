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

import freeworld.client.render.gl.GLDataType;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayoutElement {
    private final GLDataType dataType;
    private final VertexFormat format;
    private final int count;
    private final int byteSize;

    public VertexLayoutElement(GLDataType dataType, VertexFormat format, int count) {
        this.dataType = dataType;
        this.format = format;
        this.count = count;
        this.byteSize = dataType.byteSize() * count;
    }

    public GLDataType dataType() {
        return dataType;
    }

    public VertexFormat format() {
        return format;
    }

    public int count() {
        return count;
    }

    public int byteSize() {
        return byteSize;
    }

    @Override
    public String toString() {
        return "VertexLayoutElement(" + format + ")" + count + dataType.simpleStringValue();
    }
}
