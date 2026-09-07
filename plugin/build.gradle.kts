plugins {
    java
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

    testImplementation(group = "net.runelite", name = "client", version = runeLiteVersion)
    testImplementation(group = "net.runelite", name = "jshell", version = runeLiteVersion)
    testImplementation(group = "junit", name = "junit", version = "4.13.2")
    testImplementation("org.slf4j:slf4j-simple:1.7.36")
    testImplementation("org.mockito:mockito-core:5.14.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.test {
    useJUnit()
    systemProperty("runelite.static.test", "true")
}

tasks.register<JavaExec>("runClient") {
    group = "application"
    description = "Launches a real RuneLite client with this plugin preloaded (see OldStatsPluginTest.java)"
    mainClass.set("com.oldstats.OldStatsPluginTest")
    classpath = sourceSets.test.get().runtimeClasspath
    jvmArgs("-ea -XX:ErrorFile=/home/gipphe/.local/share/bolt-launcher/.runelite/logs/jvm_crash_pid_%p.log -Duser.home=/home/gipphe/.local/share/bolt-launcher") // required by ExternalPluginManager.loadBuiltin as a development safety check
}

// No extra runtime dependencies beyond what `client` (compileOnly) already
// provides on RuneLite's own classpath, so the plain `jar` task's output is
// sufficient for sideloading — no fat-jar/shadow plugin needed. Output:
// build/libs/oldstats-plugin-<version>.jar — copy into
// ~/.runelite/sideloaded-plugins/ (see README.md).
