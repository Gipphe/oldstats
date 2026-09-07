package com.oldstats.tracking.farming;

import static com.oldstats.tracking.farming.PatchImplementation.ALLOTMENT;
import static com.oldstats.tracking.farming.PatchImplementation.ANIMA;
import static com.oldstats.tracking.farming.PatchImplementation.BELLADONNA;
import static com.oldstats.tracking.farming.PatchImplementation.BIG_COMPOST;
import static com.oldstats.tracking.farming.PatchImplementation.BUSH;
import static com.oldstats.tracking.farming.PatchImplementation.CACTUS;
import static com.oldstats.tracking.farming.PatchImplementation.CALQUAT;
import static com.oldstats.tracking.farming.PatchImplementation.CELASTRUS;
import static com.oldstats.tracking.farming.PatchImplementation.COMPOST;
import static com.oldstats.tracking.farming.PatchImplementation.CORAL;
import static com.oldstats.tracking.farming.PatchImplementation.CRYSTAL_TREE;
import static com.oldstats.tracking.farming.PatchImplementation.FLOWER;
import static com.oldstats.tracking.farming.PatchImplementation.FRUIT_TREE;
import static com.oldstats.tracking.farming.PatchImplementation.GRAPES;
import static com.oldstats.tracking.farming.PatchImplementation.HARDWOOD_TREE;
import static com.oldstats.tracking.farming.PatchImplementation.HERB;
import static com.oldstats.tracking.farming.PatchImplementation.HESPORI;
import static com.oldstats.tracking.farming.PatchImplementation.HOPS;
import static com.oldstats.tracking.farming.PatchImplementation.MUSHROOM;
import static com.oldstats.tracking.farming.PatchImplementation.REDWOOD;
import static com.oldstats.tracking.farming.PatchImplementation.SEAWEED;
import static com.oldstats.tracking.farming.PatchImplementation.SPIRIT_TREE;
import static com.oldstats.tracking.farming.PatchImplementation.TREE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ported by hand from RuneLite's internal
 * {@code FarmingWorld}/{@code FarmingPatch}/{@code FarmingRegion} classes (decompiled, since
 * those classes are package-private and live inside the Time Tracking
 * plugin's own Guice scope — not reachable from an external plugin at
 * runtime). Region IDs, patch names and varbit IDs are transcribed
 * verbatim; only the disease-prediction-only {@code farmer}/{@code patchNumber} fields
 * were dropped since this plugin doesn't do growth timers.
 */
public final class FarmingWorldData {
    private FarmingWorldData() {}

