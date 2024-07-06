/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.core.registry;

import freeworld.core.Identifier;

import java.util.Map;

/**
 * An immutable view of {@link MutableRegistry}.
 *
 * @param <T> the type of the entry.
 * @author squid233
 * @since 0.1.0
 */
public interface Registry<T> extends Iterable<Map.Entry<Identifier, T>> {
    static <T> T register(MutableRegistry<? super T> registry, Identifier id, int rawId, T entry) {
        return registry.set(id, rawId, entry);
    }

    static <T> T register(MutableRegistry<? super T> registry, Identifier id, T entry) {
        return registry.add(id, entry);
    }

    static <T> T register(MutableRegistry<? super T> registry, String id, T entry) {
        return register(registry, Identifier.of(id), entry);
    }

    Identifier registryName();

    /**
     * {@return the entry associated with the given identifier}
     *
     * @param identifier the identifier
     */
    T get(Identifier identifier);

    T getByRawId(int rawId);

    Identifier getId(T entry);

    int size();
}
