/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

package freeworld.world.entity.system;

import freeworld.world.World;
import freeworld.world.entity.Entity;
import freeworld.world.entity.component.EntityComponentKey;

import java.util.List;

/**
 * @author squid233
 * @since 0.1.0
 */
public interface EntitySystem {
    void process(World world, List<Entity> entities);

    static boolean hasAllComponents(Entity entity, EntityComponentKey<?>... componentIds) {
        if (entity == null) return false;

        for (var id : componentIds) {
            if (!entity.hasComponent(id)) {
                return false;
            }
        }
        return true;
    }
}
