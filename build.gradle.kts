import dev.kikugie.semver.data.Version
import groovy.json.JsonSlurper
import me.modmuss50.mpp.ReleaseType
import kotlin.text.first

plugins {
    id("fabric-loom") version "1.11+"
    id("me.modmuss50.mod-publish-plugin") version "0.8.4"
    id("dev.kikugie.fletching-table.fabric") version "0.1+"
}

val minecraftVersion: String = stonecutter.current.version
val latestVersion: String = stonecutter.versions.last().version

@Suppress("UNCHECKED_CAST")
val modVersions: Map<String, List<String>> =
    JsonSlurper().parse(file("versions/modrinth.json")) as Map<String, List<String>>

var archivesBaseName: String = property("archives_base_name").toString()
version = "${property("mod_version")}+$minecraftVersion"
group = property("maven_group") as String

@Override
fun file(path: String): File {
    return rootProject.file(path)
}

@Override
fun fileTree(path: String): ConfigurableFileTree {
    return rootProject.fileTree(path)
}

base {
    archivesName.set(property("archives_base_name").toString())
}

val accessWidener = findAccessWidener()

fun findAccessWidenerFile(): File {
    return file("src/main/resources/accesswideners/${accessWidener.second}")
}

fun findAccessWidener(): Pair<String, String> {
    val wideners = fileTree("src/main/resources/accesswideners")
    val versions: MutableSet<Version> = sortedSetOf();
    val sampleFileName = wideners.first().name
    val filePrefix = sampleFileName.substringBefore('.')
    val fileSuffix = sampleFileName.substringAfterLast('.')

    wideners.visit {
        val version = file.name.substringAfter('.').substringBeforeLast('.')
        versions += sc.parse(version)
    }

    var returnValue: Pair<String, String>? = null;
    for (version in versions.reversed()) {
        if (sc.eval(minecraftVersion, ">=${version.value}")) {
            returnValue = Pair(version.value, "$filePrefix.${version.value}.$fileSuffix")
            break
        }
    }

    if (returnValue == null) {
        throw MissingResourceException("No valid access widener for $minecraftVersion found!")
    }

    logger.info("Excluding for $minecraftVersion")
    for (version in versions) {
        if (version.value != returnValue.first) {
            logger.info("Excluding: ${version.value}")
            tasks.processResources.get().exclude("**/$filePrefix.${version.value}.$fileSuffix")
        }
    }

    return returnValue
}

loom {
    splitEnvironmentSourceSets()
    accessWidenerPath = findAccessWidenerFile()

    runConfigs.all {
        ideConfigGenerated(true)
        runDir = "../../run"
    }

    runConfigs["server"].apply {
        runDir = "../../run_server"
    }

    runConfigs["client"].apply {
        vmArgs("-Dmixin.debug.export=true")
        programArgs(
            "--quickPlaySingleplayer \"New World (1)\"",
            "--uuid 2e7c2349-94ec-4862-8b68-344d049840d2 --username AwakenedRedstone"
        )
    }

    mods {
        register("default_components") {
            sourceSet(sourceSets.main.get())
        }
    }
}

stonecutter {
    fun registerMacro(name: String, predicate: String, then: String, `else`: String) {
        swaps[name] = when {
            eval(current.version, predicate) -> then
            else -> `else`
        }
    }

    registerMacro(
        "WhitelistProfile",
        ">=1.21.9",
        "net.minecraft.server.PlayerConfigEntry",
        "com.mojang.authlib.GameProfile"
    )
}

fletchingTable {
    j52j.register("main") {
        extension("json", "default_components.mixins.json5")
    }
}

repositories {
    maven("https://maven.nucleoid.xyz")
    //maven("https://maven.wispforest.io/releases/")
}

dependencies {
    // To change the versions see the gradle.properties file
    minecraft("com.mojang:minecraft:$minecraftVersion")
    mappings("net.fabricmc:yarn:$minecraftVersion+build.${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:${property("fabric_version")}")
    modImplementation(include("xyz.nucleoid:packet-tweaker:${property("packet_tweaker")}")!!)

    //modCompileOnly("io.wispforest:owo-lib:${property("owo_version")}")
    //include(api("blue.endless:jankson:${property("jankson_version")}")!!)
}

