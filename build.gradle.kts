plugins {
    `java-library`
    `maven-publish`
}

group = "com.runicrealms.plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly(commonLibs.worldguardevents)
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.spigot)
    compileOnly(commonLibs.craftbukkit)
    compileOnly(commonLibs.mongodbdriversync)
    compileOnly(commonLibs.mongodbdrivercore)
    compileOnly(commonLibs.jedis)
    compileOnly(commonLibs.apachecommonslang)
    compileOnly(commonLibs.apachecommonsmath)
    compileOnly(commonLibs.apachecommonspool)
    compileOnly(commonLibs.mythicmobs)
    compileOnly(commonLibs.nametagedit)
    compileOnly(commonLibs.nbtapi)
    compileOnly(commonLibs.placeholderapi)
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
    compileOnly(commonLibs.acf)
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

tasks.register("wrapper")
tasks.register("prepareKotlinBuildScriptModel")