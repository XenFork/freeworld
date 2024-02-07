/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.world;

import io.github.xenfork.freeworld.world.chunk.Chunk;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * @author baka4n
 * @author squid233
 * @since 0.1.0
 */
public final class World {
    private final String name;
    private final String directoryName;
    private final Path worldDir;
    private final Map<Position, Chunk> loadedChunks = new HashMap<>();

    public World(String name, String directoryName) {
        this.name = name;
        this.directoryName = directoryName;
        this.worldDir = Path.of("saves", directoryName);
    }

    public static String chunkFilename(int x, int y, int z) {
        return STR."\{x}-\{y}-\{z}.fwc";
    }

    public Chunk loadOrCreateChunk(int x, int y, int z) {
        return loadedChunks.computeIfAbsent(new Position(x, y, z), position -> {
            final int cx = position.x();
            final int cy = position.y();
            final int cz = position.z();
            final Path chunksPath = getResourcePath(Resource.CHUNKS);
            final Path chunkFile = chunksPath.resolve(chunkFilename(cx, cy, cz));

            if (!Files.exists(chunkFile)) {
                final Chunk chunk = createChunk(cx, cy, cz);
                chunk.generateTerrain();
                return chunk;
            }

            return loadChunk(chunkFile, cx, cy, cz);
        });
    }

    public Chunk loadChunk(Path chunkFile, int x, int y, int z) {
        final Path parent = chunkFile.getParent();
        try {
            Files.createDirectories(parent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Chunk chunk = createChunk(x, y, z);
        chunk.loadFromFile(chunkFile);
        return chunk;
    }

    public Chunk createChunk(int x, int y, int z) {
        return new Chunk(this, x, y, z);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return STR."World[name=\{name}]";
    }

    public Path getResourcePath(Resource resource) {
        return worldDir.resolve(resource.path());
    }

    /**
     * @author baka4n
     * @since 0.1.0
     */
    public enum Resource {
        ROOT("."),
        CHUNKS("chunks"),
        ;

        private final String path;

        Resource(String path) {
            this.path = path;
        }

        public String path() {
            return path;
        }
    }
}
