/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.model.block;

import freeworld.client.render.texture.Texture2D;
import freeworld.util.Identifier;
import freeworld.registry.DefaultedRegistry;
import freeworld.registry.Registries;
import freeworld.registry.Registry;
import freeworld.world.block.BlockType;
import freeworld.world.block.BlockTypes;

import java.util.List;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockModelManager {
    private final DefaultedRegistry<BlockModel> registry = new DefaultedRegistry<>(Identifier.ofBuiltin("block_model"), BlockModelManager::missing);

    public static BlockModel empty() {
        final class Holder implements BlockModel {
            private static final Holder INSTANCE = new Holder();

            @Override
            public Map<Identifier, Identifier> textureDefinitions() {
                return Map.of();
            }

            @Override
            public List<BlockModelPart> parts() {
                return List.of();
            }
        }
        return Holder.INSTANCE;
    }

    public static BlockModel missing() {
        final class Holder {
            private static final CubeAllBlockModel MODEL = new CubeAllBlockModel(Identifier.ofBuiltin("builtin/missing"));
        }
        return Holder.MODEL;
    }

    public void register(Identifier identifier, BlockModel blockModel) {
        Registry.register(registry, identifier, blockModel);
    }

    public void register(BlockType blockType, BlockModel blockModel) {
        register(Registries.BLOCK_TYPE.getId(blockType), blockModel);
    }

    public void bootstrap() {
        register(Texture2D.MISSING, missing());
        register(BlockTypes.AIR, empty());
        register(BlockTypes.GRASS_BLOCK, new CubeAllBlockModel(Identifier.ofBuiltin("block/grass_block")));
        register(BlockTypes.DIRT, new CubeAllBlockModel(Identifier.ofBuiltin("block/dirt")));
        register(BlockTypes.STONE, new CubeAllBlockModel(Identifier.ofBuiltin("block/stone")));
        register(BlockTypes.STONE_SLAB, new SlabBlockModel(Identifier.ofBuiltin("block/stone"), Identifier.ofBuiltin("block/stone"), Identifier.ofBuiltin("block/stone")));
        register(BlockTypes.OAK_LOG, new LogBlockModel(Identifier.ofBuiltin("block/oak_log_top"), Identifier.ofBuiltin("block/oak_log")));
        register(BlockTypes.COAL_ORE, new CubeAllBlockModel(Identifier.ofBuiltin("block/coal_ore")));
    }

    public BlockModel get(Identifier identifier) {
        return registry.getById(identifier);
    }

    public DefaultedRegistry<BlockModel> registry() {
        return registry;
    }
}
