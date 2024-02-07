/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.xenfork.freeworld.client.util.Options;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Automatically loaded after using the fields inside
 *
 * @author baka4n
 * @since 0.1.0
 */
public class FreeworldConfig<T> {
    private static final Gson GSON = new GsonBuilder().disableJdkUnsafe().setLenient().setPrettyPrinting().create();
    public static final Path configDir = Path.of("config");

    private static final List<FreeworldConfig<?>> loadedConfig = new ArrayList<>();
    public static final FreeworldConfig<Options> options = new FreeworldConfig<>(".", "options.json", Options.class, new Options(4, 1.0));

    private final T instance;
    private final T defaultInstance;
    private final Class<T> clazz;
    private final Path filePath;

    public FreeworldConfig(String dir, String path, Class<T> clazz, T defaultInstance) {
        this.defaultInstance = defaultInstance;
        this.clazz = clazz;
        final Path fileDir = configDir.resolve(dir);
        try {
            Files.createDirectories(fileDir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        filePath = fileDir.resolve(path);
        instance = loadOrDefault();
        loadedConfig.add(this);

    }

    public T loadOrDefault() {
        T instance;
        try (final BufferedReader br = Files.newBufferedReader(filePath)) {
            instance = GSON.fromJson(br, clazz);
            loadedConfig.add(this);
        } catch (IOException _) {
            instance = defaultInstance;
        }
        return instance;
    }

    public void save() {
        try (BufferedWriter bw = Files.newBufferedWriter(filePath)) {
            GSON.toJson(instance, bw);
        } catch (IOException _) {
        }
    }

    public static void closeAll() {
        loadedConfig.forEach(FreeworldConfig::save);
    }

    public T get() {
        return instance;
    }
}