tasks.processResources {
    val versions = JsonSlurper().parse(file("versions/versions.json")) as Map<*, *>
    val map = mapOf(
        "version" to version,
        "minecraft" to versions[minecraftVersion],
        "accesswidener" to accessWidener.second
    )

    inputs.properties(map)

    filesMatching("fabric.mod.json") {
        expand(map)
    }
}

val targetJavaVersion = 21
tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
    }
    // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
    // if it is present.
    // If you remove this line, sources will not be generated.
    withSourcesJar()
}

tasks.jar {
    from("LICENSE") {
        rename { "${it}_${archivesBaseName}" }
    }
}

val CHANGELOG: String = if (file("CHANGELOG.md").exists()) {
    file("CHANGELOG.md").readText()
} else {
    "No changelog provided"
}

val projectVersion: String = property("mod_version").toString()
val projectVersionNumber: List<String> = projectVersion.split(Regex("-"), 2)
var projectVersionName = "Release ${projectVersionNumber[0]}"
var projectVersionType = ReleaseType.STABLE
if (projectVersion.contains("beta")) {
    val projectBeta: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
    projectVersionName = "${projectVersionNumber[0]} - Beta ${projectBeta[1]}"
    projectVersionType = ReleaseType.BETA
} else if (projectVersion.contains("alpha")) {
    val projectAlpha: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
    projectVersionName = "${projectVersionNumber[0]} - Alpha ${projectAlpha[1]}"
    projectVersionType = ReleaseType.ALPHA
} else if (projectVersion.contains("rc")) {
    val projectRC: List<String> = projectVersionNumber[1].split(Regex("\\."), 2)
    projectVersionName = "${projectVersionNumber[0]} - Release Candidate ${projectRC[1]}"
    projectVersionType = ReleaseType.BETA
}

fun <T> action(action: Action<T>): Action<T> where T : Task {
    return action
}

val checks: Action<Task> = action {
    if (modVersions[minecraftVersion] == null) {
        throw MissingResourceException("Please update modrinth.json, missing $minecraftVersion")
    }

    if (CHANGELOG.isEmpty()) {
        throw MissingResourceException("Update the changelog!")
    }
}

//tasks.getByName("modrinth").doFirst(checks)
//tasks.getByName("modrinthSyncBody").doFirst(checks)
tasks.getByName("publishMods").doFirst(checks)

publishMods {
    file = (tasks.getByName("remapJar") as AbstractArchiveTask).archiveFile
    changelog = CHANGELOG
    type = projectVersionType
    modLoaders.add("fabric")
    displayName = "[$minecraftVersion] $projectVersionName"

    curseforge {
        projectId = "1099230"
        projectSlug = "default-components" // Required for discord webhook
        accessToken = providers.gradleProperty("CURSEFORGE_TOKEN")
        minecraftVersions = modVersions[minecraftVersion]
        requires("fabric-api")
    }

    modrinth {
        projectId = "ReIHZWEq"
        accessToken = providers.gradleProperty("MODRINTH_TOKEN")
        minecraftVersions = modVersions[minecraftVersion]
        requires("fabric-api")
    }

    if (minecraftVersion == latestVersion) {
        discord {
            content = """
                # Default Components | $projectVersionName
                
                $CHANGELOG
            """.trimIndent()

            avatarUrl = "https://cdn.discordapp.com/avatars/1268055578073108574/73106a33f497ea5f2c676bcfb4816917.webp"
            username = "Mod updates"
            webhookUrl = providers.gradleProperty("DISCORD_WEBHOOK")
            dryRunWebhookUrl = providers.gradleProperty("DRY_WEBHOOK")
            style {
                look = "MODERN"
                link = "BUTTON"
                thumbnailUrl = "https://cdn.modrinth.com/data/ReIHZWEq/f2e488e2f54f0e0605ea45d7208ca9311db050a6.png"
                color = "modrinth"
            }
        }
    }
}
