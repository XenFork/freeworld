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

import freeworld.client.render.gl.GLStateMgr;

import java.lang.foreign.MemorySegment;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexLayout {
    private final Map<String, VertexLayoutElement> elementMap;
    private final List<VertexLayoutElement> elementList;
    private final int stride;

    public VertexLayout(Map<String, VertexLayoutElement> elementMap) {
        this.elementMap = Collections.unmodifiableMap(elementMap);
        this.elementList = List.copyOf(this.elementMap.values());
        this.stride = this.elementList.stream().mapToInt(VertexLayoutElement::byteSize).sum();
    }

    public void bindLocations(GLStateMgr gl, int program) {
        int i = 0;
        for (var entry : elementMap.entrySet()) {
            VertexLayoutElement element = entry.getValue();
            if (element.format() != VertexFormat.PADDING) {
                gl.bindAttribLocation(program, i, entry.getKey());
                i++;
            }
        }
    }

    public void specifyAttributes(GLStateMgr gl) {
        long offset = 0L;
        int i = 0;
        for (var element : elementList) {
            VertexFormat format = element.format();
            if (format != VertexFormat.PADDING) {
                format.specify(
                    gl,
                    i,
                    element.count(),
                    element.dataType().value(),
                    stride,
                    MemorySegment.ofAddress(offset)
                );
                i++;
            }
            offset += element.byteSize();
        }
    }

    public void disableAttributes(GLStateMgr gl) {
        int i = 0;
        for (VertexLayoutElement element : elementList) {
            VertexFormat format = element.format();
            if (format != VertexFormat.PADDING) {
                format.disable(gl, i);
            }
            i++;
        }
    }

    public List<VertexLayoutElement> elements() {
        return elementList;
    }

    public int stride() {
        return stride;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VertexLayout that)) return false;
        return stride == that.stride && Objects.equals(elementMap, that.elementMap) && Objects.equals(elementList, that.elementList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementMap, elementList, stride);
    }

    @Override
    public String toString() {
        return "VertexLayout" + elementMap + stride;
    }
}
