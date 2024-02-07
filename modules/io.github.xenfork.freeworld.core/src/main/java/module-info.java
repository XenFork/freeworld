/*
 * freeworld
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

/**
 * freeworld core
 *
 * @author squid233
 * @since 0.1.0
 */
module io.github.xenfork.freeworld.core {
    exports io.github.xenfork.freeworld.core;
    exports io.github.xenfork.freeworld.core.registry;
    exports io.github.xenfork.freeworld.file;
    exports io.github.xenfork.freeworld.util;
    exports io.github.xenfork.freeworld.world.block;
    exports io.github.xenfork.freeworld.world.block.function;
    exports io.github.xenfork.freeworld.world.block.property;

    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires transitive com.google.gson;
    requires transitive org.slf4j;
    requires static org.jetbrains.annotations;

    provides ch.qos.logback.classic.spi.Configurator
        with io.github.xenfork.freeworld.util.LogbackConfigurator;
}
