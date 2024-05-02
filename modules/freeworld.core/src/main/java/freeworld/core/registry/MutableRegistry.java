/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.core.registry;

import freeworld.core.Identifier;

/**
 * A mutable registry, which allows modifying while it is not {@linkplain #frozen() frozen}.
 *
 * @param <T> the type of the entry.
 * @author squid233
 * @since 0.1.0
 */
public interface MutableRegistry<T> extends Registry<T> {
    <R extends T> R set(Identifier identifier, int rawId, R entry);

    <R extends T> R add(Identifier identifier, R entry);

    T remove(Identifier identifier);

    /**
     * Unfreezes this registry.
     */
    void unfreeze();

    /**
     * Freezes this registry.
     */
    void freeze();

    /**
     * {@return is this registry frozen?}
     */
    boolean frozen();
}
