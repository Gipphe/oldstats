package com.oldstats.tracking.farming

import com.oldstats.tracking.farming.PatchImplementation.ALLOTMENT
import com.oldstats.tracking.farming.PatchImplementation.ANIMA
import com.oldstats.tracking.farming.PatchImplementation.BELLADONNA
import com.oldstats.tracking.farming.PatchImplementation.BIG_COMPOST
import com.oldstats.tracking.farming.PatchImplementation.BUSH
import com.oldstats.tracking.farming.PatchImplementation.CACTUS
import com.oldstats.tracking.farming.PatchImplementation.CALQUAT
import com.oldstats.tracking.farming.PatchImplementation.CELASTRUS
import com.oldstats.tracking.farming.PatchImplementation.COMPOST
import com.oldstats.tracking.farming.PatchImplementation.CORAL
import com.oldstats.tracking.farming.PatchImplementation.CRYSTAL_TREE
import com.oldstats.tracking.farming.PatchImplementation.FLOWER
import com.oldstats.tracking.farming.PatchImplementation.FRUIT_TREE
import com.oldstats.tracking.farming.PatchImplementation.GRAPES
import com.oldstats.tracking.farming.PatchImplementation.HARDWOOD_TREE
import com.oldstats.tracking.farming.PatchImplementation.HERB
import com.oldstats.tracking.farming.PatchImplementation.HESPORI
import com.oldstats.tracking.farming.PatchImplementation.HOPS
import com.oldstats.tracking.farming.PatchImplementation.MUSHROOM
import com.oldstats.tracking.farming.PatchImplementation.REDWOOD
import com.oldstats.tracking.farming.PatchImplementation.SEAWEED
import com.oldstats.tracking.farming.PatchImplementation.SPIRIT_TREE
import com.oldstats.tracking.farming.PatchImplementation.TREE

data class PatchDef(val name: String, val varbit: Int, val implementation: PatchImplementation)

data class RegionDef(
    val name: String,
    val regionIds: List<Int>,
    val patches: List<PatchDef>,
    val boundsCheck: ((x: Int, y: Int, plane: Int) -> Boolean)? = null,
)

/**
 * Ported by hand from RuneLite's internal
 * `FarmingWorld`/`FarmingPatch`/`FarmingRegion` classes (decompiled, since
 * those classes are package-private and live inside the Time Tracking
 * plugin's own Guice scope — not reachable from an external plugin at
 * runtime). Region IDs, patch names and varbit IDs are transcribed
 * verbatim; only the disease-prediction-only `farmer`/`patchNumber` fields
 * were dropped since this plugin doesn't do growth timers.
 */
