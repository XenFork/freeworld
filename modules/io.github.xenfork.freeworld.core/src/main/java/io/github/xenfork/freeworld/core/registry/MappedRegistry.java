/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.core.registry;

import io.github.xenfork.freeworld.core.Identifier;
import io.github.xenfork.freeworld.util.Logging;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A registry backed with the map.
 *
 * @param <T> the type of the entry.
 * @author squid233
 * @since 0.1.0
 */
public class MappedRegistry<T> implements MutableRegistry<T> {
    private static final Logger logger = Logging.caller();
    private static final int DEFAULT_CAPACITY = 256;
    private final Identifier registryName;
    private final Map<Identifier, T> idToEntryMap = HashMap.newHashMap(DEFAULT_CAPACITY);
    private final Map<T, Identifier> entryToIdMap = HashMap.newHashMap(DEFAULT_CAPACITY);
    private final Map<Integer, T> rawIdToEntryMap = HashMap.newHashMap(DEFAULT_CAPACITY);
    private final Map<Identifier, Integer> idToRawIdMap=HashMap.newHashMap(DEFAULT_CAPACITY);
    private int nextId = -1;
    private boolean frozen = false;

    public MappedRegistry(Identifier registryName) {
        this.registryName = registryName;
    }

    @Override
    public Identifier registryName() {
        return registryName;
    }

    @Override
    public <R extends T> R set(Identifier identifier, int rawId, R entry) {
        if (frozen) {
            logger.error("Attempts to write in registry while frozen; ignoring.");
            return entry;
        }
        if (idToEntryMap.containsKey(identifier)) {
            logger.warn("Attempts to overwrite an existing key {}; this might be an programming error. Please remove it first", identifier);
        }
        idToEntryMap.put(identifier, entry);
        rawIdToEntryMap.put(rawId, entry);
        idToRawIdMap.put(identifier,rawId);
        if (rawId > nextId) {
            nextId = rawId;
        }
        return entry;
    }

    @Override
    public <R extends T> R add(Identifier identifier, R entry) {
        nextId++;
        return set(identifier, nextId, entry);
    }

    @Override
    public T remove(Identifier identifier) {
        if (frozen) {
            logger.error("Attempts to remove entry while frozen; ignoring.");
            return null;
        }
        if (!idToEntryMap.containsKey(identifier)) {
            logger.warn("Attempts to remove an absent entry with key {}; ignoring.", identifier);
            return null;
        }
        final T oldValue = idToEntryMap.remove(identifier);
        final Integer oldRawId = idToRawIdMap.remove(identifier);
        rawIdToEntryMap.remove(oldRawId);
        return oldValue;
    }

    @Override
    public void unfreeze() {
        logger.warn("Unfreezing registry {}; use at your own risk.", registryName());
        frozen = false;
    }

    @Override
    public void freeze() {
        logger.info("Freezing registry {}", registryName());
        frozen = true;
    }

    @Override
    public boolean frozen() {
        return frozen;
    }

    @Override
    public T get(Identifier identifier) {
        return idToEntryMap.get(identifier);
    }

    @Override
    public T getByRawId(int rawId) {
        return rawIdToEntryMap.get(rawId);
    }

    @Override
    public Identifier getId(T entry) {
        return entryToIdMap.get(entry);
    }

    @NotNull
    @Override
    public Iterator<Map.Entry<Identifier, T>> iterator() {
        return idToEntryMap.entrySet().iterator();
    }
}
