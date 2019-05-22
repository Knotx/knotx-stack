import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.invoke
import org.gradle.kotlin.dsl.register

val downloadDir = file("${buildDir}/download")
val distributionDir = file("${buildDir}/out")
val stackName = "knotx-stack"
val acmeDistribution = "knotx-stack-${version}.zip"

configurations {
    register("dist")
}

dependencies {
    "dist"("io.knotx:knotx-launcher:${project.version}")
    "dist"("io.knotx:knotx-server-http-core:${project.version}")
    "dist"("io.knotx:knotx-splitter-html:${project.version}")
    "dist"("io.knotx:knotx-assembler:${project.version}")
    "dist"("io.knotx:knotx-repository-connector-fs:${project.version}")
    "dist"("io.knotx:knotx-repository-connector-http:${project.version}")
    "dist"("io.knotx:knotx-fragments-handler-core:${project.version}")
    "dist"("io.knotx:knotx-action-http:${project.version}")
    "dist"("io.knotx:knotx-template-engine-core:${project.version}")
    "dist"("io.knotx:knotx-template-engine-handlebars:${project.version}")
}

val cleanDistribution = tasks.register<Delete>("cleanDistribution") {
    delete(listOf(distributionDir))
}

val copyConfigs = tasks.register<Copy>("copyConfigs") {
    from(file("src/main/packaging"))
    into(file("${distributionDir}/${stackName}"))
}

val downloadDeps = tasks.register<Copy>("downloadDeps") {
    from(configurations.named("dist"))
    into("${distributionDir}/${stackName}/lib")
}

val assembleDistribution = tasks.register<Zip>("assembleDistribution") {
    archiveName = acmeDistribution
    from(distributionDir)
}

assembleDistribution {
    dependsOn(copyConfigs, downloadDeps)
}

tasks.named("build") { finalizedBy(assembleDistribution) }
tasks.named("clean") { dependsOn(cleanDistribution) }