/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.model.block;

import freeworld.client.render.texture.TextureAtlas;
import freeworld.core.Identifier;
import freeworld.core.registry.BuiltinRegistries;
import freeworld.core.registry.DefaultedRegistry;
import freeworld.core.registry.Registry;
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
        register(BuiltinRegistries.BLOCK_TYPE.getId(blockType), blockModel);
    }

    public void bootstrap() {
        register(TextureAtlas.MISSING, missing());
        register(BlockTypes.AIR, empty());
        register(BlockTypes.GRASS_BLOCK, new CubeAllBlockModel(Identifier.ofBuiltin("block/grass_block")));
        register(BlockTypes.DIRT, new CubeAllBlockModel(Identifier.ofBuiltin("block/dirt")));
        register(BlockTypes.STONE, new CubeAllBlockModel(Identifier.ofBuiltin("block/stone")));
    }

    public BlockModel get(Identifier identifier) {
        return registry.get(identifier);
    }

    public DefaultedRegistry<BlockModel> registry() {
        return registry;
    }
}
