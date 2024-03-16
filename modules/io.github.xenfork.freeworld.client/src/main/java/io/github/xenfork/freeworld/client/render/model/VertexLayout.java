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

import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;

import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.StructLayout;
import java.util.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayout {
    private final Map<String, VertexFormat> formatMap;
    private final Map<String, Integer> attribLocationMap;
    private final StructLayout layout;

    public VertexLayout(Map<String, VertexFormat> formatMap) {
        this.formatMap = formatMap;
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

    @SafeVarargs
    public VertexLayout(Map.Entry<String, VertexFormat>... formats) {
        final Map<String, VertexFormat> map = LinkedHashMap.newLinkedHashMap(formats.length);
        for (var format : formats) {
            map.put(format.getKey(), format.getValue());
        }
        this(map);
    }

    public void bindLocations(GLStateMgr gl, int program) {
        for (var entry : attribLocationMap.entrySet()) {
            gl.bindAttribLocation(program, entry.getValue(), entry.getKey());
        }
    }

    public void enableAttribs(GLStateMgr gl) {
        attribLocationMap.values().forEach(gl::enableVertexAttribArray);
    }

    public void specifyAttribPointers(GLStateMgr gl) {
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
