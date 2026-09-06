package com.oldstats.tracking.farming

import com.oldstats.tracking.farming.CropState.DEAD
import com.oldstats.tracking.farming.CropState.DISEASED
import com.oldstats.tracking.farming.CropState.EMPTY
import com.oldstats.tracking.farming.CropState.FILLING
import com.oldstats.tracking.farming.CropState.GROWING
import com.oldstats.tracking.farming.CropState.HARVESTABLE
import com.oldstats.tracking.farming.Produce.ANYHERB
import com.oldstats.tracking.farming.Produce.APPLE
import com.oldstats.tracking.farming.Produce.ASGARNIAN
import com.oldstats.tracking.farming.Produce.ATTAS
import com.oldstats.tracking.farming.Produce.AVANTOE
import com.oldstats.tracking.farming.Produce.BANANA
import com.oldstats.tracking.farming.Produce.BARLEY
import com.oldstats.tracking.farming.Produce.BELLADONNA
import com.oldstats.tracking.farming.Produce.BIG_COMPOST
import com.oldstats.tracking.farming.Produce.BIG_ROTTEN_TOMATO
import com.oldstats.tracking.farming.Produce.BIG_SUPERCOMPOST
import com.oldstats.tracking.farming.Produce.BIG_ULTRACOMPOST
import com.oldstats.tracking.farming.Produce.CABBAGE
import com.oldstats.tracking.farming.Produce.CACTUS
import com.oldstats.tracking.farming.Produce.CADANTINE
import com.oldstats.tracking.farming.Produce.CADAVABERRIES
import com.oldstats.tracking.farming.Produce.CALQUAT
import com.oldstats.tracking.farming.Produce.CAMPHOR
import com.oldstats.tracking.farming.Produce.CELASTRUS
import com.oldstats.tracking.farming.Produce.COMPOST
import com.oldstats.tracking.farming.Produce.COTTON
import com.oldstats.tracking.farming.Produce.CRYSTAL_TREE
import com.oldstats.tracking.farming.Produce.CURRY
import com.oldstats.tracking.farming.Produce.DRAGONFRUIT
import com.oldstats.tracking.farming.Produce.DWARF_WEED
import com.oldstats.tracking.farming.Produce.DWELLBERRIES
import com.oldstats.tracking.farming.Produce.ELKHORN_CORAL
import com.oldstats.tracking.farming.Produce.EMPTY_BIG_COMPOST_BIN
import com.oldstats.tracking.farming.Produce.EMPTY_COMPOST_BIN
import com.oldstats.tracking.farming.Produce.FLAX
import com.oldstats.tracking.farming.Produce.GOUTWEED
import com.oldstats.tracking.farming.Produce.GRAPE
import com.oldstats.tracking.farming.Produce.GUAM
import com.oldstats.tracking.farming.Produce.HAMMERSTONE
import com.oldstats.tracking.farming.Produce.HARRALANDER
import com.oldstats.tracking.farming.Produce.HEMP
import com.oldstats.tracking.farming.Produce.HESPORI
import com.oldstats.tracking.farming.Produce.HUASCA
import com.oldstats.tracking.farming.Produce.IASOR
import com.oldstats.tracking.farming.Produce.IRIT
import com.oldstats.tracking.farming.Produce.IRONWOOD
import com.oldstats.tracking.farming.Produce.JANGERBERRIES
import com.oldstats.tracking.farming.Produce.JUTE
import com.oldstats.tracking.farming.Produce.KRANDORIAN
import com.oldstats.tracking.farming.Produce.KRONOS
import com.oldstats.tracking.farming.Produce.KWUARM
import com.oldstats.tracking.farming.Produce.LANTADYME
import com.oldstats.tracking.farming.Produce.LIMPWURT
import com.oldstats.tracking.farming.Produce.MAGIC
import com.oldstats.tracking.farming.Produce.MAHOGANY
import com.oldstats.tracking.farming.Produce.MAPLE
import com.oldstats.tracking.farming.Produce.MARIGOLD
import com.oldstats.tracking.farming.Produce.MARRENTILL
import com.oldstats.tracking.farming.Produce.MUSHROOM
import com.oldstats.tracking.farming.Produce.NASTURTIUM
import com.oldstats.tracking.farming.Produce.OAK
import com.oldstats.tracking.farming.Produce.ONION
import com.oldstats.tracking.farming.Produce.ORANGE
import com.oldstats.tracking.farming.Produce.PALM
import com.oldstats.tracking.farming.Produce.PAPAYA
import com.oldstats.tracking.farming.Produce.PILLAR_CORAL
import com.oldstats.tracking.farming.Produce.PINEAPPLE
import com.oldstats.tracking.farming.Produce.POISON_IVY
import com.oldstats.tracking.farming.Produce.POTATO
import com.oldstats.tracking.farming.Produce.POTATO_CACTUS
import com.oldstats.tracking.farming.Produce.RANARR
import com.oldstats.tracking.farming.Produce.REDBERRIES
import com.oldstats.tracking.farming.Produce.SCARECROW
import com.oldstats.tracking.farming.Produce.REDWOOD
import com.oldstats.tracking.farming.Produce.ROSEMARY
import com.oldstats.tracking.farming.Produce.ROSEWOOD
import com.oldstats.tracking.farming.Produce.ROTTEN_TOMATO
import com.oldstats.tracking.farming.Produce.SEAWEED
import com.oldstats.tracking.farming.Produce.SNAPDRAGON
import com.oldstats.tracking.farming.Produce.SNAPE_GRASS
import com.oldstats.tracking.farming.Produce.SPIRIT_TREE
import com.oldstats.tracking.farming.Produce.STRAWBERRY
import com.oldstats.tracking.farming.Produce.SUPERCOMPOST
import com.oldstats.tracking.farming.Produce.SWEETCORN
import com.oldstats.tracking.farming.Produce.TARROMIN
import com.oldstats.tracking.farming.Produce.TEAK
import com.oldstats.tracking.farming.Produce.TOADFLAX
import com.oldstats.tracking.farming.Produce.TOMATO
import com.oldstats.tracking.farming.Produce.TORSTOL
import com.oldstats.tracking.farming.Produce.ULTRACOMPOST
import com.oldstats.tracking.farming.Produce.UMBRAL_CORAL
import com.oldstats.tracking.farming.Produce.WATERMELON
import com.oldstats.tracking.farming.Produce.WEEDS
import com.oldstats.tracking.farming.Produce.WHITEBERRIES
import com.oldstats.tracking.farming.Produce.WHITE_LILY
import com.oldstats.tracking.farming.Produce.WILDBLOOD
import com.oldstats.tracking.farming.Produce.WILLOW
import com.oldstats.tracking.farming.Produce.WOAD
import com.oldstats.tracking.farming.Produce.YANILLIAN
import com.oldstats.tracking.farming.Produce.YEW

