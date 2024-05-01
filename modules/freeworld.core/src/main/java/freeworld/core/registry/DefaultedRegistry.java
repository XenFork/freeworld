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

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public class DefaultedRegistry<T> extends MappedRegistry<T> {
    private final Supplier<T> defaultValueSupplier;
    private T defaultValue;

    public DefaultedRegistry(Identifier registryName, Supplier<T> defaultValueSupplier) {
        super(registryName);
        this.defaultValueSupplier = defaultValueSupplier;
    }

    public T getDefaultValue() {
        if (defaultValue == null) {
            defaultValue = defaultValueSupplier.get();
        }
        return defaultValue;
    }

    @Override
    public T get(Identifier identifier) {
        final T t = super.get(identifier);
        return t != null ? t : getDefaultValue();
    }

    @Override
    public T getByRawId(int rawId) {
        final T t = super.getByRawId(rawId);
        return t != null ? t : getDefaultValue();
    }

    @Override
    public Identifier getId(T entry) {
        final Identifier id = super.getId(entry);
        return id != null ? id : super.getId(getDefaultValue());
    }

}
