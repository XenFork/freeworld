/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.model;

import freeworld.client.render.gl.GLDataType;

import java.lang.foreign.SequenceLayout;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface VertexFormat {
    VertexFormat POSITION = new DefaultVertexFormat("Position", 3, GLDataType.FLOAT, false);
    VertexFormat COLOR = new DefaultVertexFormat("Color", 4, GLDataType.UNSIGNED_BYTE, true);
    VertexFormat UV = new DefaultVertexFormat("UV", 2, GLDataType.FLOAT, false);

    String name();

    int size();

    GLDataType type();

    boolean normalized();

    int usedAttribCount();

    SequenceLayout layout();
}
