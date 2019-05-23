plugins {
    id("java-library")
    id("maven-publish")
    id("signing")
}

project.group = "io.knotx"

repositories {
    jcenter()
    maven { url = uri("https://oss.sonatype.org/content/groups/staging/") }
    maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

apply(from = "gradle/javaAndUnitTests.gradle.kts")
apply(from = "gradle/integrationTests.gradle.kts")
apply(from = "gradle/distribution.gradle.kts")

// -----------------------------------------------------------------------------
// Publication
// -----------------------------------------------------------------------------
publishing {
    publications {
        create<MavenPublication>("knotxDistribution") {
            artifactId = "knotx-stack"
            artifact(tasks.named("assembleDistribution").get())
            pom {
                name.set("Knot.x Stack")
                description.set("Distribution of Knot.x containing all dependencies, configurations and running scripts.")
                url.set("http://knotx.io")
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
                    url.set("http://knotx.io")
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