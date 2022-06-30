plugins {
    java

    val kotlinVersion: String by System.getProperties()
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("fabric-loom") version "0.12.+"
    id("io.github.juuxel.loom-quiltflower") version "1.7.+"

    id("com.modrinth.minotaur") version "2.+"
    id("me.hypherionmc.cursegradle") version "2.+"
    id("com.github.breadmoirai.github-release") version "2.+"
    id("io.github.p03w.machete") version "1.+"
    `maven-publish`
}

group = "dev.isxander"
version = "2.0.0"

repositories {
    mavenCentral()
    maven("https://maven.isxander.dev/releases")
    maven("https://maven.shedaniel.me")
    maven("https://maven.terraformersmc.com/releases")
}

val minecraftVersion: String by project

dependencies {
    val fabricLoaderVersion: String by project
    val kotlinVersion: String by System.getProperties()

    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.+:v2")

    modImplementation("net.fabricmc:fabric-loader:$fabricLoaderVersion")
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.56.3+1.19") {
        exclude(module = "fabric-renderer-indigo")
    }
    modImplementation("net.fabricmc:fabric-language-kotlin:1.8.1+kotlin.$kotlinVersion")

    include(implementation("dev.isxander.settxi:settxi-core:2.5.0")!!)
    include(implementation("dev.isxander.settxi:settxi-kotlinx-serialization:2.5.0")!!)
    include(modImplementation("dev.isxander.settxi:settxi-gui-cloth-config:2.5.0:fabric-1.19") {
        exclude(group = "me.shedaniel.cloth")
    })

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:7.+")
    modImplementation("com.terraformersmc:modmenu:4.0.0")
}

loom {
    clientOnlyMinecraftJar()
}

java {
    withSourcesJar()
}

tasks {
    remapJar {
        archiveClassifier.set("fabric-$minecraftVersion")
    }

    remapSourcesJar {
        archiveClassifier.set("fabric-$minecraftVersion-sources")
    }

    processResources {
        val modId: String by project
        val modName: String by project
        val modDescription: String by project
        val githubProject: String by project

        inputs.property("id", modId)
        inputs.property("group", project.group)
        inputs.property("name", modName)
        inputs.property("description", modDescription)
        inputs.property("version", project.version)
        inputs.property("github", githubProject)

        filesMatching(listOf("fabric.mod.json", "quilt.mod.json")) {
            expand(
                "id" to modId,
                "group" to project.group,
                "name" to modName,
                "description" to modDescription,
                "version" to project.version,
                "github" to githubProject,
            )
        }
    }

    register("releaseMod") {
        group = "mod"

        dependsOn("modrinth")
        dependsOn("modrinthSyncBody")
        dependsOn("curseforge")
        dependsOn("publish")
        dependsOn("githubRelease")
    }
}

val changelogText = file("changelogs/${project.version}.md").takeIf { it.exists() }?.readText() ?: "No changelog provided"

val modrinthId: String by project
if (modrinthId.isNotEmpty()) {
    modrinth {
        token.set(findProperty("modrinth.token")?.toString())
        projectId.set(modrinthId)
        versionNumber.set("${project.version}")
        versionType.set("release")
        uploadFile.set(tasks["remapJar"])
        gameVersions.set(listOf(minecraftVersion))
        loaders.set(listOf("fabric", "quilt"))
        changelog.set(changelogText)
        syncBodyFrom.set(file("README.md").readText())

        dependencies {
            required.project("fabric-language-kotlin")
            required.project("cloth-config")
            optional.project("modmenu")
        }
    }
}

val curseforgeId: String by project
if (hasProperty("curseforge.token") && curseforgeId.isNotEmpty()) {
    curseforge {
        apiKey = findProperty("curseforge.token")
        project(closureOf<me.hypherionmc.cursegradle.CurseProject> {
            mainArtifact(tasks["remapJar"], closureOf<me.hypherionmc.cursegradle.CurseArtifact> {
                displayName = "${project.version}"
            })

            id = curseforgeId
            releaseType = "release"
            addGameVersion(minecraftVersion)
            addGameVersion("Fabric")
            addGameVersion("Java 17")

            changelog = changelogText
            changelogType = "markdown"

            relations(closureOf<me.hypherionmc.cursegradle.CurseRelation> {
                requiredDependency("fabric-language-kotlin")
                requiredDependency("cloth-config")
                optionalDependency("modmenu")
            })
        })

        options(closureOf<me.hypherionmc.cursegradle.Options> {
            forgeGradleIntegration = false
        })
    }
}

githubRelease {
    token(findProperty("github.token")?.toString())

    val githubProject: String by project
    val split = githubProject.split("/")
    owner(split[0])
    repo(split[1])
    tagName("${project.version}")
    targetCommitish("1.19")
    body(changelogText)
    releaseAssets(tasks["remapJar"].outputs.files)
}

publishing {
    publications {
        create<MavenPublication>("mod") {
            groupId = "dev.isxander"
            artifactId = "pronounify"

            from(components["java"])
        }
    }

    repositories {
        if (hasProperty("xander-repo.username") && hasProperty("xander-repo.token")) {
            maven(url = "https://maven.isxander.dev/releases") {
                credentials {
                    username = property("xander-repo.username")?.toString()
                    password = property("xander-repo.token")?.toString()
                }
            }
        }
    }
}