    public static final List<RegionDef> regions = List.of(
        new RegionDef("Al Kharid", List.of(13106, 13362, 13105), List.of(new PatchDef("", 4771, CACTUS))),
        new RegionDef("Aldarin", List.of(5421, 5165, 5166, 5422, 5677, 5678), List.of(new PatchDef("", 4771, HOPS))),
        new RegionDef("Anglers' Retreat", List.of(9770), List.of(new PatchDef("", 4771, HARDWOOD_TREE))),
        new RegionDef("Ardougne", List.of(10290, 10546), List.of(new PatchDef("", 4771, BUSH))),
        new RegionDef(
            "Ardougne", List.of(10548),
            List.of(
                new PatchDef("North", 4771, ALLOTMENT),
                new PatchDef("South", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST)
            )
        ),
        new RegionDef(
            "Auburnvale", List.of(5427, 5428, 5684),
            List.of(new PatchDef("", 4771, TREE), new PatchDef("", 4772, BELLADONNA))
        ),
        new RegionDef("Avium Savannah", List.of(6702, 6446), List.of(new PatchDef("", 4771, HARDWOOD_TREE))),
        new RegionDef(
            "Brimhaven", List.of(11058, 11057),
            List.of(new PatchDef("", 4771, FRUIT_TREE), new PatchDef("", 4772, SPIRIT_TREE))
        ),
        new RegionDef(
            "Catherby", List.of(11062, 11061, 11318, 11317),
            List.of(
                new PatchDef("North", 4771, ALLOTMENT),
                new PatchDef("South", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST)
            ),
            (x, y, plane) -> (x >= 2816 && y < 3456) ? (x < 2840 && y >= 3440 && plane == 0) : true
        ),
        new RegionDef(
            "Catherby", List.of(11317),
            List.of(new PatchDef("", 4771, FRUIT_TREE)),
            (x, y, plane) -> x >= 2840 || y < 3440 || plane == 1
        ),
        new RegionDef(
            "Civitas illa Fortis", List.of(6192, 6447, 6448, 6449, 6191, 6193),
            List.of(
                new PatchDef("North", 4771, ALLOTMENT),
                new PatchDef("South", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST)
            )
        ),
        new RegionDef("Champions' Guild", List.of(12596), List.of(new PatchDef("", 4771, BUSH))),
        new RegionDef("Draynor Manor", List.of(12340), List.of(new PatchDef("", 4771, BELLADONNA))),
        new RegionDef("Entrana", List.of(11060, 11316), List.of(new PatchDef("", 4771, HOPS))),
        new RegionDef(
            "Etceteria", List.of(10300),
            List.of(new PatchDef("", 4771, BUSH), new PatchDef("", 4772, SPIRIT_TREE))
        ),
        new RegionDef("Falador", List.of(11828, 12084), List.of(new PatchDef("", 4771, TREE))),
        new RegionDef(
            "Falador", List.of(12083),
            List.of(
                new PatchDef("North West", 4771, ALLOTMENT),
                new PatchDef("South East", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST)
            ),
            (x, y, plane) -> y >= 3272
        ),
        new RegionDef(
            "Fossil Island", List.of(14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163),
            List.of(
                new PatchDef("East", 4771, HARDWOOD_TREE),
                new PatchDef("Middle", 4772, HARDWOOD_TREE),
                new PatchDef("West", 4773, HARDWOOD_TREE)
            ),
            (x, y, plane) -> {
                if (x == 3753 && y >= 3868 && y <= 3870) return false;
                if ((x == 3729 || x == 3728 || x == 3747 || x == 3746) && y >= 3830 && y <= 3832) return false;
                return plane == 0;
            }
        ),
        new RegionDef(
            "Seaweed", List.of(15008),
            List.of(new PatchDef("North", 4771, SEAWEED), new PatchDef("South", 4772, SEAWEED))
        ),
        new RegionDef(
            "Gnome Stronghold", List.of(9781, 9782, 9526, 9525),
            List.of(new PatchDef("", 4771, TREE), new PatchDef("", 4772, FRUIT_TREE))
        ),
        new RegionDef(
            "Great Conch",
            List.of(12581, 12325, 12326, 12327, 12580, 12582, 12583, 12836, 12837, 12838, 12839, 13092, 13093, 13194),
            List.of(
                new PatchDef("East", 4771, CORAL),
                new PatchDef("West", 4772, CORAL),
                new PatchDef("", 4773, CALQUAT)
            )
        ),
        new RegionDef("Harmony", List.of(15148), List.of(new PatchDef("", 4771, ALLOTMENT), new PatchDef("", 4772, HERB))),
        new RegionDef(
            "Kastori", List.of(5423, 5167, 5424),
            List.of(new PatchDef("", 4771, CALQUAT), new PatchDef("", 4772, FRUIT_TREE), new PatchDef("", 4773, FLOWER))
        ),
        new RegionDef(
            "Kourend", List.of(6967, 6711),
            List.of(
                new PatchDef("North East", 4771, ALLOTMENT),
                new PatchDef("South West", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST),
                new PatchDef("", 7904, SPIRIT_TREE)
            )
        ),
        new RegionDef(
            "Kourend (vineyard)", List.of(7223),
            List.of(
                new PatchDef("East 1", 4953, GRAPES), new PatchDef("East 2", 4954, GRAPES), new PatchDef("East 3", 4955, GRAPES),
                new PatchDef("East 4", 4956, GRAPES), new PatchDef("East 5", 4957, GRAPES), new PatchDef("East 6", 4958, GRAPES),
                new PatchDef("West 1", 4959, GRAPES), new PatchDef("West 2", 4960, GRAPES), new PatchDef("West 3", 4961, GRAPES),
                new PatchDef("West 4", 4962, GRAPES), new PatchDef("West 5", 4963, GRAPES), new PatchDef("West 6", 4964, GRAPES)
            )
        ),
        new RegionDef("Lletya", List.of(9265, 11103), List.of(new PatchDef("", 4771, FRUIT_TREE))),
        new RegionDef("Lumbridge", List.of(12851), List.of(new PatchDef("", 4771, HOPS))),
        new RegionDef("Lumbridge", List.of(12594, 12850), List.of(new PatchDef("", 4771, TREE))),
        new RegionDef("Morytania", List.of(13622, 13878), List.of(new PatchDef("Mushroom", 4771, MUSHROOM))),
        new RegionDef(
            "Morytania", List.of(14391, 14390),
            List.of(
                new PatchDef("North West", 4771, ALLOTMENT),
                new PatchDef("South East", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4774, HERB),
                new PatchDef("", 4775, COMPOST)
            )
        ),
        new RegionDef(
            "Port Sarim", List.of(12082, 12083),
            List.of(new PatchDef("", 4771, SPIRIT_TREE)),
            (x, y, plane) -> y < 3272
        ),
        new RegionDef("Rimmington", List.of(11570, 11826), List.of(new PatchDef("", 4771, BUSH))),
        new RegionDef("Seers' Village", List.of(10551, 10550), List.of(new PatchDef("", 4771, HOPS))),
        new RegionDef("Tai Bwo Wannai", List.of(11056), List.of(new PatchDef("", 4771, CALQUAT))),
        new RegionDef("Taverley", List.of(11573, 11829), List.of(new PatchDef("", 4771, TREE))),
        new RegionDef("Tree Gnome Village", List.of(9777, 10033), List.of(new PatchDef("", 4771, FRUIT_TREE))),
        new RegionDef("Troll Stronghold", List.of(11321), List.of(new PatchDef("", 4771, HERB))),
        new RegionDef("Varrock", List.of(12854, 12853), List.of(new PatchDef("", 4771, TREE))),
        new RegionDef("Yanille", List.of(10288), List.of(new PatchDef("", 4771, HOPS))),
        new RegionDef("Weiss", List.of(11325), List.of(new PatchDef("", 4771, HERB))),
        new RegionDef("Farming Guild", List.of(5021), List.of(new PatchDef("Hespori", 7908, HESPORI))),
        new RegionDef(
            "Farming Guild", List.of(4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667),
            List.of(
                new PatchDef("", 7905, TREE),
                new PatchDef("", 4775, HERB),
                new PatchDef("", 4772, BUSH),
                new PatchDef("", 7906, FLOWER),
                new PatchDef("North", 4773, ALLOTMENT),
                new PatchDef("South", 4774, ALLOTMENT),
                new PatchDef("", 7912, BIG_COMPOST),
                new PatchDef("", 7904, CACTUS),
                new PatchDef("", 4771, SPIRIT_TREE),
                new PatchDef("", 7909, FRUIT_TREE),
                new PatchDef("Anima", 7911, ANIMA),
                new PatchDef("", 7910, CELASTRUS),
                new PatchDef("", 7907, REDWOOD)
            )
        ),
        new RegionDef(
            "Prifddinas",
            List.of(13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250),
            List.of(
                new PatchDef("North", 4771, ALLOTMENT),
                new PatchDef("South", 4772, ALLOTMENT),
                new PatchDef("", 4773, FLOWER),
                new PatchDef("", 4775, CRYSTAL_TREE),
                new PatchDef("", 4774, COMPOST)
            )
        )
    );

    /** Region IDs mapping to more than one {@link RegionDef} need the bounds check to disambiguate. */
    public static final Map<Integer, List<RegionDef>> regionsById;

    static {
        Map<Integer, List<RegionDef>> map = new HashMap<>();
        for (RegionDef region : regions) {
            for (int id : region.regionIds) {
                map.computeIfAbsent(id, k -> new ArrayList<>()).add(region);
            }
        }
        regionsById = Collections.unmodifiableMap(map);
    }
}
