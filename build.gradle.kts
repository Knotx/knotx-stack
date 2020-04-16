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
import org.nosphere.apache.rat.RatTask

plugins {
    id("io.knotx.java-library")
    id("io.knotx.unit-test")
    id("io.knotx.maven-publish")
    id("io.knotx.release-base")
    id("org.nosphere.apache.rat")
    id("idea")
}

project.group = "io.knotx"

// we do not use mavenLocal - instead please setup composite build environment (https://github.com/Knotx/knotx-aggregator)
repositories {
    jcenter()
    mavenLocal()
    maven { url = uri("https://plugins.gradle.org/m2/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/staging/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

sourceSets {
    register("functionalTest") {
        compileClasspath += sourceSets.test.get().output.classesDirs
        runtimeClasspath += sourceSets.test.get().output.classesDirs
    }
    test {
        resources {
            srcDir("src/main/packaging/conf")
        }
    }
}

apply(from = "gradle/distribution.gradle.kts")

val functionalTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.named("implementation").get()) }
val functionalTestRuntimeOnly: Configuration by configurations.getting

dependencies {
    implementation(platform("io.knotx:knotx-dependencies:${project.version}"))
    testImplementation(platform("io.knotx:knotx-dependencies:${project.version}"))

    implementation("io.knotx:knotx-launcher:${project.version}")
    implementation("io.knotx:knotx-server-http-core:${project.version}")
    implementation("io.knotx:knotx-repository-connector-fs:${project.version}")
    implementation("io.knotx:knotx-repository-connector-http:${project.version}")
    implementation("io.knotx:knotx-fragments-supplier-html-splitter:${project.version}")
    implementation("io.knotx:knotx-fragments-supplier-single-fragment:${project.version}")
    implementation("io.knotx:knotx-fragments-assembler:${project.version}")
    implementation("io.knotx:knotx-fragments-action-library:${project.version}")
    // tasks
    implementation("io.knotx:knotx-fragments-task-handler:${project.version}")
    implementation("io.knotx:knotx-fragments-task-factory-default:${project.version}")
    implementation("io.knotx:knotx-fragments-task-handler-log-html:${project.version}")
    implementation("io.knotx:knotx-fragments-task-handler-log-json:${project.version}")
    // te
    implementation("io.knotx:knotx-template-engine-core:${project.version}")
    implementation("io.knotx:knotx-template-engine-handlebars:${project.version}")
    implementation("io.knotx:knotx-template-engine-pebble:${project.version}")
    implementation("io.netty:netty-tcnative-boringssl-static")

    testImplementation("io.knotx:knotx-junit5:${project.version}")
    testImplementation(group = "io.vertx", name = "vertx-junit5")
    testImplementation(group = "io.vertx", name = "vertx-unit")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("org.junit.jupiter:junit-jupiter-engine")
    testImplementation(group = "com.github.tomakehurst", name = "wiremock")
    testImplementation(group = "io.rest-assured", name = "rest-assured", version = "3.3.0")

    functionalTestImplementation(platform("io.knotx:knotx-dependencies:${project.version}"))
    functionalTestImplementation("io.knotx:knotx-junit5:${project.version}")
    functionalTestImplementation(group = "io.vertx", name = "vertx-junit5")
    functionalTestImplementation(group = "io.vertx", name = "vertx-unit")
    functionalTestImplementation("org.junit.jupiter:junit-jupiter-api")
    functionalTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    functionalTestImplementation(group = "com.github.tomakehurst", name = "wiremock")
}

tasks {
    register<Test>("functionalTest") {
        description = "Runs functional tests."
        group = "verification"

        classpath = sourceSets["functionalTest"].runtimeClasspath
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs

        shouldRunAfter("test")
    }

    named("check") { dependsOn("functionalTest") }

    named("build") {
        if (file(".composite-enabled").exists()) {
            dependsOn(gradle.includedBuild("knotx-dependencies").task(":publishToMavenLocal"))
        }
        mustRunAfter("setVersion")
    }

    named("updateChangelog") {
        dependsOn("signMavenJavaPublication", "signAssembleDistribution", "setVersion")
    }

    register("prepare") {
        group = "release"
        dependsOn("updateChangelog", "publishToMavenLocal")
    }

    register("publishArtifacts") {
        group = "release"
        dependsOn("publish")
        logger.lifecycle("Publishing java artifacts")
    }
}

// -----------------------------------------------------------------------------
// License headers validation
// -----------------------------------------------------------------------------
tasks {
    named<RatTask>("rat") {
        excludes.addAll(listOf("*.md", "**/*.md", "**/bin/*", "azure-pipelines.yml", "**/build/*", "**/out/*", "**/*.json", "**/*.conf", "**/*.xml", "**/*.html", "**/*.properties", ".idea", ".composite-enabled", "/logs/**"))
    }
    getByName("build").dependsOn("rat")
}

// -----------------------------------------------------------------------------
// Publication
// -----------------------------------------------------------------------------
tasks.register("publish-all") {
    dependsOn(gradle.includedBuilds.stream().map { ib -> ib.task(":publish") }.toArray())
    dependsOn(tasks.named("publish"))
}

tasks.register("publish-local-all") {
    dependsOn(gradle.includedBuilds.stream().map { ib -> ib.task(":publishToMavenLocal") }.toArray())
    dependsOn(tasks.named("publishToMavenLocal"))
}

publishing {
    publications {
        create<MavenPublication>("knotxDistribution") {
            artifactId = "knotx-stack"
            artifact(tasks.named("assembleDistribution").get())
            pom {
                name.set("Knot.x Stack")
                description.set("Distribution of Knot.x containing all dependencies, configurations and running scripts.")
                url.set("https://knotx.io")
                licenses {
                    license {
                        name.set("The Apache Software License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("marcinczeczko")
                        name.set("Marcin Czeczko")
                        email.set("https://github.com/marcinczeczko")
                    }
                    developer {
                        id.set("skejven")
                        name.set("Maciej Laskowski")
                        email.set("https://github.com/Skejven")
                    }
                    developer {
                        id.set("tomaszmichalak")
                        name.set("Tomasz Michalak")
                        email.set("https://github.com/tomaszmichalak")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/Knotx/knotx-stack.git")
                    developerConnection.set("scm:git:ssh://github.com:Knotx/knotx-stack.git")
                    url.set("https://knotx.io")
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
                url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
                credentials {
                    username = if (project.hasProperty("ossrhUsername")) project.property("ossrhUsername")?.toString() else "UNKNOWN"
                    password = if (project.hasProperty("ossrhPassword")) project.property("ossrhPassword")?.toString() else "UNKNOWN"
                    println("Connecting with user: ${username}")
                }
            }
        }
    }
}

signing {
    sign(tasks["assembleDistribution"])
}

extra["isReleaseVersion"] = true // !version.toString().endsWith("SNAPSHOT")
tasks.withType<Sign>().configureEach {
    onlyIf { project.extra["isReleaseVersion"] as Boolean }
}
