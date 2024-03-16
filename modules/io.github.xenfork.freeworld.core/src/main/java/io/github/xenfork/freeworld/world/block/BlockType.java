/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world.block;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockType {
    private final boolean air;

    private BlockType(boolean air) {
        this.air = air;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private boolean air = false;

        public Builder air() {
            this.air = true;
            return this;
        }

        public BlockType build() {
            return new BlockType(air);
        }
    }

    public boolean air() {
        return air;
    }
}