/**
 * Ported by hand from RuneLite's internal
 * `PatchImplementation.forVarbitValue(int)` methods (decompiled, since that
 * class is package-private). Growth-stage sub-values (e.g. "2 of 5 ticks
 * grown") are dropped — only crop identity + [CropState] is kept, since this
 * plugin tracks activity, not growth timers.
 */
fun decodePatchState(implementation: PatchImplementation, value: Int): PatchState? = when (implementation) {
    PatchImplementation.MUSHROOM -> decodeMushroom(value)
    PatchImplementation.HESPORI -> decodeHespori(value)
    PatchImplementation.ALLOTMENT -> decodeAllotment(value)
    PatchImplementation.HERB -> decodeHerb(value)
    PatchImplementation.FLOWER -> decodeFlower(value)
    PatchImplementation.BUSH -> decodeBush(value)
    PatchImplementation.FRUIT_TREE -> decodeFruitTree(value)
    PatchImplementation.HOPS -> decodeHops(value)
    PatchImplementation.TREE -> decodeTree(value)
    PatchImplementation.HARDWOOD_TREE -> decodeHardwoodTree(value)
    PatchImplementation.REDWOOD -> decodeRedwood(value)
    PatchImplementation.SPIRIT_TREE -> decodeSpiritTree(value)
    PatchImplementation.ANIMA -> decodeAnima(value)
    PatchImplementation.BELLADONNA -> decodeBelladonna(value)
    PatchImplementation.CACTUS -> decodeCactus(value)
    PatchImplementation.CORAL -> decodeCoral(value)
    PatchImplementation.SEAWEED -> decodeSeaweed(value)
    PatchImplementation.CALQUAT -> decodeCalquat(value)
    PatchImplementation.CELASTRUS -> decodeCelastrus(value)
    PatchImplementation.GRAPES -> decodeGrapes(value)
    PatchImplementation.CRYSTAL_TREE -> decodeCrystalTree(value)
    PatchImplementation.COMPOST -> decodeCompost(value)
    PatchImplementation.BIG_COMPOST -> decodeBigCompost(value)
}

