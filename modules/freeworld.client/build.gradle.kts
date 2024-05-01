/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

plugins {
    application
}

val jdkEnablePreview: String by rootProject

val overrunglVersion: String by rootProject

val overrunglNatives = Pair(
    System.getProperty("os.name")!!,
    System.getProperty("os.arch")!!
).let { (name, arch) ->
    when {
        arrayOf("Linux", "FreeBSD", "SunOS", "Unit").any { name.startsWith(it) } ->
            if (arrayOf("arm", "aarch64").any { arch.startsWith(it) })
                "natives-linux${if (arch.contains("64") || arch.startsWith("armv8")) "-arm64" else "-arm32"}"
            else "natives-linux"

        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } ->
            "natives-macos${if (arch.startsWith("aarch64")) "-arm64" else ""}"

        arrayOf("Windows").any { name.startsWith(it) } ->
            if (arch.contains("64"))
                "natives-windows${if (arch.startsWith("aarch64")) "-arm64" else ""}"
            else throw Error("Unrecognized or unsupported architecture. Please set \"overrunglNatives\" manually")

        else -> throw Error("Unrecognized or unsupported platform. Please set \"overrunglNatives\" manually")
    }
}

dependencies {
    api(project(":freeworld"))
    implementation(platform("io.github.over-run:overrungl-bom:$overrunglVersion"))
    implementation("io.github.over-run:overrungl")
    implementation("io.github.over-run:overrungl-joml")
    implementation("io.github.over-run:overrungl-glfw")
    runtimeOnly("io.github.over-run:overrungl-glfw::$overrunglNatives")
    implementation("io.github.over-run:overrungl-opengl")
    implementation("io.github.over-run:overrungl-stb")
    runtimeOnly("io.github.over-run:overrungl-stb::$overrunglNatives")
    //TODO
    implementation("io.github.over-run:marshal:0.1.0-alpha.24-jdk22")
}

application {
    applicationName = "freeworld"
    mainModule = "freeworld.client"
    mainClass = "freeworld.client.main.Main"
    applicationDefaultJvmArgs = buildList {
        if (jdkEnablePreview.toBoolean())add("--enable-preview")
        add(
            "--enable-native-access=${
                listOf(
                    "freeworld.client",
                    "io.github.overrun.marshal",
                    "overrungl.core",
                    "overrungl.glfw",
                    "overrungl.opengl",
                    "overrungl.stb"
                ).joinToString(separator = ",")
            }"
        )
    }
}
