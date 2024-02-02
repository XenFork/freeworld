/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package io.github.xenfork.freeworld.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logger utilities
 *
 * @author squid233
 * @since 0.1.0
 */
public final class Logging {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    /**
     * {@return a logger for the caller}
     */
    public static Logger caller() {
        return LoggerFactory.getLogger(STACK_WALKER.getCallerClass());
    }
}
