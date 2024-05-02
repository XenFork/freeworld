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

/**
 * Indicates an invalid identifier.
 *
 * @author squid233
 * @since 0.1.0
 */
public final class InvalidIdentifierException extends RuntimeException {
    /**
     * Constructs {@code InvalidIdentifierException}.
     *
     * @param message the message
     */
    public InvalidIdentifierException(String message) {
        super(message);
    }
}
