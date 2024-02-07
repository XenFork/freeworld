/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client.util;

/**
 * @author baka4n
 * @since 0.1.0
 */
public final class Options {
    public int viewDistance;
    public double gamma;
    public boolean autoJump;
    public String language;
    public boolean fullscreen;

    public Options() {
    }

    public Options(int viewDistance, double gamma) {
        this.viewDistance = viewDistance;
        this.gamma = gamma;
        autoJump = false;
        language = "en_us";
        fullscreen = false;
    }
}
