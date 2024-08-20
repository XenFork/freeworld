/*
 * freeworld - 3D sandbox game
 * Copyright (C) 2024  XenFork Union
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */

val coreVersion: String by rootProject

val annotationsVersion: String by rootProject
val gsonVersion: String by rootProject
val logbackVersion: String by rootProject
val reactorVersion: String by rootProject

dependencies {
    api(project(":freeworld-math"))
    compileOnlyApi("org.jetbrains:annotations:$annotationsVersion")
    api("ch.qos.logback:logback-classic:$logbackVersion")
    api("com.google.code.gson:gson:$gsonVersion")
    api(platform("io.projectreactor:reactor-bom:$reactorVersion"))
    api("io.projectreactor:reactor-core")
    api("io.projectreactor.addons:reactor-pool")
}

tasks.processResources {
    val map = mapOf(
        "core_version" to coreVersion
    )
    inputs.properties(map)
    filesMatching("core_version.json") {
        expand(map)
    }
}
