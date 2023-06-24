val artifactName = "core"
val rrGroup: String by rootProject.extra
val rrVersion: String by rootProject.extra

plugins {
    `java-library`
    `maven-publish`
}

group = rrGroup
version = rrVersion

dependencies {
    compileOnly(commonLibs.worldguardevents)
    compileOnly(commonLibs.taskchain)
    compileOnly(commonLibs.springdatamongodb)
    compileOnly(commonLibs.holographicdisplays)
    compileOnly(commonLibs.spigot)
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
    compileOnly(project(":Projects:Common"))
    compileOnly(project(":Projects:Database"))
    compileOnly(commonLibs.paper)
    compileOnly(commonLibs.protocollib)
    compileOnly(commonLibs.tabbed)
    compileOnly(commonLibs.worldguardcore)
    compileOnly(commonLibs.worldguardlegacy)
    compileOnly(commonLibs.viaversion)
    compileOnly(commonLibs.acf)
    compileOnly(commonLibs.luckperms)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = rrGroup
            artifactId = artifactName
            version = rrVersion
            from(components["java"])
        }
    }
}