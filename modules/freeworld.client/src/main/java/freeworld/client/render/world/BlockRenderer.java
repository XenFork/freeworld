/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.world;

import freeworld.client.render.builder.VertexBuilder;
import freeworld.client.render.model.block.BlockModel;
import freeworld.client.render.model.block.BlockModelFace;
import freeworld.client.render.model.block.BlockModelPart;
import freeworld.client.render.texture.TextureAtlas;
import freeworld.client.render.texture.TextureManager;
import freeworld.client.render.texture.TextureRegion;
import freeworld.core.ModelResourcePath;
import freeworld.math.Matrix4f;
import freeworld.math.Vector2f;
import freeworld.math.Vector3f;
import freeworld.math.Vector4i;
import freeworld.util.Direction;
import freeworld.util.math.AABBox;

import java.util.function.Predicate;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class BlockRenderer {
    private final TextureManager textureManager;

    public BlockRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    private void emitVertices(VertexBuilder builder, Matrix4f matrix, Vector3f from, Vector3f to, Vector2f uvFrom, Vector2f uvTo, Direction direction) {
        // TODO: 2024/7/6 squid233: color
        float color = switch (direction) {
            case WEST -> 0.7f;
            case EAST -> 1.0f;
            case DOWN -> 0.6f;
            case UP -> 0.9f;
            case NORTH, SOUTH -> 0.8f;
        };
        Vector4i vertexIndices = direction.vertexIndices();
        builder.position(matrix, AABBox.getPoint(from, to, vertexIndices.x())).color(color, color, color).texCoord(uvFrom.x(), uvFrom.y()).emit();
        builder.position(matrix, AABBox.getPoint(from, to, vertexIndices.y())).color(color, color, color).texCoord(uvFrom.x(), uvTo.y()).emit();
        builder.position(matrix, AABBox.getPoint(from, to, vertexIndices.z())).color(color, color, color).texCoord(uvTo.x(), uvTo.y()).emit();
        builder.position(matrix, AABBox.getPoint(from, to, vertexIndices.w())).color(color, color, color).texCoord(uvTo.x(), uvFrom.y()).emit();
    }

    public void renderBlockModel(VertexBuilder builder, BlockModel model, Matrix4f matrix, int x, int y, int z, Predicate<Direction> shouldCullFace) {
        final TextureAtlas texture = textureManager.getTexture(TextureManager.BLOCK_ATLAS);
        final int width = texture.width();
        final int height = texture.height();

        for (BlockModelPart part : model.parts()) {
            final Vector3f from = part.from().add(x, y, z);
            final Vector3f to = part.to().add(x, y, z);
            for (var e : part.faces().entrySet()) {
                final BlockModelFace face = e.getValue();
                if (face != null && (face.cullFace() == null || !shouldCullFace.test(face.cullFace()))) {
                    final ModelResourcePath path = face.texture();
                    final TextureRegion region = texture.getRegion(switch (path.type()) {
                        case DIRECT -> path.identifier();
                        case VARIABLE -> model.textureDefinitions().get(path.identifier());
                    });
                    if (region == null) {
                        continue;
                    }
                    final Vector2f uvFrom = face.uvFrom().mul(region.width(), region.height()).add(region.x(), region.y()).div(width, height);
                    final Vector2f uvTo = face.uvTo().mul(region.width(), region.height()).add(region.x(), region.y()).div(width, height);

                    builder.indices(0, 1, 2, 2, 3, 0);
                    emitVertices(builder, matrix, from, to, uvFrom, uvTo, e.getKey());
                }
            }
        }
    }

    public void renderBlockModel(VertexBuilder builder, BlockModel model, int x, int y, int z, Predicate<Direction> shouldCullFace) {
        renderBlockModel(builder, model, Matrix4f.IDENTITY, x, y, z, shouldCullFace);
    }
}
