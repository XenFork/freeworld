/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.world.entity;

import freeworld.math.Vector3d;
import freeworld.world.World;

import java.util.Objects;
import java.util.UUID;

/**
 * @author squid233
 * @since 0.1.0
 */
public class EntityType<T extends Entity> {
    private final Factory<T> factory;
    private final Vector3d dimension;
    private final Vector3d eyePosition;

    public EntityType(Settings settings, Factory<T> factory) {
        this.factory = factory;
        this.dimension = Objects.requireNonNull(settings.dimension);
        this.eyePosition = Objects.requireNonNull(settings.eyePosition);
    }

    @FunctionalInterface
    public interface Factory<T extends Entity> {
        T create(World world, UUID uuid);
    }

    public static final class Settings {
        private Vector3d dimension;
        private Vector3d eyePosition = new Vector3d(0.0, 0.5, 0.0);

        public Settings dimension(Vector3d dimension) {
            this.dimension = dimension;
            return this;
        }

        public Settings eyePosition(Vector3d eyePosition) {
            this.eyePosition = eyePosition;
            return this;
        }
    }

    public Factory<T> factory() {
        return factory;
    }

    public Vector3d dimension() {
        return dimension;
    }

    public Vector3d eyePosition() {
        return eyePosition;
    }
}
