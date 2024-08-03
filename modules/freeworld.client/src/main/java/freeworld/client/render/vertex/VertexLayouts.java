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
import freeworld.util.MapBuilder;

/**
 * Vertex layouts
 *
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayouts {
    public static final VertexLayoutElement POSITION_ELEMENT = new VertexLayoutElement(GLDataType.FLOAT, VertexFormat.POSITION, 3);
    public static final VertexLayoutElement COLOR_ELEMENT = new VertexLayoutElement(GLDataType.UNSIGNED_BYTE, VertexFormat.COLOR, 4);
    public static final VertexLayoutElement UV_ELEMENT = new VertexLayoutElement(GLDataType.FLOAT, VertexFormat.UV, 2);
    public static final VertexLayoutElement PADDING_ELEMENT = new VertexLayoutElement(GLDataType.BYTE, VertexFormat.PADDING, 1);
    public static final VertexLayout POSITION_COLOR = new VertexLayout(
        mapBuilder()
            .add("Position", POSITION_ELEMENT)
            .add("Color", COLOR_ELEMENT)
            .build()
    );
    public static final VertexLayout POSITION_COLOR_TEXTURE = new VertexLayout(
        mapBuilder()
            .add("Position", POSITION_ELEMENT)
            .add("Color", COLOR_ELEMENT)
            .add("UV", UV_ELEMENT)
            .build()
    );
    public static final VertexLayout TEXT = new VertexLayout(
        mapBuilder()
            .add("Position", POSITION_ELEMENT)
            .add("Color", COLOR_ELEMENT)
            .add("UV", UV_ELEMENT)
            .build()
    );

    private VertexLayouts() {
    }

    private static MapBuilder<String, VertexLayoutElement> mapBuilder() {
        return new MapBuilder<>();
    }
}
