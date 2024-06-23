/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation;
 * only version 2.1 of the License.
 */

package freeworld.client.render.texture;

import freeworld.client.render.gl.GLStateMgr;
import freeworld.client.render.gl.GLResource;
import freeworld.core.Identifier;
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
    private final Map<Identifier, Texture> textureMap = new HashMap<>();

    public void addTexture(Identifier identifier, Texture texture) {
        textureMap.put(identifier, texture);
    }

    @SuppressWarnings("unchecked")
    public <T extends Texture> T getTexture(Identifier identifier) {
        return (T) textureMap.get(identifier);
    }

    @Override
    public void close(GLStateMgr gl) {
        logger.info("Closing texture manager");
        for (Texture texture : textureMap.values()) {
            texture.close(gl);
        }
    }
}