private fun decodeMushroom(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..9 -> PatchState(MUSHROOM, GROWING)
    in 10..15 -> PatchState(MUSHROOM, HARVESTABLE)
    in 16..20 -> PatchState(MUSHROOM, DISEASED)
    in 21..25 -> PatchState(MUSHROOM, DEAD)
    in 26..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeHespori(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..6 -> PatchState(HESPORI, GROWING)
    in 7..8 -> PatchState(HESPORI, HARVESTABLE)
    9 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeAllotment(v: Int): PatchState? = when (v) {
    in 0..3, in 4..5 -> PatchState(WEEDS, GROWING)
    in 6..9 -> PatchState(POTATO, GROWING)
    in 10..12 -> PatchState(POTATO, HARVESTABLE)
    in 13..16 -> PatchState(ONION, GROWING)
    in 17..19 -> PatchState(ONION, HARVESTABLE)
    in 20..23 -> PatchState(CABBAGE, GROWING)
    in 24..26 -> PatchState(CABBAGE, HARVESTABLE)
    in 27..30 -> PatchState(TOMATO, GROWING)
    in 31..33 -> PatchState(TOMATO, HARVESTABLE)
    in 34..39 -> PatchState(SWEETCORN, GROWING)
    in 40..42 -> PatchState(SWEETCORN, HARVESTABLE)
    in 43..48 -> PatchState(STRAWBERRY, GROWING)
    in 49..51 -> PatchState(STRAWBERRY, HARVESTABLE)
    in 52..59 -> PatchState(WATERMELON, GROWING)
    in 60..62 -> PatchState(WATERMELON, HARVESTABLE)
    in 63..69 -> PatchState(SNAPE_GRASS, GROWING)
    in 70..73 -> PatchState(POTATO, GROWING)
    in 74..76 -> PatchState(WEEDS, GROWING)
    in 77..80 -> PatchState(ONION, GROWING)
    in 81..83 -> PatchState(WEEDS, GROWING)
    in 84..87 -> PatchState(CABBAGE, GROWING)
    in 88..90 -> PatchState(WEEDS, GROWING)
    in 91..94 -> PatchState(TOMATO, GROWING)
    in 95..97 -> PatchState(WEEDS, GROWING)
    in 98..103 -> PatchState(SWEETCORN, GROWING)
    in 104..106 -> PatchState(WEEDS, GROWING)
    in 107..112 -> PatchState(STRAWBERRY, GROWING)
    in 113..115 -> PatchState(WEEDS, GROWING)
    in 116..123 -> PatchState(WATERMELON, GROWING)
    in 124..127 -> PatchState(WEEDS, GROWING)
    in 128..134 -> PatchState(SNAPE_GRASS, GROWING)
    in 135..137 -> PatchState(POTATO, DISEASED)
    in 138..140 -> PatchState(SNAPE_GRASS, HARVESTABLE)
    141 -> PatchState(WEEDS, GROWING)
    in 142..144 -> PatchState(ONION, DISEASED)
    in 145..148 -> PatchState(WEEDS, GROWING)
    in 149..151 -> PatchState(CABBAGE, DISEASED)
    in 152..155 -> PatchState(WEEDS, GROWING)
    in 156..158 -> PatchState(TOMATO, DISEASED)
    in 159..162 -> PatchState(WEEDS, GROWING)
    in 163..167 -> PatchState(SWEETCORN, DISEASED)
    in 168..171 -> PatchState(WEEDS, GROWING)
    in 172..176 -> PatchState(STRAWBERRY, DISEASED)
    in 177..180 -> PatchState(WEEDS, GROWING)
    in 181..187 -> PatchState(WATERMELON, DISEASED)
    in 188..192 -> PatchState(WEEDS, GROWING)
    in 193..195 -> PatchState(SNAPE_GRASS, DEAD)
    in 196..198 -> PatchState(SNAPE_GRASS, DISEASED)
    in 199..201 -> PatchState(POTATO, DEAD)
    in 202..204 -> PatchState(SNAPE_GRASS, DISEASED)
    205 -> PatchState(WEEDS, GROWING)
    in 206..208 -> PatchState(ONION, DEAD)
    in 209..211 -> PatchState(SNAPE_GRASS, DEAD)
    212 -> PatchState(WEEDS, GROWING)
    in 213..215 -> PatchState(CABBAGE, DEAD)
    in 216..219 -> PatchState(WEEDS, GROWING)
    in 220..222 -> PatchState(TOMATO, DEAD)
    in 223..226 -> PatchState(WEEDS, GROWING)
    in 227..231 -> PatchState(SWEETCORN, DEAD)
    in 232..235 -> PatchState(WEEDS, GROWING)
    in 236..240 -> PatchState(STRAWBERRY, DEAD)
    in 241..244 -> PatchState(WEEDS, GROWING)
    in 245..251 -> PatchState(WATERMELON, DEAD)
    in 252..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeHerb(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..7 -> PatchState(GUAM, GROWING)
    in 8..10 -> PatchState(GUAM, HARVESTABLE)
    in 11..14 -> PatchState(MARRENTILL, GROWING)
    in 15..17 -> PatchState(MARRENTILL, HARVESTABLE)
    in 18..21 -> PatchState(TARROMIN, GROWING)
    in 22..24 -> PatchState(TARROMIN, HARVESTABLE)
    in 25..28 -> PatchState(HARRALANDER, GROWING)
    in 29..31 -> PatchState(HARRALANDER, HARVESTABLE)
    in 32..35 -> PatchState(RANARR, GROWING)
    in 36..38 -> PatchState(RANARR, HARVESTABLE)
    in 39..42 -> PatchState(TOADFLAX, GROWING)
    in 43..45 -> PatchState(TOADFLAX, HARVESTABLE)
    in 46..49 -> PatchState(IRIT, GROWING)
    in 50..52 -> PatchState(IRIT, HARVESTABLE)
    in 53..56 -> PatchState(AVANTOE, GROWING)
    in 57..59 -> PatchState(AVANTOE, HARVESTABLE)
    in 60..63 -> PatchState(HUASCA, GROWING)
    in 64..66 -> PatchState(HUASCA, HARVESTABLE)
    67 -> PatchState(WEEDS, GROWING)
    in 68..71 -> PatchState(KWUARM, GROWING)
    in 72..74 -> PatchState(KWUARM, HARVESTABLE)
    in 75..78 -> PatchState(SNAPDRAGON, GROWING)
    in 79..81 -> PatchState(SNAPDRAGON, HARVESTABLE)
    in 82..85 -> PatchState(CADANTINE, GROWING)
    in 86..88 -> PatchState(CADANTINE, HARVESTABLE)
    in 89..92 -> PatchState(LANTADYME, GROWING)
    in 93..95 -> PatchState(LANTADYME, HARVESTABLE)
    in 96..99 -> PatchState(DWARF_WEED, GROWING)
    in 100..102 -> PatchState(DWARF_WEED, HARVESTABLE)
    in 103..106 -> PatchState(TORSTOL, GROWING)
    in 107..109 -> PatchState(TORSTOL, HARVESTABLE)
    in 128..130 -> PatchState(GUAM, DISEASED)
    in 131..133 -> PatchState(MARRENTILL, DISEASED)
    in 134..136 -> PatchState(TARROMIN, DISEASED)
    in 137..139 -> PatchState(HARRALANDER, DISEASED)
    in 140..142 -> PatchState(RANARR, DISEASED)
    in 143..145 -> PatchState(TOADFLAX, DISEASED)
    in 146..148 -> PatchState(IRIT, DISEASED)
    in 149..151 -> PatchState(AVANTOE, DISEASED)
    in 152..154 -> PatchState(KWUARM, DISEASED)
    in 155..157 -> PatchState(SNAPDRAGON, DISEASED)
    in 158..160 -> PatchState(CADANTINE, DISEASED)
    in 161..163 -> PatchState(LANTADYME, DISEASED)
    in 164..166 -> PatchState(DWARF_WEED, DISEASED)
    in 167..169 -> PatchState(TORSTOL, DISEASED)
    in 170..172 -> PatchState(ANYHERB, DEAD)
    in 173..175 -> PatchState(HUASCA, DISEASED)
    in 176..191 -> PatchState(WEEDS, GROWING)
    in 192..195 -> PatchState(GOUTWEED, GROWING)
    in 196..197 -> PatchState(GOUTWEED, HARVESTABLE)
    in 198..200 -> PatchState(GOUTWEED, DISEASED)
    in 201..203 -> PatchState(GOUTWEED, DEAD)
    in 204..219 -> PatchState(WEEDS, GROWING)
    in 221..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeFlower(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..11 -> PatchState(MARIGOLD, GROWING)
    12 -> PatchState(MARIGOLD, HARVESTABLE)
    in 13..16 -> PatchState(ROSEMARY, GROWING)
    17 -> PatchState(ROSEMARY, HARVESTABLE)
    in 18..21 -> PatchState(NASTURTIUM, GROWING)
    22 -> PatchState(NASTURTIUM, HARVESTABLE)
    in 23..26 -> PatchState(WOAD, GROWING)
    27 -> PatchState(WOAD, HARVESTABLE)
    in 28..31 -> PatchState(LIMPWURT, GROWING)
    32 -> PatchState(LIMPWURT, HARVESTABLE)
    in 33..35 -> PatchState(SCARECROW, GROWING)
    36 -> PatchState(SCARECROW, GROWING)
    in 37..40 -> PatchState(WHITE_LILY, GROWING)
    41 -> PatchState(WHITE_LILY, HARVESTABLE)
    in 42..71 -> PatchState(WEEDS, GROWING)
    in 72..75 -> PatchState(MARIGOLD, GROWING)
    76 -> PatchState(WEEDS, GROWING)
    in 77..80 -> PatchState(ROSEMARY, GROWING)
    81 -> PatchState(WEEDS, GROWING)
    in 82..85 -> PatchState(NASTURTIUM, GROWING)
    86 -> PatchState(WEEDS, GROWING)
    in 87..90 -> PatchState(WOAD, GROWING)
    91 -> PatchState(WEEDS, GROWING)
    in 92..95 -> PatchState(LIMPWURT, GROWING)
    in 96..100 -> PatchState(WEEDS, GROWING)
    in 101..104 -> PatchState(WHITE_LILY, GROWING)
    in 105..136 -> PatchState(WEEDS, GROWING)
    in 137..139 -> PatchState(MARIGOLD, DISEASED)
    in 140..141 -> PatchState(WEEDS, GROWING)
    in 142..144 -> PatchState(ROSEMARY, DISEASED)
    in 145..146 -> PatchState(WEEDS, GROWING)
    in 147..149 -> PatchState(NASTURTIUM, DISEASED)
    in 150..151 -> PatchState(WEEDS, GROWING)
    in 152..154 -> PatchState(WOAD, DISEASED)
    in 155..156 -> PatchState(WEEDS, GROWING)
    in 157..159 -> PatchState(LIMPWURT, DISEASED)
    in 160..165 -> PatchState(WEEDS, GROWING)
    in 166..168 -> PatchState(WHITE_LILY, DISEASED)
    in 169..200 -> PatchState(WEEDS, GROWING)
    in 201..204 -> PatchState(MARIGOLD, DEAD)
    205 -> PatchState(WEEDS, GROWING)
    in 206..209 -> PatchState(ROSEMARY, DEAD)
    210 -> PatchState(WEEDS, GROWING)
    in 211..214 -> PatchState(NASTURTIUM, DEAD)
    215 -> PatchState(WEEDS, GROWING)
    in 216..219 -> PatchState(WOAD, DEAD)
    220 -> PatchState(WEEDS, GROWING)
    in 221..224 -> PatchState(LIMPWURT, DEAD)
    in 225..229 -> PatchState(WEEDS, GROWING)
    in 230..233 -> PatchState(WHITE_LILY, DEAD)
    in 234..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeBush(v: Int): PatchState? = when (v) {
    in 0..3, 4 -> PatchState(WEEDS, GROWING)
    in 5..9 -> PatchState(REDBERRIES, GROWING)
    in 10..14 -> PatchState(REDBERRIES, HARVESTABLE)
    in 15..20 -> PatchState(CADAVABERRIES, GROWING)
    in 21..25 -> PatchState(CADAVABERRIES, HARVESTABLE)
    in 26..32 -> PatchState(DWELLBERRIES, GROWING)
    in 33..37 -> PatchState(DWELLBERRIES, HARVESTABLE)
    in 38..45 -> PatchState(JANGERBERRIES, GROWING)
    in 46..50 -> PatchState(JANGERBERRIES, HARVESTABLE)
    in 51..58 -> PatchState(WHITEBERRIES, GROWING)
    in 59..63 -> PatchState(WHITEBERRIES, HARVESTABLE)
    in 64..69 -> PatchState(WEEDS, GROWING)
    in 70..74 -> PatchState(REDBERRIES, DISEASED)
    in 75..79 -> PatchState(WEEDS, GROWING)
    in 80..85 -> PatchState(CADAVABERRIES, DISEASED)
    in 86..90 -> PatchState(WEEDS, GROWING)
    in 91..97 -> PatchState(DWELLBERRIES, DISEASED)
    in 98..102 -> PatchState(WEEDS, GROWING)
    in 103..110 -> PatchState(JANGERBERRIES, DISEASED)
    in 111..115 -> PatchState(WEEDS, GROWING)
    in 116..123 -> PatchState(WHITEBERRIES, DISEASED)
    in 124..133 -> PatchState(WEEDS, GROWING)
    in 134..138 -> PatchState(REDBERRIES, DEAD)
    in 139..143 -> PatchState(WEEDS, GROWING)
    in 144..149 -> PatchState(CADAVABERRIES, DEAD)
    in 150..154 -> PatchState(WEEDS, GROWING)
    in 155..161 -> PatchState(DWELLBERRIES, DEAD)
    in 162..166 -> PatchState(WEEDS, GROWING)
    in 167..174 -> PatchState(JANGERBERRIES, DEAD)
    in 175..179 -> PatchState(WEEDS, GROWING)
    in 180..187 -> PatchState(WHITEBERRIES, DEAD)
    in 188..196 -> PatchState(WEEDS, GROWING)
    in 197..204 -> PatchState(POISON_IVY, GROWING)
    in 205..209 -> PatchState(POISON_IVY, HARVESTABLE)
    in 210..216 -> PatchState(POISON_IVY, DISEASED)
    in 217..224 -> PatchState(POISON_IVY, DEAD)
    225 -> PatchState(POISON_IVY, DISEASED)
    in 226..249 -> PatchState(WEEDS, GROWING)
    250 -> PatchState(REDBERRIES, GROWING)
    251 -> PatchState(CADAVABERRIES, GROWING)
    252 -> PatchState(DWELLBERRIES, GROWING)
    253 -> PatchState(JANGERBERRIES, GROWING)
    254 -> PatchState(WHITEBERRIES, GROWING)
    255 -> PatchState(POISON_IVY, GROWING)
    else -> null
}

private fun decodeFruitTree(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..13 -> PatchState(APPLE, GROWING)
    in 14..20 -> PatchState(APPLE, HARVESTABLE)
    in 21..26 -> PatchState(APPLE, DISEASED)
    in 27..32 -> PatchState(APPLE, DEAD)
    33 -> PatchState(APPLE, HARVESTABLE)
    34 -> PatchState(APPLE, GROWING)
    in 35..40 -> PatchState(BANANA, GROWING)
    in 41..47 -> PatchState(BANANA, HARVESTABLE)
    in 48..53 -> PatchState(BANANA, DISEASED)
    in 54..59 -> PatchState(BANANA, DEAD)
    60 -> PatchState(BANANA, HARVESTABLE)
    61 -> PatchState(BANANA, GROWING)
    in 62..71 -> PatchState(WEEDS, GROWING)
    in 72..77 -> PatchState(ORANGE, GROWING)
    in 78..84 -> PatchState(ORANGE, HARVESTABLE)
    in 85..89 -> PatchState(ORANGE, DISEASED)
    90 -> PatchState(ORANGE, DISEASED)
    in 91..96 -> PatchState(ORANGE, DEAD)
    97 -> PatchState(ORANGE, HARVESTABLE)
    98 -> PatchState(ORANGE, GROWING)
    in 99..104 -> PatchState(CURRY, GROWING)
    in 105..111 -> PatchState(CURRY, HARVESTABLE)
    in 112..117 -> PatchState(CURRY, DISEASED)
    in 118..123 -> PatchState(CURRY, DEAD)
    124 -> PatchState(CURRY, HARVESTABLE)
    125 -> PatchState(CURRY, GROWING)
    in 126..135 -> PatchState(WEEDS, GROWING)
    in 136..141 -> PatchState(PINEAPPLE, GROWING)
    in 142..148 -> PatchState(PINEAPPLE, HARVESTABLE)
    in 149..154 -> PatchState(PINEAPPLE, DISEASED)
    in 155..160 -> PatchState(PINEAPPLE, DEAD)
    161 -> PatchState(PINEAPPLE, HARVESTABLE)
    162 -> PatchState(PINEAPPLE, GROWING)
    in 163..168 -> PatchState(PAPAYA, GROWING)
    in 169..175 -> PatchState(PAPAYA, HARVESTABLE)
    in 176..181 -> PatchState(PAPAYA, DISEASED)
    in 182..187 -> PatchState(PAPAYA, DEAD)
    188 -> PatchState(PAPAYA, HARVESTABLE)
    189 -> PatchState(PAPAYA, GROWING)
    in 190..199 -> PatchState(WEEDS, GROWING)
    in 200..205 -> PatchState(PALM, GROWING)
    in 206..212 -> PatchState(PALM, HARVESTABLE)
    in 213..218 -> PatchState(PALM, DISEASED)
    in 219..224 -> PatchState(PALM, DEAD)
    225 -> PatchState(PALM, HARVESTABLE)
    226 -> PatchState(PALM, GROWING)
    in 227..232 -> PatchState(DRAGONFRUIT, GROWING)
    in 233..239 -> PatchState(DRAGONFRUIT, HARVESTABLE)
    in 240..245 -> PatchState(DRAGONFRUIT, DISEASED)
    in 246..251 -> PatchState(DRAGONFRUIT, DEAD)
    252 -> PatchState(DRAGONFRUIT, HARVESTABLE)
    253 -> PatchState(DRAGONFRUIT, GROWING)
    in 254..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeHops(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..7 -> PatchState(HAMMERSTONE, GROWING)
    in 8..10 -> PatchState(HAMMERSTONE, HARVESTABLE)
    in 11..13 -> PatchState(HAMMERSTONE, DISEASED)
    in 14..18 -> PatchState(ASGARNIAN, GROWING)
    in 19..21 -> PatchState(ASGARNIAN, HARVESTABLE)
    in 22..25 -> PatchState(ASGARNIAN, DISEASED)
    in 26..31 -> PatchState(YANILLIAN, GROWING)
    in 32..34 -> PatchState(YANILLIAN, HARVESTABLE)
    in 35..39 -> PatchState(YANILLIAN, DISEASED)
    in 40..46 -> PatchState(KRANDORIAN, GROWING)
    in 47..49 -> PatchState(KRANDORIAN, HARVESTABLE)
    in 50..55 -> PatchState(KRANDORIAN, DISEASED)
    in 56..63 -> PatchState(WILDBLOOD, GROWING)
    in 64..66 -> PatchState(WILDBLOOD, HARVESTABLE)
    in 67..73 -> PatchState(WILDBLOOD, DISEASED)
    in 74..77 -> PatchState(BARLEY, GROWING)
    in 78..80 -> PatchState(BARLEY, HARVESTABLE)
    in 81..83 -> PatchState(BARLEY, DISEASED)
    in 84..88 -> PatchState(JUTE, GROWING)
    in 89..91 -> PatchState(JUTE, HARVESTABLE)
    in 92..95 -> PatchState(JUTE, DISEASED)
    in 96..98 -> PatchState(FLAX, GROWING)
    in 99..101 -> PatchState(FLAX, HARVESTABLE)
    in 102..103 -> PatchState(FLAX, DISEASED)
    in 104..107 -> PatchState(HEMP, GROWING)
    in 108..110 -> PatchState(HEMP, HARVESTABLE)
    in 111..113 -> PatchState(HEMP, DISEASED)
    in 114..118 -> PatchState(COTTON, GROWING)
    in 119..121 -> PatchState(COTTON, HARVESTABLE)
    in 122..125 -> PatchState(COTTON, DISEASED)
    in 126..131 -> PatchState(WEEDS, GROWING)
    in 132..135 -> PatchState(HAMMERSTONE, GROWING)
    in 136..138 -> PatchState(WEEDS, GROWING)
    in 139..141 -> PatchState(HAMMERSTONE, DEAD)
    in 142..146 -> PatchState(ASGARNIAN, GROWING)
    in 147..149 -> PatchState(WEEDS, GROWING)
    in 150..153 -> PatchState(ASGARNIAN, DEAD)
    in 154..159 -> PatchState(YANILLIAN, GROWING)
    in 160..162 -> PatchState(WEEDS, GROWING)
    in 163..167 -> PatchState(YANILLIAN, DEAD)
    in 168..174 -> PatchState(KRANDORIAN, GROWING)
    in 175..177 -> PatchState(WEEDS, GROWING)
    in 178..183 -> PatchState(KRANDORIAN, DEAD)
    in 184..191 -> PatchState(WILDBLOOD, GROWING)
    in 192..194 -> PatchState(WEEDS, GROWING)
    in 195..201 -> PatchState(WILDBLOOD, DEAD)
    in 202..205 -> PatchState(BARLEY, GROWING)
    in 206..208 -> PatchState(WEEDS, GROWING)
    in 209..211 -> PatchState(BARLEY, DEAD)
    in 212..216 -> PatchState(JUTE, GROWING)
    in 217..219 -> PatchState(WEEDS, GROWING)
    in 220..223 -> PatchState(JUTE, DEAD)
    in 224..226 -> PatchState(FLAX, GROWING)
    in 227..229 -> PatchState(WEEDS, GROWING)
    in 230..231 -> PatchState(FLAX, DEAD)
    in 232..235 -> PatchState(HEMP, GROWING)
    in 236..238 -> PatchState(WEEDS, GROWING)
    in 239..240 -> PatchState(HEMP, DEAD)
    241 -> PatchState(HEMP, DEAD)
    in 242..246 -> PatchState(COTTON, GROWING)
    in 247..249 -> PatchState(WEEDS, GROWING)
    in 250..253 -> PatchState(COTTON, DEAD)
    in 254..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeTree(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..11 -> PatchState(OAK, GROWING)
    12 -> PatchState(OAK, GROWING)
    in 13..14 -> PatchState(OAK, HARVESTABLE)
    in 15..20 -> PatchState(WILLOW, GROWING)
    21 -> PatchState(WILLOW, GROWING)
    in 22..23 -> PatchState(WILLOW, HARVESTABLE)
    in 24..31 -> PatchState(MAPLE, GROWING)
    32 -> PatchState(MAPLE, GROWING)
    in 33..34 -> PatchState(MAPLE, HARVESTABLE)
    in 35..44 -> PatchState(YEW, GROWING)
    45 -> PatchState(YEW, GROWING)
    in 46..47 -> PatchState(YEW, HARVESTABLE)
    in 48..59 -> PatchState(MAGIC, GROWING)
    60 -> PatchState(MAGIC, GROWING)
    in 61..62 -> PatchState(MAGIC, HARVESTABLE)
    in 63..72 -> PatchState(WEEDS, GROWING)
    in 73..75 -> PatchState(OAK, DISEASED)
    77 -> PatchState(OAK, DISEASED)
    in 78..79 -> PatchState(WEEDS, GROWING)
    in 80..84 -> PatchState(WILLOW, DISEASED)
    86 -> PatchState(WILLOW, DISEASED)
    in 87..88 -> PatchState(WEEDS, GROWING)
    in 89..95 -> PatchState(MAPLE, DISEASED)
    97 -> PatchState(MAPLE, DISEASED)
    in 98..99 -> PatchState(WEEDS, GROWING)
    in 100..108 -> PatchState(YEW, DISEASED)
    110 -> PatchState(YEW, DISEASED)
    in 111..112 -> PatchState(WEEDS, GROWING)
    in 113..123 -> PatchState(MAGIC, DISEASED)
    125 -> PatchState(MAGIC, DISEASED)
    in 126..136 -> PatchState(WEEDS, GROWING)
    in 137..139 -> PatchState(OAK, DEAD)
    141 -> PatchState(OAK, DEAD)
    in 142..143 -> PatchState(WEEDS, GROWING)
    in 144..148 -> PatchState(WILLOW, DEAD)
    150 -> PatchState(WILLOW, DEAD)
    in 151..152 -> PatchState(WEEDS, GROWING)
    in 153..159 -> PatchState(MAPLE, DEAD)
    161 -> PatchState(MAPLE, DEAD)
    in 162..163 -> PatchState(WEEDS, GROWING)
    in 164..172 -> PatchState(YEW, DEAD)
    174 -> PatchState(YEW, DEAD)
    in 175..176 -> PatchState(WEEDS, GROWING)
    in 177..187 -> PatchState(MAGIC, DEAD)
    189 -> PatchState(MAGIC, DEAD)
    in 190..191 -> PatchState(WEEDS, GROWING)
    in 192..197 -> PatchState(WILLOW, HARVESTABLE)
    in 198..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeHardwoodTree(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..14 -> PatchState(TEAK, GROWING)
    15 -> PatchState(TEAK, GROWING)
    in 16..17 -> PatchState(TEAK, HARVESTABLE)
    in 18..23 -> PatchState(TEAK, DISEASED)
    in 24..29 -> PatchState(TEAK, DEAD)
    in 30..37 -> PatchState(MAHOGANY, GROWING)
    38 -> PatchState(MAHOGANY, GROWING)
    in 39..40 -> PatchState(MAHOGANY, HARVESTABLE)
    in 41..47 -> PatchState(MAHOGANY, DISEASED)
    in 48..54 -> PatchState(MAHOGANY, DEAD)
    in 55..62 -> PatchState(CAMPHOR, GROWING)
    63 -> PatchState(CAMPHOR, GROWING)
    in 64..65 -> PatchState(CAMPHOR, HARVESTABLE)
    in 66..72 -> PatchState(CAMPHOR, DISEASED)
    in 73..79 -> PatchState(CAMPHOR, DEAD)
    in 80..87 -> PatchState(IRONWOOD, GROWING)
    88 -> PatchState(IRONWOOD, GROWING)
    in 89..90 -> PatchState(IRONWOOD, HARVESTABLE)
    in 91..97 -> PatchState(IRONWOOD, DISEASED)
    in 98..104 -> PatchState(IRONWOOD, DEAD)
    in 105..113 -> PatchState(ROSEWOOD, GROWING)
    114 -> PatchState(ROSEWOOD, GROWING)
    in 115..116 -> PatchState(ROSEWOOD, HARVESTABLE)
    in 117..124 -> PatchState(ROSEWOOD, DISEASED)
    in 125..132 -> PatchState(ROSEWOOD, DEAD)
    in 133..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeRedwood(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..17 -> PatchState(REDWOOD, GROWING)
    18 -> PatchState(REDWOOD, HARVESTABLE)
    in 19..27 -> PatchState(REDWOOD, DISEASED)
    in 28..36 -> PatchState(REDWOOD, DEAD)
    37 -> PatchState(REDWOOD, GROWING)
    in 41..55 -> PatchState(REDWOOD, HARVESTABLE)
    else -> null
}

private fun decodeSpiritTree(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..19 -> PatchState(SPIRIT_TREE, GROWING)
    20 -> PatchState(SPIRIT_TREE, GROWING)
    in 21..31 -> PatchState(SPIRIT_TREE, DISEASED)
    in 32..43 -> PatchState(SPIRIT_TREE, DEAD)
    44 -> PatchState(SPIRIT_TREE, GROWING)
    in 45..63 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeAnima(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..16 -> PatchState(ATTAS, GROWING)
    in 17..25 -> PatchState(IASOR, GROWING)
    in 26..34 -> PatchState(KRONOS, GROWING)
    in 35..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeBelladonna(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..7 -> PatchState(BELLADONNA, GROWING)
    8 -> PatchState(BELLADONNA, HARVESTABLE)
    in 9..11 -> PatchState(BELLADONNA, DISEASED)
    in 12..14 -> PatchState(BELLADONNA, DEAD)
    in 15..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeCactus(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..14 -> PatchState(CACTUS, GROWING)
    in 15..18 -> PatchState(CACTUS, HARVESTABLE)
    in 19..24 -> PatchState(CACTUS, DISEASED)
    in 25..30 -> PatchState(CACTUS, DEAD)
    31 -> PatchState(CACTUS, GROWING)
    in 32..38 -> PatchState(POTATO_CACTUS, GROWING)
    in 39..45 -> PatchState(POTATO_CACTUS, HARVESTABLE)
    in 46..51 -> PatchState(POTATO_CACTUS, DISEASED)
    in 52..57 -> PatchState(POTATO_CACTUS, DEAD)
    58 -> PatchState(POTATO_CACTUS, GROWING)
    in 59..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeCoral(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..7 -> PatchState(ELKHORN_CORAL, GROWING)
    8 -> PatchState(ELKHORN_CORAL, GROWING)
    in 9..11 -> PatchState(ELKHORN_CORAL, DISEASED)
    in 12..14 -> PatchState(ELKHORN_CORAL, DEAD)
    in 15..18 -> PatchState(PILLAR_CORAL, GROWING)
    19 -> PatchState(PILLAR_CORAL, GROWING)
    in 20..22 -> PatchState(PILLAR_CORAL, DISEASED)
    in 23..25 -> PatchState(PILLAR_CORAL, DEAD)
    in 26..29 -> PatchState(UMBRAL_CORAL, GROWING)
    30 -> PatchState(UMBRAL_CORAL, GROWING)
    in 31..33 -> PatchState(UMBRAL_CORAL, DISEASED)
    in 34..36 -> PatchState(UMBRAL_CORAL, DEAD)
    in 37..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeSeaweed(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..7 -> PatchState(SEAWEED, GROWING)
    in 8..10 -> PatchState(SEAWEED, HARVESTABLE)
    in 11..13 -> PatchState(SEAWEED, DISEASED)
    in 14..16 -> PatchState(SEAWEED, DEAD)
    in 17..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeCalquat(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 4..11 -> PatchState(CALQUAT, GROWING)
    in 12..18 -> PatchState(CALQUAT, HARVESTABLE)
    in 19..25 -> PatchState(CALQUAT, DISEASED)
    in 26..33 -> PatchState(CALQUAT, DEAD)
    34 -> PatchState(CALQUAT, GROWING)
    in 35..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeCelastrus(v: Int): PatchState? = when (v) {
    in 0..3, in 4..7 -> PatchState(WEEDS, GROWING)
    in 8..12 -> PatchState(CELASTRUS, GROWING)
    13 -> PatchState(CELASTRUS, GROWING)
    in 14..16 -> PatchState(CELASTRUS, HARVESTABLE)
    17 -> PatchState(CELASTRUS, HARVESTABLE)
    in 18..22 -> PatchState(CELASTRUS, DISEASED)
    in 23..27 -> PatchState(CELASTRUS, DEAD)
    28 -> PatchState(CELASTRUS, HARVESTABLE)
    in 29..255 -> PatchState(WEEDS, GROWING)
    else -> null
}

private fun decodeGrapes(v: Int): PatchState? = when (v) {
    in 0..1 -> PatchState(WEEDS, GROWING)
    in 2..9 -> PatchState(GRAPE, GROWING)
    10 -> PatchState(GRAPE, GROWING)
    in 11..15 -> PatchState(GRAPE, HARVESTABLE)
    else -> null
}

private fun decodeCrystalTree(v: Int): PatchState? = when (v) {
    in 0..3 -> PatchState(WEEDS, GROWING)
    in 8..13 -> PatchState(CRYSTAL_TREE, GROWING)
    14 -> PatchState(CRYSTAL_TREE, GROWING)
    15 -> PatchState(CRYSTAL_TREE, HARVESTABLE)
    else -> null
}

private fun decodeCompost(v: Int): PatchState? = when (v) {
    0 -> PatchState(EMPTY_COMPOST_BIN, EMPTY)
    in 1..15 -> PatchState(COMPOST, FILLING)
    in 16..30 -> PatchState(COMPOST, HARVESTABLE)
    31, 32 -> PatchState(COMPOST, GROWING)
    in 33..47 -> PatchState(SUPERCOMPOST, FILLING)
    in 48..62 -> PatchState(SUPERCOMPOST, HARVESTABLE)
    94 -> PatchState(COMPOST, GROWING)
    95, 96 -> PatchState(SUPERCOMPOST, GROWING)
    126 -> PatchState(SUPERCOMPOST, GROWING)
    in 129..143 -> PatchState(ROTTEN_TOMATO, FILLING)
    in 144..158 -> PatchState(ROTTEN_TOMATO, HARVESTABLE)
    in 159..160 -> PatchState(ROTTEN_TOMATO, GROWING)
    in 176..190 -> PatchState(ULTRACOMPOST, HARVESTABLE)
    else -> null
}

private fun decodeBigCompost(v: Int): PatchState? = when (v) {
    0 -> PatchState(EMPTY_BIG_COMPOST_BIN, EMPTY)
    in 1..15 -> PatchState(BIG_COMPOST, FILLING)
    in 16..30 -> PatchState(BIG_COMPOST, HARVESTABLE)
    in 33..47 -> PatchState(BIG_SUPERCOMPOST, FILLING)
    in 48..62 -> PatchState(BIG_SUPERCOMPOST, HARVESTABLE)
    in 63..77 -> PatchState(BIG_COMPOST, FILLING)
    in 78..92 -> PatchState(BIG_COMPOST, HARVESTABLE)
    93 -> PatchState(BIG_COMPOST, GROWING)
    in 97..99 -> PatchState(BIG_SUPERCOMPOST, GROWING)
    in 100..114 -> PatchState(BIG_SUPERCOMPOST, HARVESTABLE)
    in 127..128 -> PatchState(BIG_COMPOST, GROWING)
    in 129..143 -> PatchState(BIG_ROTTEN_TOMATO, FILLING)
    in 144..158 -> PatchState(BIG_ROTTEN_TOMATO, HARVESTABLE)
    in 159..160 -> PatchState(BIG_ROTTEN_TOMATO, GROWING)
    in 161..175 -> PatchState(BIG_SUPERCOMPOST, FILLING)
    in 176..205 -> PatchState(BIG_ULTRACOMPOST, HARVESTABLE)
    in 207..221 -> PatchState(BIG_ROTTEN_TOMATO, HARVESTABLE)
    222 -> PatchState(BIG_ROTTEN_TOMATO, GROWING)
    in 223..237 -> PatchState(BIG_ROTTEN_TOMATO, FILLING)
    else -> null
}
