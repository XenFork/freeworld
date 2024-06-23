/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        // you can add mirror repositories by:
        // maven { url = uri("THE URL") }
    }
}

val projName: String by settings
rootProject.name = projName

mapOf(
    "freeworld" to "core",
    "freeworld-client" to "client",
    "freeworld-math" to "math"
).forEach { (name, path) ->
    include(name)
    project(":$name").projectDir = file("modules/freeworld.$path")
}
