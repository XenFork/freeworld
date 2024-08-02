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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.UnaryOperator;

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

    public Identifier {
        checkNamespace(namespace, path);
        checkPath(namespace, path);
    }

    public Identifier(String identifier) {
        String[] split = identifier.split(":", 2);
        this(split.length == 2 ? split[0] : DEFAULT_NAMESPACE, switch (split.length) {
            case 0 -> "";
            case 1 -> split[0];
            default -> split[1];
        });
    }

    @Nullable
    public static Identifier of(@NotNull String identifier) {
        return isValidIdentifier(identifier) ? new Identifier(identifier) : null;
    }

    @NotNull
    public static Identifier ofBuiltin(@NotNull String path) throws InvalidIdentifierException {
        return new Identifier(DEFAULT_NAMESPACE, path);
    }

    public static boolean isValidNamespace(@Nullable String namespace) {
        return namespace != null && namespace.codePoints().allMatch(Identifier::isValidNamespaceCodepoint);
    }

    public static boolean isValidPath(@Nullable String path) {
        return path != null && path.codePoints().allMatch(Identifier::isValidPathCodepoint);
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

    private static boolean isValidNamespaceCodepoint(int codePoint) {
        return codePoint == '_' ||
            codePoint == '-' ||
            codePoint == '.' ||
            codePoint >= '0' && codePoint <= '9' ||
            codePoint >= 'a' && codePoint <= 'z';
    }

    private static boolean isValidPathCodepoint(int codePoint) {
        return isValidNamespaceCodepoint(codePoint) || codePoint == '/';
    }

    private static String checkNamespace(@NotNull String namespace, String path) {
        Objects.requireNonNull(namespace);
        if (isValidNamespace(namespace)) {
            return namespace;
        }
        throw new InvalidIdentifierException("Invalid namespace '" + namespace + "' in identifier " + namespace + ":" + path);
    }

    private static String checkPath(String namespace, @NotNull String path) {
        Objects.requireNonNull(path);
        if (isValidPath(path)) {
            return path;
        }
        throw new InvalidIdentifierException("Invalid path '" + path + "' in identifier " + namespace + ":" + path);
    }

    public String toResourcePath() {
        return namespace + "/" + path;
    }

    public Identifier withPath(String path) {
        return new Identifier(namespace, path);
    }

    public Identifier withPath(UnaryOperator<String> operator) {
        return withPath(operator.apply(path));
    }

    public Identifier withPathPrefix(String prefix) {
        return withPath(prefix + path);
    }

    public Identifier withPathSuffix(String suffix) {
        return withPath(path + suffix);
    }

    @Override
    public String toString() {
        return namespace + ":" + path;
    }
}
