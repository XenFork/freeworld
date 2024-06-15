/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.entity.component;

import freeworld.core.Identifier;

import java.util.function.Supplier;

/**
 * @author squid233
 * @since 0.1.0
 */
public record EntityComponentKey<T>(Identifier identifier, Supplier<T> defaultValue) {
}