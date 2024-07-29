/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.vertex;

import freeworld.client.render.gl.GLDataType;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VertexFormat {
    VertexFormat POSITION = new DefaultVertexFormat("Position", 3, GLDataType.FLOAT, false);
    VertexFormat COLOR = new DefaultVertexFormat("Color", 4, GLDataType.UNSIGNED_BYTE, true);
    VertexFormat UV = new DefaultVertexFormat("UV", 2, GLDataType.FLOAT, false);

    static VertexFormat padding(int size) {
        return new PaddingVertexFormat(size);
    }

    default boolean padding() {
        return false;
    }

    String name();

    int size();

    GLDataType type();

    boolean normalized();

    int usedAttribCount();

    int elementCount();

    int byteSize();
}
