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

/**
 * Vertex layouts
 *
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayouts {
    public static final VertexLayout POSITION_COLOR = new VertexLayout(
        VertexFormat.POSITION,
        VertexFormat.COLOR
    );
    public static final VertexLayout POSITION_COLOR_TEX = new VertexLayout(
        VertexFormat.POSITION,
        VertexFormat.COLOR,
        VertexFormat.UV
    );

    private VertexLayouts() {
    }
}
