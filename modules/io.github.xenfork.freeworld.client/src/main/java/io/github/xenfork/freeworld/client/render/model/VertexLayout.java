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
    private final List<VertexFormat> formats;
    private final Map<String, Integer> attribLocationMap;
    private final StructLayout layout;

    public VertexLayout(List<VertexFormat> formats) {
        this.formats = List.copyOf(formats);
        this.attribLocationMap = HashMap.newHashMap(this.formats.size());
        final List<MemoryLayout> elements = new ArrayList<>(this.formats.size());

        int i = 0;
        for (var format : this.formats) {
            final String name = format.name();
            this.attribLocationMap.put(name, i);
            elements.add(format.layout().withName(name));
            i += format.usedAttribCount();
        }

        this.layout = MemoryLayout.structLayout(elements.toArray(MemoryLayout[]::new));
    }

    public VertexLayout(VertexFormat... formats) {
        this(List.of(formats));
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
        for (var format : formats) {
            gl.vertexAttribPointer(attribLocationMap.get(format.name()),
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

    public List<VertexFormat> formats() {
        return formats;
    }

    public StructLayout layout() {
        return layout;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexLayout that)) return false;
        return Objects.equals(formats, that.formats) && Objects.equals(attribLocationMap, that.attribLocationMap) && Objects.equals(layout, that.layout);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formats, attribLocationMap, layout);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VertexLayout.class.getSimpleName() + "[", "]")
            .add("formats=" + formats)
            .add("attribLocationMap=" + attribLocationMap)
            .add("layout=" + layout)
            .toString();
    }
}
