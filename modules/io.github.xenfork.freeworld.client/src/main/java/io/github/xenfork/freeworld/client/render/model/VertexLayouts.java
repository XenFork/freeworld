/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.model;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.StructLayout;
import java.lang.foreign.ValueLayout;

/**
 * Vertex layouts
 *
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayouts {
    public static final String NAME_POSITION = "Position";
    public static final String NAME_COLOR = "Color";
    public static final StructLayout POSITION_COLOR = MemoryLayout.structLayout(
        MemoryLayout.sequenceLayout(3L, ValueLayout.JAVA_FLOAT).withName(NAME_POSITION),
        MemoryLayout.sequenceLayout(4L, ValueLayout.JAVA_BYTE).withName(NAME_COLOR)
    );

    private VertexLayouts() {
    }
}
