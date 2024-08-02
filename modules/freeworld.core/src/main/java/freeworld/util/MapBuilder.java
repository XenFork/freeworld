/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A map builder backed with {@link LinkedHashMap} which is insertion-ordered.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class MapBuilder<K, V> {
    private final Map<K, V> map = new LinkedHashMap<>();

    public MapBuilder<K, V> add(K key, V value) {
        map.put(key, value);
        return this;
    }

    public Map<K, V> build() {
        return Collections.unmodifiableMap(map);
    }
}