object FarmingWorldData {
    val regions: List<RegionDef> = listOf(
        RegionDef("Al Kharid", listOf(13106, 13362, 13105), listOf(PatchDef("", 4771, CACTUS))),
        RegionDef("Aldarin", listOf(5421, 5165, 5166, 5422, 5677, 5678), listOf(PatchDef("", 4771, HOPS))),
        RegionDef("Anglers' Retreat", listOf(9770), listOf(PatchDef("", 4771, HARDWOOD_TREE))),
        RegionDef("Ardougne", listOf(10290, 10546), listOf(PatchDef("", 4771, BUSH))),
        RegionDef(
            "Ardougne", listOf(10548),
            listOf(
                PatchDef("North", 4771, ALLOTMENT),
                PatchDef("South", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
            ),
        ),
        RegionDef(
            "Auburnvale", listOf(5427, 5428, 5684),
            listOf(PatchDef("", 4771, TREE), PatchDef("", 4772, BELLADONNA)),
        ),
        RegionDef("Avium Savannah", listOf(6702, 6446), listOf(PatchDef("", 4771, HARDWOOD_TREE))),
        RegionDef(
            "Brimhaven", listOf(11058, 11057),
            listOf(PatchDef("", 4771, FRUIT_TREE), PatchDef("", 4772, SPIRIT_TREE)),
        ),
        RegionDef(
            "Catherby", listOf(11062, 11061, 11318, 11317),
            listOf(
                PatchDef("North", 4771, ALLOTMENT),
                PatchDef("South", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
            ),
            boundsCheck = { x, y, plane ->
                if (x >= 2816 && y < 3456) x < 2840 && y >= 3440 && plane == 0 else true
            },
        ),
        RegionDef(
            "Catherby", listOf(11317),
            listOf(PatchDef("", 4771, FRUIT_TREE)),
            boundsCheck = { x, y, plane -> x >= 2840 || y < 3440 || plane == 1 },
        ),
        RegionDef(
            "Civitas illa Fortis", listOf(6192, 6447, 6448, 6449, 6191, 6193),
            listOf(
                PatchDef("North", 4771, ALLOTMENT),
                PatchDef("South", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
            ),
        ),
        RegionDef("Champions' Guild", listOf(12596), listOf(PatchDef("", 4771, BUSH))),
        RegionDef("Draynor Manor", listOf(12340), listOf(PatchDef("", 4771, BELLADONNA))),
        RegionDef("Entrana", listOf(11060, 11316), listOf(PatchDef("", 4771, HOPS))),
        RegionDef(
            "Etceteria", listOf(10300),
            listOf(PatchDef("", 4771, BUSH), PatchDef("", 4772, SPIRIT_TREE)),
        ),
        RegionDef("Falador", listOf(11828, 12084), listOf(PatchDef("", 4771, TREE))),
        RegionDef(
            "Falador", listOf(12083),
            listOf(
                PatchDef("North West", 4771, ALLOTMENT),
                PatchDef("South East", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
            ),
            boundsCheck = { _, y, _ -> y >= 3272 },
        ),
        RegionDef(
            "Fossil Island", listOf(14651, 14907, 14908, 15164, 14652, 14906, 14650, 15162, 15163),
            listOf(
                PatchDef("East", 4771, HARDWOOD_TREE),
                PatchDef("Middle", 4772, HARDWOOD_TREE),
                PatchDef("West", 4773, HARDWOOD_TREE),
            ),
            boundsCheck = { x, y, plane ->
                when {
                    x == 3753 && y in 3868..3870 -> false
                    (x == 3729 || x == 3728 || x == 3747 || x == 3746) && y in 3830..3832 -> false
                    else -> plane == 0
                }
            },
        ),
        RegionDef(
            "Seaweed", listOf(15008),
            listOf(PatchDef("North", 4771, SEAWEED), PatchDef("South", 4772, SEAWEED)),
        ),
        RegionDef(
            "Gnome Stronghold", listOf(9781, 9782, 9526, 9525),
            listOf(PatchDef("", 4771, TREE), PatchDef("", 4772, FRUIT_TREE)),
        ),
        RegionDef(
            "Great Conch",
            listOf(12581, 12325, 12326, 12327, 12580, 12582, 12583, 12836, 12837, 12838, 12839, 13092, 13093, 13194),
            listOf(
                PatchDef("East", 4771, CORAL),
                PatchDef("West", 4772, CORAL),
                PatchDef("", 4773, CALQUAT),
            ),
        ),
        RegionDef("Harmony", listOf(15148), listOf(PatchDef("", 4771, ALLOTMENT), PatchDef("", 4772, HERB))),
        RegionDef(
            "Kastori", listOf(5423, 5167, 5424),
            listOf(PatchDef("", 4771, CALQUAT), PatchDef("", 4772, FRUIT_TREE), PatchDef("", 4773, FLOWER)),
        ),
        RegionDef(
            "Kourend", listOf(6967, 6711),
            listOf(
                PatchDef("North East", 4771, ALLOTMENT),
                PatchDef("South West", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
                PatchDef("", 7904, SPIRIT_TREE),
            ),
        ),
        RegionDef(
            "Kourend (vineyard)", listOf(7223),
            listOf(
                PatchDef("East 1", 4953, GRAPES), PatchDef("East 2", 4954, GRAPES), PatchDef("East 3", 4955, GRAPES),
                PatchDef("East 4", 4956, GRAPES), PatchDef("East 5", 4957, GRAPES), PatchDef("East 6", 4958, GRAPES),
                PatchDef("West 1", 4959, GRAPES), PatchDef("West 2", 4960, GRAPES), PatchDef("West 3", 4961, GRAPES),
                PatchDef("West 4", 4962, GRAPES), PatchDef("West 5", 4963, GRAPES), PatchDef("West 6", 4964, GRAPES),
            ),
        ),
        RegionDef("Lletya", listOf(9265, 11103), listOf(PatchDef("", 4771, FRUIT_TREE))),
        RegionDef("Lumbridge", listOf(12851), listOf(PatchDef("", 4771, HOPS))),
        RegionDef("Lumbridge", listOf(12594, 12850), listOf(PatchDef("", 4771, TREE))),
        RegionDef("Morytania", listOf(13622, 13878), listOf(PatchDef("Mushroom", 4771, MUSHROOM))),
        RegionDef(
            "Morytania", listOf(14391, 14390),
            listOf(
                PatchDef("North West", 4771, ALLOTMENT),
                PatchDef("South East", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4774, HERB),
                PatchDef("", 4775, COMPOST),
            ),
        ),
        RegionDef(
            "Port Sarim", listOf(12082, 12083),
            listOf(PatchDef("", 4771, SPIRIT_TREE)),
            boundsCheck = { _, y, _ -> y < 3272 },
        ),
        RegionDef("Rimmington", listOf(11570, 11826), listOf(PatchDef("", 4771, BUSH))),
        RegionDef("Seers' Village", listOf(10551, 10550), listOf(PatchDef("", 4771, HOPS))),
        RegionDef("Tai Bwo Wannai", listOf(11056), listOf(PatchDef("", 4771, CALQUAT))),
        RegionDef("Taverley", listOf(11573, 11829), listOf(PatchDef("", 4771, TREE))),
        RegionDef("Tree Gnome Village", listOf(9777, 10033), listOf(PatchDef("", 4771, FRUIT_TREE))),
        RegionDef("Troll Stronghold", listOf(11321), listOf(PatchDef("", 4771, HERB))),
        RegionDef("Varrock", listOf(12854, 12853), listOf(PatchDef("", 4771, TREE))),
        RegionDef("Yanille", listOf(10288), listOf(PatchDef("", 4771, HOPS))),
        RegionDef("Weiss", listOf(11325), listOf(PatchDef("", 4771, HERB))),
        RegionDef("Farming Guild", listOf(5021), listOf(PatchDef("Hespori", 7908, HESPORI))),
        RegionDef(
            "Farming Guild", listOf(4922, 5177, 5178, 5179, 4921, 4923, 4665, 4666, 4667),
            listOf(
                PatchDef("", 7905, TREE),
                PatchDef("", 4775, HERB),
                PatchDef("", 4772, BUSH),
                PatchDef("", 7906, FLOWER),
                PatchDef("North", 4773, ALLOTMENT),
                PatchDef("South", 4774, ALLOTMENT),
                PatchDef("", 7912, BIG_COMPOST),
                PatchDef("", 7904, CACTUS),
                PatchDef("", 4771, SPIRIT_TREE),
                PatchDef("", 7909, FRUIT_TREE),
                PatchDef("Anima", 7911, ANIMA),
                PatchDef("", 7910, CELASTRUS),
                PatchDef("", 7907, REDWOOD),
            ),
        ),
        RegionDef(
            "Prifddinas",
            listOf(13151, 12895, 12894, 13150, 12994, 12993, 12737, 12738, 12126, 12127, 13250),
            listOf(
                PatchDef("North", 4771, ALLOTMENT),
                PatchDef("South", 4772, ALLOTMENT),
                PatchDef("", 4773, FLOWER),
                PatchDef("", 4775, CRYSTAL_TREE),
                PatchDef("", 4774, COMPOST),
            ),
        ),
    )

    /** Region IDs mapping to more than one [RegionDef] need the bounds check to disambiguate. */
    val regionsById: Map<Int, List<RegionDef>> = buildMap<Int, MutableList<RegionDef>> {
        for (region in regions) {
            for (id in region.regionIds) {
                getOrPut(id) { mutableListOf() }.add(region)
            }
        }
    }
}
