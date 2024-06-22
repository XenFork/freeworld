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
module freeworld.core {
    exports freeworld.core;
    exports freeworld.core.math;
    exports freeworld.core.registry;
    exports freeworld.util;
    exports freeworld.util.file;
    exports freeworld.util.math;
    exports freeworld.world;
    exports freeworld.world.block;
    exports freeworld.world.chunk;
    exports freeworld.world.component;
    exports freeworld.world.entity;
    exports freeworld.world.entity.system;

    requires transitive freeworld.math;
    requires ch.qos.logback.core;
    requires ch.qos.logback.classic;
    requires transitive com.google.gson;
    requires transitive org.slf4j;
    requires static org.jetbrains.annotations;

    provides ch.qos.logback.classic.spi.Configurator
        with freeworld.util.LogbackConfigurator;
}
