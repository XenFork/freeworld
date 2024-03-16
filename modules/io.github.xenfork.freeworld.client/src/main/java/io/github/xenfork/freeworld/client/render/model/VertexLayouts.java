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

import java.util.Map;

/**
 * Vertex layouts
 *
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayouts {
    public static final String NAME_POSITION = "Position";
    public static final String NAME_COLOR = "Color";
    public static final String NAME_UV = "UV";
    public static final VertexLayout POSITION_COLOR = new VertexLayout(Map.of(
        NAME_POSITION, VertexFormat.POSITION,
        NAME_COLOR, VertexFormat.COLOR
    ));
    public static final VertexLayout POSITION_COLOR_TEX = new VertexLayout(Map.of(
        NAME_POSITION, VertexFormat.POSITION,
        NAME_COLOR, VertexFormat.COLOR,
        NAME_UV, VertexFormat.UV
    ));

    private VertexLayouts() {
    }
}
