/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block.property;

/**
 * @author squid233
 * @since 0.1.0
 */
public class EnumProperty<T extends Enum<T>> extends BlockStateProperty<T> {
    private final T[] constants;

    private EnumProperty(String name, T[] constants) {
        super(name);
        this.constants = constants;
    }

    public static <T extends Enum<T>> EnumProperty<T> of(String name, Class<T> tClass) {
        return new EnumProperty<>(name, tClass.getEnumConstants());
    }
}
