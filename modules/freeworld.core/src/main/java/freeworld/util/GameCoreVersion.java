/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.util;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author squid233
 * @since 0.1.0
 */
public record GameCoreVersion(String version) {
    private static final Gson GSON = new Gson();
    private static final GameCoreVersion INSTANCE = load();

    public static GameCoreVersion get() {
        return INSTANCE;
    }

    public static GameCoreVersion load() {
        String lines;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(GameCoreVersion.class.getResourceAsStream("/core_version.json"))))) {
            lines = reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load core version", e);
        }
        return GSON.fromJson(lines, GameCoreVersion.class);
    }
}
