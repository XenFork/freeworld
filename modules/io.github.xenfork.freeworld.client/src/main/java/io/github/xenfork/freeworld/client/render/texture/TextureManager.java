/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.texture;

import io.github.xenfork.freeworld.client.render.gl.GLResource;
import io.github.xenfork.freeworld.client.render.gl.GLStateMgr;
import io.github.xenfork.freeworld.core.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureManager implements GLResource {
    public static final Identifier BLOCK_ATLAS = Identifier.ofBuiltin("texture/atlas/block-atlas");
    private final Map<Identifier, Texture> textureMap = new HashMap<>();

    public void addTexture(Identifier identifier, Texture texture) {
        textureMap.put(identifier, texture);
    }

    public Texture getTexture(Identifier identifier) {
        return textureMap.get(identifier);
    }

    @Override
    public void close(GLStateMgr gl) {
        for (Texture texture : textureMap.values()) {
            texture.close(gl);
        }
    }
}
