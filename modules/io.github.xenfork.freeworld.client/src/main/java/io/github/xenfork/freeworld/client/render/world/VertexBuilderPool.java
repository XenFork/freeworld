/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.world;

import io.github.xenfork.freeworld.client.render.builder.VertexBuilder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class VertexBuilderPool<T extends VertexBuilder> {
    private final Map<Integer, Pair<T>> map = new ConcurrentHashMap<>();
    private final Supplier<T> factory;

    public VertexBuilderPool(Supplier<T> factory) {
        this.factory = factory;
    }

    private static final class Pair<T extends VertexBuilder> {
        final T builder;
        final AtomicBoolean acquired;

        Pair(T builder, boolean acquired) {
            this.builder = builder;
            this.acquired = new AtomicBoolean(acquired);
        }
    }

    public T acquire() {
        for (var entry : map.entrySet()) {
            final Pair<T> value = entry.getValue();
            if (!value.acquired.get()) {
                value.acquired.set(true);
                return value.builder;
            }
        }
        final T t = factory.get();
        final int hashCode = System.identityHashCode(t);
        map.put(hashCode, new Pair<>(t, true));
        return t;
    }

    public void release(T t) {
        Objects.requireNonNull(t);
        final int hashCode = System.identityHashCode(t);
        if (map.containsKey(hashCode)) {
            final Pair<T> pair = map.get(hashCode);
            if (pair.acquired.get()) {
                pair.acquired.set(false);
            }
        }
    }
}
