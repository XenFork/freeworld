/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.core;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
public record Identifier(@NotNull String namespace, @NotNull String path) {
    public static final String DEFAULT_NAMESPACE = "freeworld";
    public static final String ROOT_ASSETS = "assets";
    public static final String RES_SHADER = "shader";
    public static final String RES_TEXTURE = "texture";
    public static final String EXT_JSON = ".json";
    public static final String EXT_PNG = ".png";
    private static final Pattern NAMESPACE_RULE = Pattern.compile("^[\\w-]+$");
    private static final Pattern PATH_RULE = Pattern.compile("^[\\w/.-]*$");
    private static final Identifier EMPTY = new Identifier(DEFAULT_NAMESPACE, "");

    public Identifier {
        checkNamespace(namespace);
        checkPath(path);
    }

    @NotNull
    public static Identifier of(@NotNull String identifier) throws InvalidIdentifierException {
        Objects.requireNonNull(identifier);
        final String[] split = identifier.split(":", 2);
        return switch (split.length) {
            case 0 -> EMPTY;
            case 1 -> ofBuiltin(split[0]);
            default -> new Identifier(split[0], split[1]);
        };
    }

    @NotNull
    public static Identifier ofBuiltin(@NotNull String path) throws InvalidIdentifierException {
        return new Identifier(DEFAULT_NAMESPACE, path);
    }

    @Nullable
    public static Identifier ofSafe(@Nullable String identifier) {
        return isValidIdentifier(identifier) ? of(identifier) : null;
    }

    public static boolean isValidNamespace(@Nullable String namespace) {
        return namespace != null && NAMESPACE_RULE.matcher(namespace).matches();
    }

    public static boolean isValidPath(@Nullable String path) {
        return path != null && PATH_RULE.matcher(path).matches();
    }

    public static boolean isValidIdentifier(@Nullable String identifier) {
        if (identifier == null) {
            return false;
        }
        final String[] split = identifier.split(":");
        return switch (split.length) {
            case 0 -> true;
            case 1 -> isValidPath(split[0]);
            default -> isValidNamespace(split[0]) && isValidPath(split[1]);
        };
    }

    private static String checkNamespace(@NotNull String namespace) {
        Objects.requireNonNull(namespace);
        if (isValidNamespace(namespace)) {
            return namespace;
        }
        throw new InvalidIdentifierException(STR."Invalid namespace: \{namespace}");
    }

    private static String checkPath(@NotNull String path) {
        Objects.requireNonNull(path);
        if (isValidPath(path)) {
            return path;
        }
        throw new InvalidIdentifierException(STR."Invalid path: \{path}");
    }

    public String toResourcePath(@Nullable String root, @Nullable String type, @Nullable String suffix) {
        return STR."\{root != null ? STR."\{root}/" : ""}\{namespace()}/\{type != null ? STR."\{type}/" : ""}\{path()}\{suffix != null ? suffix : ""}";
    }

    public Identifier toResourceId(@Nullable String type, @Nullable String suffix) {
        return new Identifier(namespace(), STR."\{type != null ? STR."\{type}/" : ""}\{path()}\{suffix != null ? suffix : ""}");
    }

    @Override
    public String toString() {
        return STR."\{namespace()}:\{path()}";
    }
}
