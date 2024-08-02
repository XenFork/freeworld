/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.client.render.texture;

import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gl.GLResource;
import freeworld.util.Identifier;
import freeworld.util.Logging;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author squid233
 * @since 0.1.0
 */
public final class TextureManager implements GLResource {
    private static final Logger logger = Logging.caller();
    public static final Identifier BLOCK_ATLAS = Identifier.ofBuiltin("texture/atlas/block-atlas");
    public static final Identifier GUI_ATLAS = Identifier.ofBuiltin("texture/atlas/gui-atlas");
    private final Map<Identifier, Texture2D> textureMap = new HashMap<>();

    public void addTexture(Identifier identifier, Texture2D texture) {
        textureMap.put(identifier, texture);
    }

    @SuppressWarnings("unchecked")
    public <T extends Texture2D> T getTexture(Identifier identifier) {
        return (T) textureMap.get(identifier);
    }

    public Texture2D getOrLoad(GLStateMgr gl, Identifier identifier) {
        final Texture2D texture = getTexture(identifier);
        if (texture != null) {
            return texture;
        }
        final Texture2D load = Texture2D.load(gl, identifier);
        addTexture(identifier, load);
        return load;
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing texture manager");
        for (Texture2D texture : textureMap.values()) {
            texture.close(gl);
        }
    }
}
