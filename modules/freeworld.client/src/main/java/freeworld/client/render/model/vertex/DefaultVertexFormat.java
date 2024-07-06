/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.model.vertex;

import freeworld.client.render.gl.GLDataType;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SequenceLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public record DefaultVertexFormat(String name, int size, GLDataType type, boolean normalized) implements VertexFormat {
    @Override
    public int usedAttribCount() {
        return 1;
    }

    @Override
    public SequenceLayout layout() {
        return MemoryLayout.sequenceLayout(size, type.layout());
    }
}
