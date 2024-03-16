/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.render.gl;

import io.github.xenfork.freeworld.client.Freeworld;
import overrun.marshal.DirectAccess;
import overrun.marshal.gen.Skip;
import overrungl.opengl.*;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface GLStateMgr extends
    GL10C, GL11C, GL15C,
    GL20C,
    GL30C,
    GL41C,
    DirectAccess {
    @Skip
    default GLFlags flags() {
        return Freeworld.getInstance().glFlags();
    }
}
