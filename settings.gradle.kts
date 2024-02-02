/*
 * freeworld
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 * USA
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

mapOf("freeworld" to "core", "freeworld-client" to "client").forEach { (name, path) ->
    include(name)
    project(":$name").projectDir = file("modules/io.github.xenfork.freeworld.$path")
}
