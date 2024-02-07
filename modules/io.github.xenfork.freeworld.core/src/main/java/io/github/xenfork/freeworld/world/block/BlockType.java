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

import io.github.xenfork.freeworld.world.block.function.BlockStateDefiner;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockType {
    private final BlockStateDefiner blockStateDefiner;
    private BlockState defaultBlockState;

    private BlockType(BlockStateDefiner blockStateDefiner) {
        this.blockStateDefiner = blockStateDefiner;
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @author squid233
     * @since 0.1.0
     */
    public static final class Builder {
        private BlockStateDefiner blockStateDefiner = BlockStateDefiner.identity();

        public Builder stateDefinitions(BlockStateDefiner definer) {
            this.blockStateDefiner = definer;
            return this;
        }

        public BlockType build() {
            return new BlockType(blockStateDefiner);
        }
    }

    public BlockState defaultBlockState() {
        if (defaultBlockState == null) {
            defaultBlockState = blockStateDefiner.apply(BlockState.empty(this));
        }
        return defaultBlockState;
    }
}
