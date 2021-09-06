/*
 * Copyright (C) 2019 Knot.x Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

rootProject.name = "knotx-stack"

pluginManagement {
    val version: String by settings
    plugins {
        id("io.knotx.java-library") version version
        id("io.knotx.unit-test") version version
        id("io.knotx.jacoco") version version
        id("io.knotx.maven-publish") version version
        id("io.knotx.release-base") version version
        id("org.nosphere.apache.rat") version "0.7.0"
        id("net.ossindex.audit") version "0.4.11"
    }
    repositories {
        mavenLocal()
        jcenter()
        gradlePluginPortal()
    }
}

if (file(".composite-enabled").exists()) {
    includeBuild("../knotx-gradle-plugins")
    includeBuild("../knotx-dependencies")
    includeBuild("../knotx-commons")
    includeBuild("../knotx-launcher")
    includeBuild("../knotx-junit5")
    includeBuild("../knotx-server-http")
    includeBuild("../knotx-repository-connector")
    includeBuild("../knotx-fragments")
    includeBuild("../knotx-template-engine")
}
