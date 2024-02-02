/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.core;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * An identifier that locates to a resource.
 *
 * @param namespace the namespace of the resource
 * @param path      the path of the resource
 * @author squid233
 * @since 0.1.0
 */
public record Identifier(String namespace, String path) {
    private static final Pattern NAMESPACE_RULE = Pattern.compile("^\\w+$");
    private static final Pattern PATH_RULE = Pattern.compile("^[\\w/.]*$");
    public static final String DEFAULT_NAMESPACE = "freeworld";
    private static final Identifier EMPTY = new Identifier(DEFAULT_NAMESPACE, "");

    @NotNull
    public static Identifier of(@NotNull String namespace, @NotNull String path) {
        return new Identifier(checkNamespace(namespace), checkPath(path));
    }

    public static Identifier of(@NotNull String identifier) {
        Objects.requireNonNull(identifier);
        final String[] split = identifier.split(":", 2);
        return switch (split.length) {
            case 0 -> EMPTY;
            case 1 -> ofBuiltin(split[0]);
            default -> of(split[0], split[1]);
        };
    }

    public static Identifier ofBuiltin(@NotNull String path) {
        return new Identifier(DEFAULT_NAMESPACE, checkPath(path));
    }

    private static String checkNamespace(@NotNull String namespace) {
        Objects.requireNonNull(namespace);
        if (NAMESPACE_RULE.matcher(namespace).matches()) {
            return namespace;
        }
        throw new InvalidIdentifierException(STR."Invalid namespace: \{namespace}");
    }

    private static String checkPath(@NotNull String path) {
        Objects.requireNonNull(path);
        if (PATH_RULE.matcher(path).matches()) {
            return path;
        }
        throw new InvalidIdentifierException(STR."Invalid path: \{path}");
    }

    @Override
    public String toString() {
        return STR."\{namespace()}:\{path()}";
    }
}
