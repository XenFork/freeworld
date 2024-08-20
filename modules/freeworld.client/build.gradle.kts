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

val clientVersion: String by rootProject
val jdkEnablePreview: String by rootProject
val overrunglVersion: String by rootProject
val jdkVersion: String by rootProject

val overrunglOs = System.getProperty("os.name")!!.let { name ->
    when {
        "FreeBSD" == name -> "freebsd"
        arrayOf("Linux", "SunOS", "Unit").any { name.startsWith(it) } -> "linux"
        arrayOf("Mac OS X", "Darwin").any { name.startsWith(it) } -> "macos"
        arrayOf("Windows").any { name.startsWith(it) } -> "windows"
        else -> throw Error("Unrecognized or unsupported platform $name. Please set \"overrunglOs\" manually")
    }
}
val overrunglArch = System.getProperty("os.arch")!!.let { arch ->
    when (overrunglOs) {
        "freebsd" -> "x64"
        "linux" -> if (arrayOf("arm", "aarch64").any { arch.startsWith(it) }) {
            if (arch.contains("64") || arch.startsWith("armv8")) "arm64" else "arm32"
        } else if (arch.startsWith("ppc")) "ppc64le"
        else if (arch.startsWith("riscv")) "riscv64"
        else "x64"

        "macos", "windows" -> if (arch.startsWith("aarch64")) "arm64" else "x64"
        else -> throw Error("Unrecognized or unsupported platform $overrunglOs. Please set \"overrunglArch\" manually")
    }
}

val nativeAccessList = listOf(
    "freeworld.client",
    "io.github.overrun.marshal",
    "overrungl.core",
    "overrungl.glfw",
    "overrungl.opengl",
    "overrungl.stb"
)

configurations.runtimeClasspath.get().attributes {
    attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, objects.named(overrunglOs))
    attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, objects.named(overrunglArch))
}

dependencies {
    api(project(":freeworld"))
    implementation(platform("io.github.over-run:overrungl-bom:$overrunglVersion"))
    implementation("io.github.over-run:overrungl")
    implementation("io.github.over-run:overrungl-glfw")
    implementation("io.github.over-run:overrungl-opengl")
    implementation("io.github.over-run:overrungl-stb")
}

application {
    applicationName = "freeworld-client"
    mainModule = "freeworld.client"
    mainClass = "freeworld.client.main.Main"
    applicationDefaultJvmArgs = buildList {
        if (jdkEnablePreview.toBoolean()) add("--enable-preview")
        add(
            "--enable-native-access=${nativeAccessList.joinToString(separator = ",")}"
        )
    }
}

tasks.processResources {
    val map = mapOf(
        "client_version" to clientVersion
    )
    inputs.properties(map)
    filesMatching("client_version.json") {
        expand(map)
    }
}

tasks.register<JavaExec>("runClient") {
    group = "freeworld run"

    classpath = sourceSets.main.get().runtimeClasspath
    mainModule = "freeworld.client"
    mainClass = "freeworld.client.main.Main"

    val runDir = rootDir.resolve("run/client")
    workingDir = runDir

    if (jdkEnablePreview.toBoolean()) {
        jvmArgs("--enable-preview")
    }
    jvmArgs("--enable-native-access=${nativeAccessList.joinToString(separator = ",")}")

    javaLauncher = javaToolchains.launcherFor {
        languageVersion.set(JavaLanguageVersion.of(jdkVersion))
    }

    doFirst {
        if (!runDir.exists()) {
            runDir.mkdirs()
        }
    }
}
