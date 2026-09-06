plugins {
    java
    kotlin("jvm") version "1.9.24"
    id("com.gradleup.shadow") version "8.3.11"
}

group = "com.oldstats"
version = "1.0.0"

val runeLiteVersion = "1.12.38"

repositories {
    mavenLocal()
    maven(url = "https://repo.runelite.net")
    mavenCentral()
}

dependencies {
    compileOnly(group = "net.runelite", name = "client", version = runeLiteVersion)
    implementation(kotlin("stdlib"))

    testImplementation(group = "net.runelite", name = "client", version = runeLiteVersion)
    testImplementation(group = "net.runelite", name = "jshell", version = runeLiteVersion)
    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("org.mockito:mockito-core:5.14.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.4.0")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
}

tasks.test {
    useJUnit()
    systemProperty("runelite.static.test", "true")
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Launches a real RuneLite client with this plugin preloaded (see OldStatsPluginTest.kt)"
    mainClass.set("com.oldstats.OldStatsPluginTestKt")
    classpath = sourceSets.test.get().runtimeClasspath
    jvmArgs("-ea") // required by ExternalPluginManager.loadBuiltin as a development safety check
}

// `shadowJar` (from the shadow plugin) bundles kotlin-stdlib into the built
// jar — needed to sideload this plugin into an existing RuneLite install,
// since RuneLite's PluginClassLoader gives each sideloaded jar its own
// classloader with no access to Kotlin's runtime otherwise. Output:
// build/libs/oldstats-plugin-<version>-all.jar — copy into
// ~/.runelite/sideloaded-plugins/ (see README.md).
