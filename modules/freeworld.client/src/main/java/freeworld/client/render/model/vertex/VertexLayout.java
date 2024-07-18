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

import freeworld.client.render.gl.GLStateMgr;

import java.lang.foreign.MemorySegment;
import java.util.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayout {
    private final List<VertexFormat> formats;
    private final Map<String, Integer> attribLocationMap;
    private final int stride;

    public VertexLayout(List<VertexFormat> formats) {
        this.formats = List.copyOf(formats);
        this.attribLocationMap = HashMap.newHashMap(this.formats.size());

        int i = 0;
        int stride = 0;
        for (var format : this.formats) {
            if (!format.padding()) {
                final String name = format.name();
                this.attribLocationMap.put(name, i);
                i += format.usedAttribCount();
            }
            stride += format.byteSize();
        }
        this.stride = stride;
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
        long offset = 0L;
        for (var format : formats) {
            if (!format.padding()) {
                gl.vertexAttribPointer(attribLocationMap.get(format.name()),
                    format.size(),
                    format.type().value(),
                    format.normalized(),
                    stride,
                    MemorySegment.ofAddress(offset));
            }
            offset += format.byteSize();
        }
    }

    public int getLocation(String name) {
        return attribLocationMap.get(name);
    }

    public List<VertexFormat> formats() {
        return formats;
    }

    public int stride() {
        return stride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexLayout that)) return false;
        return stride == that.stride && Objects.equals(formats, that.formats) && Objects.equals(attribLocationMap, that.attribLocationMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(formats, attribLocationMap, stride);
    }

    @Override
    public String toString() {
        return "VertexLayout" + formats + "b" + stride;
    }
}
