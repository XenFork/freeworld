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

import io.github.xenfork.freeworld.client.render.GameRenderer;
import overrungl.opengl.GL;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayout {
    private final Map<String, VertexFormat> formatMap;
    private final Map<String, Integer> attribLocationMap;
    private final StructLayout layout;

    public VertexLayout(Map<String, VertexFormat> formatMap) {
        this.formatMap = Map.copyOf(formatMap);
        this.attribLocationMap = HashMap.newHashMap(formatMap.size());
        final List<MemoryLayout> elements = new ArrayList<>(formatMap.size());

        int i = 0;
        for (var entry : this.formatMap.entrySet()) {
            final String key = entry.getKey();
            final VertexFormat value = entry.getValue();
            this.attribLocationMap.put(key, i);
            elements.add(value.layout().withName(key));
            i += value.usedAttribCount();
        }

        this.layout = MemoryLayout.structLayout(elements.toArray(MemoryLayout[]::new));
    }

    public void bindLocations(int program) {
        final GL gl = GameRenderer.OpenGL.get();
        attribLocationMap.forEach((name, loc) -> gl.bindAttribLocation(program, loc, name));
    }

    public void enableAttribs() {
        final GL gl = GameRenderer.OpenGL.get();
        attribLocationMap.values().forEach(gl::enableVertexAttribArray);
    }

    public void specifyAttribPointers() {
        final GL gl = GameRenderer.OpenGL.get();
        final int stride = Math.toIntExact(layout().byteSize());
        long offset = 0L;
        for (var entry : formatMap.entrySet()) {
            final VertexFormat format = entry.getValue();
            gl.vertexAttribPointer(attribLocationMap.get(entry.getKey()),
                format.size(),
                format.type().value(),
                format.normalized(),
                stride,
                MemorySegment.ofAddress(offset));
            offset += format.layout().byteSize();
        }
    }

    public int getLocation(String name) {
        return attribLocationMap.get(name);
    }

    public StructLayout layout() {
        return layout;
    }
}
