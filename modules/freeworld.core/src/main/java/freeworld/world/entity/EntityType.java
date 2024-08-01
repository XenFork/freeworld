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

/**
 * @author squid233
 * @since 0.1.0
 */
public class EntityType<T extends Entity> {
    private final Factory<T> factory;
    private final Vector3d dimension;

    private EntityType(Factory<T> factory, Vector3d dimension) {
        this.factory = factory;
        this.dimension = dimension;
    }

    @FunctionalInterface
    public interface Factory<T extends Entity> {
        T create(World world);
    }

    public static <T extends Entity> Builder<T> builder(Factory<T> factory) {
        return new Builder<>(factory);
    }

    public static final class Builder<T extends Entity> {
        private final Factory<T> factory;
        private Vector3d dimension = new Vector3d(0.6, 1.8, 0.6);

        private Builder(Factory<T> factory) {
            this.factory = factory;
        }

        public Builder<T> dimension(Vector3d dimension) {
            this.dimension = dimension;
            return this;
        }

        public EntityType<T> build() {
            return new EntityType<>(
                factory,
                Objects.requireNonNull(dimension)
            );
        }
    }

    public Factory<T> factory() {
        return factory;
    }

    public Vector3d dimension() {
        return dimension;
    }
}
