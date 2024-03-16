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
public abstract class GLStateMgr implements
    GL10C, GL11C, GL15C,
    GL20C,
    GL30C,
    GL41C,
    DirectAccess {
    private int currentProgram = 0;
    private int textureBinding2D = 0;
    private int vertexArrayBinding = 0;

    @Skip
    public void setCurrentProgram(int currentProgram) {
        if (this.currentProgram != currentProgram) {
            this.currentProgram = currentProgram;
            useProgram(currentProgram);
        }
    }

    @Skip
    public int currentProgram() {
        return currentProgram;
    }

    @Skip
    public void setTextureBinding2D(int textureBinding2D) {
        if (this.textureBinding2D != textureBinding2D) {
            this.textureBinding2D = textureBinding2D;
            bindTexture(TEXTURE_2D, textureBinding2D);
        }
    }

    @Skip
    public int textureBinding2D() {
        return textureBinding2D;
    }

    @Skip
    public void setVertexArrayBinding(int vertexArrayBinding) {
        if (this.vertexArrayBinding != vertexArrayBinding) {
            this.vertexArrayBinding = vertexArrayBinding;
            bindVertexArray(vertexArrayBinding);
        }
    }

    @Skip
    public int vertexArrayBinding() {
        return vertexArrayBinding;
    }

    @Skip
    public GLFlags flags() {
        return Freeworld.getInstance().glFlags();
    }
}
