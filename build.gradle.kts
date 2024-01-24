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
    id("net.ossindex.audit")
}

project.group = "io.knotx"

repositories {
    mavenLocal()
    mavenCentral()
    gradlePluginPortal()
}

sourceSets {
    register("functionalTest") {
        compileClasspath += sourceSets.test.get().output
        runtimeClasspath += sourceSets.test.get().output
    }
    test {
        resources {
            srcDir("src/main/packaging/conf")
        }
    }
}

apply(from = "gradle/distribution.gradle.kts")

val functionalTestImplementation: Configuration by configurations.getting { extendsFrom(configurations.named("implementation").get()) }
val functionalTestRuntimeOnly: Configuration by configurations.getting { }

dependencies {
    implementation(platform("io.knotx:knotx-dependencies:${project.version}"))

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

    testImplementation("io.knotx:knotx-junit5:${project.version}")
    testImplementation(group = "io.vertx", name = "vertx-web-client")
    testImplementation(group = "io.rest-assured", name = "rest-assured", version = "5.4.0")

    functionalTestImplementation(platform("io.knotx:knotx-dependencies:${project.version}"))
    functionalTestImplementation("io.knotx:knotx-junit5:${project.version}")
    functionalTestImplementation(group = "io.vertx", name = "vertx-junit5")
    functionalTestImplementation(group = "io.vertx", name = "vertx-unit")
    functionalTestImplementation(group = "io.vertx", name = "vertx-rx-java")
    functionalTestImplementation(group = "io.vertx", name = "vertx-rx-java2")
    functionalTestImplementation("org.junit.jupiter:junit-jupiter-api")
    functionalTestRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    functionalTestImplementation(group = "org.wiremock", name = "wiremock")
    functionalTestImplementation(group = "io.vertx", name = "vertx-web-client")
}

tasks {
    register<Test>("functionalTest") {
        description = "Runs functional tests."
        group = "verification"

        classpath = sourceSets["functionalTest"].runtimeClasspath
        testClassesDirs = sourceSets["functionalTest"].output.classesDirs

        shouldRunAfter("test")
    }

    named("check") {
        dependsOn("functionalTest")
        dependsOn(gradle.includedBuilds.stream().map { ib -> ib.task(":check") }.toArray())
    }

    named("build") {
        if (file(".composite-enabled").exists()) {
            dependsOn(gradle.includedBuild("knotx-dependencies").task(":publishToMavenLocal"))
        }
        mustRunAfter("setVersion")
    }

    named("updateChangelog") {
        dependsOn("signMavenJavaPublication", "signKnotxDistributionPublication", "setVersion")
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
        excludes.addAll(listOf(
            "**/*.md", // docs
            "gradle/wrapper/**", "gradle*", "**/build/**", "**/bin/**", // Gradle
            "*.iml", "*.ipr", "*.iws", "*.idea/**", // IDEs
            "**/generated/*", "**/*.adoc", "**/resources/**", // assets
            ".github/*", "**/packaging/**", "**/logs/**", ".composite-enabled"
        ))
    }
    getByName("check").dependsOn("rat")
    getByName("rat").dependsOn("compileJava")
}

// AUDIT
tasks {
    val audit = named("audit") {
        group = "verification"
        onlyIf { project.hasProperty("audit.enabled") }
    }
    named("check") {
        dependsOn(audit)
    }
    named("test") {
        mustRunAfter(audit)
    }
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
    sign(publishing.publications["knotxDistribution"])
}
