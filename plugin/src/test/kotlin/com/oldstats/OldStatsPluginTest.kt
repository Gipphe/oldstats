package com.oldstats

import net.runelite.client.RuneLite
import net.runelite.client.externalplugins.ExternalPluginManager

/**
 * Launches a real RuneLite client with [OldStatsPlugin] preloaded, so it can
 * be tried against a live game without publishing to the Plugin Hub — the
 * standard pattern for developing a RuneLite plugin outside the main
 * RuneLite source tree. Run via `./gradlew runClient` (see build.gradle.kts),
 * or directly from an IDE by running this file's `main`.
 */
fun main(args: Array<String>) {
    ExternalPluginManager.loadBuiltin(OldStatsPlugin::class.java)
    RuneLite.main(args)
}
