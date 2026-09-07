package com.oldstats;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

/**
 * Launches a real RuneLite client with {@link OldStatsPlugin} preloaded, so it
 * can be tried against a live game without publishing to the Plugin Hub — the
 * standard pattern for developing a RuneLite plugin outside the main RuneLite
 * source tree. Run via {@code ./gradlew runClient} (see build.gradle.kts), or
 * directly from an IDE by running this class's {@code main}.
 */
public class OldStatsPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(OldStatsPlugin.class);
        RuneLite.main(args);
    }
}
