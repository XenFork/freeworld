/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.registry;

import freeworld.util.Identifier;

/**
 * A mutable registry that allows modifying.
 *
 * @param <T> the type of the entry.
 * @author squid233
 * @since 0.1.0
 */
public interface MutableRegistry<T> extends Registry<T> {
    <R extends T> R set(Identifier identifier, int rawId, R entry);

    <R extends T> R add(Identifier identifier, R entry);

    T remove(Identifier identifier);
}
