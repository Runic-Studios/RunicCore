plugins {
    `java-library`
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(commonLibs.worldguardevents)
    implementation(commonLibs.taskchain)
    implementation(commonLibs.springdatamongodb)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.craftbukkit)
    implementation(commonLibs.mongodbdriversync)
    implementation(commonLibs.mongodbdrivercore)
    implementation(commonLibs.jedis)
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.apache.commons:commons-math3:3.6.1")
    implementation("org.apache.commons:commons-pool2:2.11.1")
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.nametagedit)
    compileOnly(commonLibs.nbtapi)
    compileOnly(project(":Projects:Chat"))
    compileOnly(project(":Projects:Items"))
    compileOnly(project(":Projects:Npcs"))
    compileOnly(project(":Projects:Restart"))
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.tabbed)
    compileOnly(commonLibs.worldguardcore)
    compileOnly(commonLibs.worldguardlegacy)
    compileOnly(commonLibs.viaversion)
    implementation(commonLibs.acf)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "com.runicrealms.plugin"
            artifactId = "core"
            version = "1.0-SNAPSHOT"
            from(components["java"])
        }
    }
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
//    build {
//        dependsOn(shadowJar)
//    }
}

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")