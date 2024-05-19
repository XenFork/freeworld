/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.file;

import freeworld.util.Logging;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.io.*;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * Builtin files utilities
 *
 * @author squid233
 * @since 0.1.0
 */
public final class BuiltinFiles {
    private static final Logger logger = Logging.caller();

    private BuiltinFiles() {
    }

    @Nullable
    public static URL loadURL(ClassLoader classLoader, String name) {
        return classLoader.getResource(name);
    }

    @Nullable
    public static URL loadURL(String name) {
        return loadURL(BuiltinFiles.class.getClassLoader(), name);
    }

    @Nullable
    public static InputStream load(ClassLoader classLoader, String name) {
        return classLoader.getResourceAsStream(name);
    }

    @Nullable
    public static InputStream load(String name) {
        return load(BuiltinFiles.class.getClassLoader(), name);
    }

    public static MemorySegment loadBinary(SegmentAllocator allocator, InputStream stream, String name) {
        if (stream == null) {
            return MemorySegment.NULL;
        }
        try (stream) {
            return allocator.allocateFrom(ValueLayout.JAVA_BYTE, stream.readAllBytes());
        } catch (IOException e) {
            logger.error("Failed to load file {} from stream", name, e);
            return MemorySegment.NULL;
        }
    }

    @Nullable
    public static BufferedReader readTextAsReader(InputStream stream) {
        if (stream == null) {
            return null;
        }
        return new BufferedReader(new InputStreamReader(stream));
    }

    /**
     * Reads text from the given input stream.
     * <p>
     * This method automatically closes the given input stream.
     *
     * @param stream the input stream
     * @param name   the name of the file
     * @return the string
     */
    @Nullable
    public static String readText(InputStream stream, String name) {
        if (stream == null) {
            return null;
        }
        try (BufferedReader reader = readTextAsReader(stream)) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            logger.error("Failed to load file {} from stream", name, e);
            return null;
        }
    }
}
