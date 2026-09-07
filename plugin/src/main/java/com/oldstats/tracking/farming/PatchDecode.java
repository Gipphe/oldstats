package com.oldstats.tracking.farming;

import static com.oldstats.tracking.farming.CropState.DEAD;
import static com.oldstats.tracking.farming.CropState.DISEASED;
import static com.oldstats.tracking.farming.CropState.EMPTY;
import static com.oldstats.tracking.farming.CropState.FILLING;
import static com.oldstats.tracking.farming.CropState.GROWING;
import static com.oldstats.tracking.farming.CropState.HARVESTABLE;
import static com.oldstats.tracking.farming.Produce.ANYHERB;
import static com.oldstats.tracking.farming.Produce.APPLE;
import static com.oldstats.tracking.farming.Produce.ASGARNIAN;
import static com.oldstats.tracking.farming.Produce.ATTAS;
import static com.oldstats.tracking.farming.Produce.AVANTOE;
import static com.oldstats.tracking.farming.Produce.BANANA;
import static com.oldstats.tracking.farming.Produce.BARLEY;
import static com.oldstats.tracking.farming.Produce.BELLADONNA;
import static com.oldstats.tracking.farming.Produce.BIG_COMPOST;
import static com.oldstats.tracking.farming.Produce.BIG_ROTTEN_TOMATO;
import static com.oldstats.tracking.farming.Produce.BIG_SUPERCOMPOST;
import static com.oldstats.tracking.farming.Produce.BIG_ULTRACOMPOST;
import static com.oldstats.tracking.farming.Produce.CABBAGE;
import static com.oldstats.tracking.farming.Produce.CACTUS;
import static com.oldstats.tracking.farming.Produce.CADANTINE;
import static com.oldstats.tracking.farming.Produce.CADAVABERRIES;
import static com.oldstats.tracking.farming.Produce.CALQUAT;
import static com.oldstats.tracking.farming.Produce.CAMPHOR;
import static com.oldstats.tracking.farming.Produce.CELASTRUS;
import static com.oldstats.tracking.farming.Produce.COMPOST;
import static com.oldstats.tracking.farming.Produce.COTTON;
import static com.oldstats.tracking.farming.Produce.CRYSTAL_TREE;
import static com.oldstats.tracking.farming.Produce.CURRY;
import static com.oldstats.tracking.farming.Produce.DRAGONFRUIT;
import static com.oldstats.tracking.farming.Produce.DWARF_WEED;
import static com.oldstats.tracking.farming.Produce.DWELLBERRIES;
import static com.oldstats.tracking.farming.Produce.ELKHORN_CORAL;
import static com.oldstats.tracking.farming.Produce.EMPTY_BIG_COMPOST_BIN;
import static com.oldstats.tracking.farming.Produce.EMPTY_COMPOST_BIN;
import static com.oldstats.tracking.farming.Produce.FLAX;
import static com.oldstats.tracking.farming.Produce.GOUTWEED;
import static com.oldstats.tracking.farming.Produce.GRAPE;
import static com.oldstats.tracking.farming.Produce.GUAM;
import static com.oldstats.tracking.farming.Produce.HAMMERSTONE;
import static com.oldstats.tracking.farming.Produce.HARRALANDER;
import static com.oldstats.tracking.farming.Produce.HEMP;
import static com.oldstats.tracking.farming.Produce.HESPORI;
import static com.oldstats.tracking.farming.Produce.HUASCA;
import static com.oldstats.tracking.farming.Produce.IASOR;
import static com.oldstats.tracking.farming.Produce.IRIT;
import static com.oldstats.tracking.farming.Produce.IRONWOOD;
import static com.oldstats.tracking.farming.Produce.JANGERBERRIES;
import static com.oldstats.tracking.farming.Produce.JUTE;
import static com.oldstats.tracking.farming.Produce.KRANDORIAN;
import static com.oldstats.tracking.farming.Produce.KRONOS;
import static com.oldstats.tracking.farming.Produce.KWUARM;
import static com.oldstats.tracking.farming.Produce.LANTADYME;
import static com.oldstats.tracking.farming.Produce.LIMPWURT;
import static com.oldstats.tracking.farming.Produce.MAGIC;
import static com.oldstats.tracking.farming.Produce.MAHOGANY;
import static com.oldstats.tracking.farming.Produce.MAPLE;
import static com.oldstats.tracking.farming.Produce.MARIGOLD;
import static com.oldstats.tracking.farming.Produce.MARRENTILL;
import static com.oldstats.tracking.farming.Produce.MUSHROOM;
import static com.oldstats.tracking.farming.Produce.NASTURTIUM;
import static com.oldstats.tracking.farming.Produce.OAK;
import static com.oldstats.tracking.farming.Produce.ONION;
import static com.oldstats.tracking.farming.Produce.ORANGE;
import static com.oldstats.tracking.farming.Produce.PALM;
import static com.oldstats.tracking.farming.Produce.PAPAYA;
import static com.oldstats.tracking.farming.Produce.PILLAR_CORAL;
import static com.oldstats.tracking.farming.Produce.PINEAPPLE;
import static com.oldstats.tracking.farming.Produce.POISON_IVY;
import static com.oldstats.tracking.farming.Produce.POTATO;
import static com.oldstats.tracking.farming.Produce.POTATO_CACTUS;
import static com.oldstats.tracking.farming.Produce.RANARR;
import static com.oldstats.tracking.farming.Produce.REDBERRIES;
import static com.oldstats.tracking.farming.Produce.REDWOOD;
import static com.oldstats.tracking.farming.Produce.ROSEMARY;
import static com.oldstats.tracking.farming.Produce.ROSEWOOD;
import static com.oldstats.tracking.farming.Produce.ROTTEN_TOMATO;
import static com.oldstats.tracking.farming.Produce.SCARECROW;
import static com.oldstats.tracking.farming.Produce.SEAWEED;
import static com.oldstats.tracking.farming.Produce.SNAPDRAGON;
import static com.oldstats.tracking.farming.Produce.SNAPE_GRASS;
import static com.oldstats.tracking.farming.Produce.SPIRIT_TREE;
import static com.oldstats.tracking.farming.Produce.STRAWBERRY;
import static com.oldstats.tracking.farming.Produce.SUPERCOMPOST;
import static com.oldstats.tracking.farming.Produce.SWEETCORN;
import static com.oldstats.tracking.farming.Produce.TARROMIN;
import static com.oldstats.tracking.farming.Produce.TEAK;
import static com.oldstats.tracking.farming.Produce.TOADFLAX;
import static com.oldstats.tracking.farming.Produce.TOMATO;
import static com.oldstats.tracking.farming.Produce.TORSTOL;
import static com.oldstats.tracking.farming.Produce.ULTRACOMPOST;
import static com.oldstats.tracking.farming.Produce.UMBRAL_CORAL;
import static com.oldstats.tracking.farming.Produce.WATERMELON;
import static com.oldstats.tracking.farming.Produce.WEEDS;
import static com.oldstats.tracking.farming.Produce.WHITEBERRIES;
import static com.oldstats.tracking.farming.Produce.WHITE_LILY;
import static com.oldstats.tracking.farming.Produce.WILDBLOOD;
import static com.oldstats.tracking.farming.Produce.WILLOW;
import static com.oldstats.tracking.farming.Produce.WOAD;
import static com.oldstats.tracking.farming.Produce.YANILLIAN;
import static com.oldstats.tracking.farming.Produce.YEW;

/**
 * Ported by hand from RuneLite's internal
 * {@code PatchImplementation.forVarbitValue(int)} methods (decompiled, since that
 * class is package-private). Growth-stage sub-values (e.g. "2 of 5 ticks
 * grown") are dropped — only crop identity + {@link CropState} is kept, since this
 * plugin tracks activity, not growth timers.
 */
public final class PatchDecode {
    private PatchDecode() {}

    public static PatchState decodePatchState(PatchImplementation implementation, int value) {
        switch (implementation) {
            case MUSHROOM: return decodeMushroom(value);
            case HESPORI: return decodeHespori(value);
            case ALLOTMENT: return decodeAllotment(value);
            case HERB: return decodeHerb(value);
            case FLOWER: return decodeFlower(value);
            case BUSH: return decodeBush(value);
            case FRUIT_TREE: return decodeFruitTree(value);
            case HOPS: return decodeHops(value);
            case TREE: return decodeTree(value);
            case HARDWOOD_TREE: return decodeHardwoodTree(value);
            case REDWOOD: return decodeRedwood(value);
            case SPIRIT_TREE: return decodeSpiritTree(value);
            case ANIMA: return decodeAnima(value);
            case BELLADONNA: return decodeBelladonna(value);
            case CACTUS: return decodeCactus(value);
            case CORAL: return decodeCoral(value);
            case SEAWEED: return decodeSeaweed(value);
            case CALQUAT: return decodeCalquat(value);
            case CELASTRUS: return decodeCelastrus(value);
            case GRAPES: return decodeGrapes(value);
            case CRYSTAL_TREE: return decodeCrystalTree(value);
            case COMPOST: return decodeCompost(value);
            case BIG_COMPOST: return decodeBigCompost(value);
            default: return null;
        }
    }

    private static PatchState decodeMushroom(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 9) return new PatchState(MUSHROOM, GROWING);
        if (v >= 10 && v <= 15) return new PatchState(MUSHROOM, HARVESTABLE);
        if (v >= 16 && v <= 20) return new PatchState(MUSHROOM, DISEASED);
        if (v >= 21 && v <= 25) return new PatchState(MUSHROOM, DEAD);
        if (v >= 26 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeHespori(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 6) return new PatchState(HESPORI, GROWING);
        if (v >= 7 && v <= 8) return new PatchState(HESPORI, HARVESTABLE);
        if (v == 9) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeAllotment(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 5)) return new PatchState(WEEDS, GROWING);
        if (v >= 6 && v <= 9) return new PatchState(POTATO, GROWING);
        if (v >= 10 && v <= 12) return new PatchState(POTATO, HARVESTABLE);
        if (v >= 13 && v <= 16) return new PatchState(ONION, GROWING);
        if (v >= 17 && v <= 19) return new PatchState(ONION, HARVESTABLE);
        if (v >= 20 && v <= 23) return new PatchState(CABBAGE, GROWING);
        if (v >= 24 && v <= 26) return new PatchState(CABBAGE, HARVESTABLE);
        if (v >= 27 && v <= 30) return new PatchState(TOMATO, GROWING);
        if (v >= 31 && v <= 33) return new PatchState(TOMATO, HARVESTABLE);
        if (v >= 34 && v <= 39) return new PatchState(SWEETCORN, GROWING);
        if (v >= 40 && v <= 42) return new PatchState(SWEETCORN, HARVESTABLE);
        if (v >= 43 && v <= 48) return new PatchState(STRAWBERRY, GROWING);
        if (v >= 49 && v <= 51) return new PatchState(STRAWBERRY, HARVESTABLE);
        if (v >= 52 && v <= 59) return new PatchState(WATERMELON, GROWING);
        if (v >= 60 && v <= 62) return new PatchState(WATERMELON, HARVESTABLE);
        if (v >= 63 && v <= 69) return new PatchState(SNAPE_GRASS, GROWING);
        if (v >= 70 && v <= 73) return new PatchState(POTATO, GROWING);
        if (v >= 74 && v <= 76) return new PatchState(WEEDS, GROWING);
        if (v >= 77 && v <= 80) return new PatchState(ONION, GROWING);
        if (v >= 81 && v <= 83) return new PatchState(WEEDS, GROWING);
        if (v >= 84 && v <= 87) return new PatchState(CABBAGE, GROWING);
        if (v >= 88 && v <= 90) return new PatchState(WEEDS, GROWING);
        if (v >= 91 && v <= 94) return new PatchState(TOMATO, GROWING);
        if (v >= 95 && v <= 97) return new PatchState(WEEDS, GROWING);
        if (v >= 98 && v <= 103) return new PatchState(SWEETCORN, GROWING);
        if (v >= 104 && v <= 106) return new PatchState(WEEDS, GROWING);
        if (v >= 107 && v <= 112) return new PatchState(STRAWBERRY, GROWING);
        if (v >= 113 && v <= 115) return new PatchState(WEEDS, GROWING);
        if (v >= 116 && v <= 123) return new PatchState(WATERMELON, GROWING);
        if (v >= 124 && v <= 127) return new PatchState(WEEDS, GROWING);
        if (v >= 128 && v <= 134) return new PatchState(SNAPE_GRASS, GROWING);
        if (v >= 135 && v <= 137) return new PatchState(POTATO, DISEASED);
        if (v >= 138 && v <= 140) return new PatchState(SNAPE_GRASS, HARVESTABLE);
        if (v == 141) return new PatchState(WEEDS, GROWING);
        if (v >= 142 && v <= 144) return new PatchState(ONION, DISEASED);
        if (v >= 145 && v <= 148) return new PatchState(WEEDS, GROWING);
        if (v >= 149 && v <= 151) return new PatchState(CABBAGE, DISEASED);
        if (v >= 152 && v <= 155) return new PatchState(WEEDS, GROWING);
        if (v >= 156 && v <= 158) return new PatchState(TOMATO, DISEASED);
        if (v >= 159 && v <= 162) return new PatchState(WEEDS, GROWING);
        if (v >= 163 && v <= 167) return new PatchState(SWEETCORN, DISEASED);
        if (v >= 168 && v <= 171) return new PatchState(WEEDS, GROWING);
        if (v >= 172 && v <= 176) return new PatchState(STRAWBERRY, DISEASED);
        if (v >= 177 && v <= 180) return new PatchState(WEEDS, GROWING);
        if (v >= 181 && v <= 187) return new PatchState(WATERMELON, DISEASED);
        if (v >= 188 && v <= 192) return new PatchState(WEEDS, GROWING);
        if (v >= 193 && v <= 195) return new PatchState(SNAPE_GRASS, DEAD);
        if (v >= 196 && v <= 198) return new PatchState(SNAPE_GRASS, DISEASED);
        if (v >= 199 && v <= 201) return new PatchState(POTATO, DEAD);
        if (v >= 202 && v <= 204) return new PatchState(SNAPE_GRASS, DISEASED);
        if (v == 205) return new PatchState(WEEDS, GROWING);
        if (v >= 206 && v <= 208) return new PatchState(ONION, DEAD);
        if (v >= 209 && v <= 211) return new PatchState(SNAPE_GRASS, DEAD);
        if (v == 212) return new PatchState(WEEDS, GROWING);
        if (v >= 213 && v <= 215) return new PatchState(CABBAGE, DEAD);
        if (v >= 216 && v <= 219) return new PatchState(WEEDS, GROWING);
        if (v >= 220 && v <= 222) return new PatchState(TOMATO, DEAD);
        if (v >= 223 && v <= 226) return new PatchState(WEEDS, GROWING);
        if (v >= 227 && v <= 231) return new PatchState(SWEETCORN, DEAD);
        if (v >= 232 && v <= 235) return new PatchState(WEEDS, GROWING);
        if (v >= 236 && v <= 240) return new PatchState(STRAWBERRY, DEAD);
        if (v >= 241 && v <= 244) return new PatchState(WEEDS, GROWING);
        if (v >= 245 && v <= 251) return new PatchState(WATERMELON, DEAD);
        if (v >= 252 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeHerb(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 7) return new PatchState(GUAM, GROWING);
        if (v >= 8 && v <= 10) return new PatchState(GUAM, HARVESTABLE);
        if (v >= 11 && v <= 14) return new PatchState(MARRENTILL, GROWING);
        if (v >= 15 && v <= 17) return new PatchState(MARRENTILL, HARVESTABLE);
        if (v >= 18 && v <= 21) return new PatchState(TARROMIN, GROWING);
        if (v >= 22 && v <= 24) return new PatchState(TARROMIN, HARVESTABLE);
        if (v >= 25 && v <= 28) return new PatchState(HARRALANDER, GROWING);
        if (v >= 29 && v <= 31) return new PatchState(HARRALANDER, HARVESTABLE);
        if (v >= 32 && v <= 35) return new PatchState(RANARR, GROWING);
        if (v >= 36 && v <= 38) return new PatchState(RANARR, HARVESTABLE);
        if (v >= 39 && v <= 42) return new PatchState(TOADFLAX, GROWING);
        if (v >= 43 && v <= 45) return new PatchState(TOADFLAX, HARVESTABLE);
        if (v >= 46 && v <= 49) return new PatchState(IRIT, GROWING);
        if (v >= 50 && v <= 52) return new PatchState(IRIT, HARVESTABLE);
        if (v >= 53 && v <= 56) return new PatchState(AVANTOE, GROWING);
        if (v >= 57 && v <= 59) return new PatchState(AVANTOE, HARVESTABLE);
        if (v >= 60 && v <= 63) return new PatchState(HUASCA, GROWING);
        if (v >= 64 && v <= 66) return new PatchState(HUASCA, HARVESTABLE);
        if (v == 67) return new PatchState(WEEDS, GROWING);
        if (v >= 68 && v <= 71) return new PatchState(KWUARM, GROWING);
        if (v >= 72 && v <= 74) return new PatchState(KWUARM, HARVESTABLE);
        if (v >= 75 && v <= 78) return new PatchState(SNAPDRAGON, GROWING);
        if (v >= 79 && v <= 81) return new PatchState(SNAPDRAGON, HARVESTABLE);
        if (v >= 82 && v <= 85) return new PatchState(CADANTINE, GROWING);
        if (v >= 86 && v <= 88) return new PatchState(CADANTINE, HARVESTABLE);
        if (v >= 89 && v <= 92) return new PatchState(LANTADYME, GROWING);
        if (v >= 93 && v <= 95) return new PatchState(LANTADYME, HARVESTABLE);
        if (v >= 96 && v <= 99) return new PatchState(DWARF_WEED, GROWING);
        if (v >= 100 && v <= 102) return new PatchState(DWARF_WEED, HARVESTABLE);
        if (v >= 103 && v <= 106) return new PatchState(TORSTOL, GROWING);
        if (v >= 107 && v <= 109) return new PatchState(TORSTOL, HARVESTABLE);
        if (v >= 128 && v <= 130) return new PatchState(GUAM, DISEASED);
        if (v >= 131 && v <= 133) return new PatchState(MARRENTILL, DISEASED);
        if (v >= 134 && v <= 136) return new PatchState(TARROMIN, DISEASED);
        if (v >= 137 && v <= 139) return new PatchState(HARRALANDER, DISEASED);
        if (v >= 140 && v <= 142) return new PatchState(RANARR, DISEASED);
        if (v >= 143 && v <= 145) return new PatchState(TOADFLAX, DISEASED);
        if (v >= 146 && v <= 148) return new PatchState(IRIT, DISEASED);
        if (v >= 149 && v <= 151) return new PatchState(AVANTOE, DISEASED);
        if (v >= 152 && v <= 154) return new PatchState(KWUARM, DISEASED);
        if (v >= 155 && v <= 157) return new PatchState(SNAPDRAGON, DISEASED);
        if (v >= 158 && v <= 160) return new PatchState(CADANTINE, DISEASED);
        if (v >= 161 && v <= 163) return new PatchState(LANTADYME, DISEASED);
        if (v >= 164 && v <= 166) return new PatchState(DWARF_WEED, DISEASED);
        if (v >= 167 && v <= 169) return new PatchState(TORSTOL, DISEASED);
        if (v >= 170 && v <= 172) return new PatchState(ANYHERB, DEAD);
        if (v >= 173 && v <= 175) return new PatchState(HUASCA, DISEASED);
        if (v >= 176 && v <= 191) return new PatchState(WEEDS, GROWING);
        if (v >= 192 && v <= 195) return new PatchState(GOUTWEED, GROWING);
        if (v >= 196 && v <= 197) return new PatchState(GOUTWEED, HARVESTABLE);
        if (v >= 198 && v <= 200) return new PatchState(GOUTWEED, DISEASED);
        if (v >= 201 && v <= 203) return new PatchState(GOUTWEED, DEAD);
        if (v >= 204 && v <= 219) return new PatchState(WEEDS, GROWING);
        if (v >= 221 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeFlower(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 11) return new PatchState(MARIGOLD, GROWING);
        if (v == 12) return new PatchState(MARIGOLD, HARVESTABLE);
        if (v >= 13 && v <= 16) return new PatchState(ROSEMARY, GROWING);
        if (v == 17) return new PatchState(ROSEMARY, HARVESTABLE);
        if (v >= 18 && v <= 21) return new PatchState(NASTURTIUM, GROWING);
        if (v == 22) return new PatchState(NASTURTIUM, HARVESTABLE);
        if (v >= 23 && v <= 26) return new PatchState(WOAD, GROWING);
        if (v == 27) return new PatchState(WOAD, HARVESTABLE);
        if (v >= 28 && v <= 31) return new PatchState(LIMPWURT, GROWING);
        if (v == 32) return new PatchState(LIMPWURT, HARVESTABLE);
        if (v >= 33 && v <= 35) return new PatchState(SCARECROW, GROWING);
        if (v == 36) return new PatchState(SCARECROW, GROWING);
        if (v >= 37 && v <= 40) return new PatchState(WHITE_LILY, GROWING);
        if (v == 41) return new PatchState(WHITE_LILY, HARVESTABLE);
        if (v >= 42 && v <= 71) return new PatchState(WEEDS, GROWING);
        if (v >= 72 && v <= 75) return new PatchState(MARIGOLD, GROWING);
        if (v == 76) return new PatchState(WEEDS, GROWING);
        if (v >= 77 && v <= 80) return new PatchState(ROSEMARY, GROWING);
        if (v == 81) return new PatchState(WEEDS, GROWING);
        if (v >= 82 && v <= 85) return new PatchState(NASTURTIUM, GROWING);
        if (v == 86) return new PatchState(WEEDS, GROWING);
        if (v >= 87 && v <= 90) return new PatchState(WOAD, GROWING);
        if (v == 91) return new PatchState(WEEDS, GROWING);
        if (v >= 92 && v <= 95) return new PatchState(LIMPWURT, GROWING);
        if (v >= 96 && v <= 100) return new PatchState(WEEDS, GROWING);
        if (v >= 101 && v <= 104) return new PatchState(WHITE_LILY, GROWING);
        if (v >= 105 && v <= 136) return new PatchState(WEEDS, GROWING);
        if (v >= 137 && v <= 139) return new PatchState(MARIGOLD, DISEASED);
        if (v >= 140 && v <= 141) return new PatchState(WEEDS, GROWING);
        if (v >= 142 && v <= 144) return new PatchState(ROSEMARY, DISEASED);
        if (v >= 145 && v <= 146) return new PatchState(WEEDS, GROWING);
        if (v >= 147 && v <= 149) return new PatchState(NASTURTIUM, DISEASED);
        if (v >= 150 && v <= 151) return new PatchState(WEEDS, GROWING);
        if (v >= 152 && v <= 154) return new PatchState(WOAD, DISEASED);
        if (v >= 155 && v <= 156) return new PatchState(WEEDS, GROWING);
        if (v >= 157 && v <= 159) return new PatchState(LIMPWURT, DISEASED);
        if (v >= 160 && v <= 165) return new PatchState(WEEDS, GROWING);
        if (v >= 166 && v <= 168) return new PatchState(WHITE_LILY, DISEASED);
        if (v >= 169 && v <= 200) return new PatchState(WEEDS, GROWING);
        if (v >= 201 && v <= 204) return new PatchState(MARIGOLD, DEAD);
        if (v == 205) return new PatchState(WEEDS, GROWING);
        if (v >= 206 && v <= 209) return new PatchState(ROSEMARY, DEAD);
        if (v == 210) return new PatchState(WEEDS, GROWING);
        if (v >= 211 && v <= 214) return new PatchState(NASTURTIUM, DEAD);
        if (v == 215) return new PatchState(WEEDS, GROWING);
        if (v >= 216 && v <= 219) return new PatchState(WOAD, DEAD);
        if (v == 220) return new PatchState(WEEDS, GROWING);
        if (v >= 221 && v <= 224) return new PatchState(LIMPWURT, DEAD);
        if (v >= 225 && v <= 229) return new PatchState(WEEDS, GROWING);
        if (v >= 230 && v <= 233) return new PatchState(WHITE_LILY, DEAD);
        if (v >= 234 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeBush(int v) {
        if ((v >= 0 && v <= 3) || v == 4) return new PatchState(WEEDS, GROWING);
        if (v >= 5 && v <= 9) return new PatchState(REDBERRIES, GROWING);
        if (v >= 10 && v <= 14) return new PatchState(REDBERRIES, HARVESTABLE);
        if (v >= 15 && v <= 20) return new PatchState(CADAVABERRIES, GROWING);
        if (v >= 21 && v <= 25) return new PatchState(CADAVABERRIES, HARVESTABLE);
        if (v >= 26 && v <= 32) return new PatchState(DWELLBERRIES, GROWING);
        if (v >= 33 && v <= 37) return new PatchState(DWELLBERRIES, HARVESTABLE);
        if (v >= 38 && v <= 45) return new PatchState(JANGERBERRIES, GROWING);
        if (v >= 46 && v <= 50) return new PatchState(JANGERBERRIES, HARVESTABLE);
        if (v >= 51 && v <= 58) return new PatchState(WHITEBERRIES, GROWING);
        if (v >= 59 && v <= 63) return new PatchState(WHITEBERRIES, HARVESTABLE);
        if (v >= 64 && v <= 69) return new PatchState(WEEDS, GROWING);
        if (v >= 70 && v <= 74) return new PatchState(REDBERRIES, DISEASED);
        if (v >= 75 && v <= 79) return new PatchState(WEEDS, GROWING);
        if (v >= 80 && v <= 85) return new PatchState(CADAVABERRIES, DISEASED);
        if (v >= 86 && v <= 90) return new PatchState(WEEDS, GROWING);
        if (v >= 91 && v <= 97) return new PatchState(DWELLBERRIES, DISEASED);
        if (v >= 98 && v <= 102) return new PatchState(WEEDS, GROWING);
        if (v >= 103 && v <= 110) return new PatchState(JANGERBERRIES, DISEASED);
        if (v >= 111 && v <= 115) return new PatchState(WEEDS, GROWING);
        if (v >= 116 && v <= 123) return new PatchState(WHITEBERRIES, DISEASED);
        if (v >= 124 && v <= 133) return new PatchState(WEEDS, GROWING);
        if (v >= 134 && v <= 138) return new PatchState(REDBERRIES, DEAD);
        if (v >= 139 && v <= 143) return new PatchState(WEEDS, GROWING);
        if (v >= 144 && v <= 149) return new PatchState(CADAVABERRIES, DEAD);
        if (v >= 150 && v <= 154) return new PatchState(WEEDS, GROWING);
        if (v >= 155 && v <= 161) return new PatchState(DWELLBERRIES, DEAD);
        if (v >= 162 && v <= 166) return new PatchState(WEEDS, GROWING);
        if (v >= 167 && v <= 174) return new PatchState(JANGERBERRIES, DEAD);
        if (v >= 175 && v <= 179) return new PatchState(WEEDS, GROWING);
        if (v >= 180 && v <= 187) return new PatchState(WHITEBERRIES, DEAD);
        if (v >= 188 && v <= 196) return new PatchState(WEEDS, GROWING);
        if (v >= 197 && v <= 204) return new PatchState(POISON_IVY, GROWING);
        if (v >= 205 && v <= 209) return new PatchState(POISON_IVY, HARVESTABLE);
        if (v >= 210 && v <= 216) return new PatchState(POISON_IVY, DISEASED);
        if (v >= 217 && v <= 224) return new PatchState(POISON_IVY, DEAD);
        if (v == 225) return new PatchState(POISON_IVY, DISEASED);
        if (v >= 226 && v <= 249) return new PatchState(WEEDS, GROWING);
        if (v == 250) return new PatchState(REDBERRIES, GROWING);
        if (v == 251) return new PatchState(CADAVABERRIES, GROWING);
        if (v == 252) return new PatchState(DWELLBERRIES, GROWING);
        if (v == 253) return new PatchState(JANGERBERRIES, GROWING);
        if (v == 254) return new PatchState(WHITEBERRIES, GROWING);
        if (v == 255) return new PatchState(POISON_IVY, GROWING);
        return null;
    }

    private static PatchState decodeFruitTree(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 13) return new PatchState(APPLE, GROWING);
        if (v >= 14 && v <= 20) return new PatchState(APPLE, HARVESTABLE);
        if (v >= 21 && v <= 26) return new PatchState(APPLE, DISEASED);
        if (v >= 27 && v <= 32) return new PatchState(APPLE, DEAD);
        if (v == 33) return new PatchState(APPLE, HARVESTABLE);
        if (v == 34) return new PatchState(APPLE, GROWING);
        if (v >= 35 && v <= 40) return new PatchState(BANANA, GROWING);
        if (v >= 41 && v <= 47) return new PatchState(BANANA, HARVESTABLE);
        if (v >= 48 && v <= 53) return new PatchState(BANANA, DISEASED);
        if (v >= 54 && v <= 59) return new PatchState(BANANA, DEAD);
        if (v == 60) return new PatchState(BANANA, HARVESTABLE);
        if (v == 61) return new PatchState(BANANA, GROWING);
        if (v >= 62 && v <= 71) return new PatchState(WEEDS, GROWING);
        if (v >= 72 && v <= 77) return new PatchState(ORANGE, GROWING);
        if (v >= 78 && v <= 84) return new PatchState(ORANGE, HARVESTABLE);
        if (v >= 85 && v <= 89) return new PatchState(ORANGE, DISEASED);
        if (v == 90) return new PatchState(ORANGE, DISEASED);
        if (v >= 91 && v <= 96) return new PatchState(ORANGE, DEAD);
        if (v == 97) return new PatchState(ORANGE, HARVESTABLE);
        if (v == 98) return new PatchState(ORANGE, GROWING);
        if (v >= 99 && v <= 104) return new PatchState(CURRY, GROWING);
        if (v >= 105 && v <= 111) return new PatchState(CURRY, HARVESTABLE);
        if (v >= 112 && v <= 117) return new PatchState(CURRY, DISEASED);
        if (v >= 118 && v <= 123) return new PatchState(CURRY, DEAD);
        if (v == 124) return new PatchState(CURRY, HARVESTABLE);
        if (v == 125) return new PatchState(CURRY, GROWING);
        if (v >= 126 && v <= 135) return new PatchState(WEEDS, GROWING);
        if (v >= 136 && v <= 141) return new PatchState(PINEAPPLE, GROWING);
        if (v >= 142 && v <= 148) return new PatchState(PINEAPPLE, HARVESTABLE);
        if (v >= 149 && v <= 154) return new PatchState(PINEAPPLE, DISEASED);
        if (v >= 155 && v <= 160) return new PatchState(PINEAPPLE, DEAD);
        if (v == 161) return new PatchState(PINEAPPLE, HARVESTABLE);
        if (v == 162) return new PatchState(PINEAPPLE, GROWING);
        if (v >= 163 && v <= 168) return new PatchState(PAPAYA, GROWING);
        if (v >= 169 && v <= 175) return new PatchState(PAPAYA, HARVESTABLE);
        if (v >= 176 && v <= 181) return new PatchState(PAPAYA, DISEASED);
        if (v >= 182 && v <= 187) return new PatchState(PAPAYA, DEAD);
        if (v == 188) return new PatchState(PAPAYA, HARVESTABLE);
        if (v == 189) return new PatchState(PAPAYA, GROWING);
        if (v >= 190 && v <= 199) return new PatchState(WEEDS, GROWING);
        if (v >= 200 && v <= 205) return new PatchState(PALM, GROWING);
        if (v >= 206 && v <= 212) return new PatchState(PALM, HARVESTABLE);
        if (v >= 213 && v <= 218) return new PatchState(PALM, DISEASED);
        if (v >= 219 && v <= 224) return new PatchState(PALM, DEAD);
        if (v == 225) return new PatchState(PALM, HARVESTABLE);
        if (v == 226) return new PatchState(PALM, GROWING);
        if (v >= 227 && v <= 232) return new PatchState(DRAGONFRUIT, GROWING);
        if (v >= 233 && v <= 239) return new PatchState(DRAGONFRUIT, HARVESTABLE);
        if (v >= 240 && v <= 245) return new PatchState(DRAGONFRUIT, DISEASED);
        if (v >= 246 && v <= 251) return new PatchState(DRAGONFRUIT, DEAD);
        if (v == 252) return new PatchState(DRAGONFRUIT, HARVESTABLE);
        if (v == 253) return new PatchState(DRAGONFRUIT, GROWING);
        if (v >= 254 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeHops(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 7) return new PatchState(HAMMERSTONE, GROWING);
        if (v >= 8 && v <= 10) return new PatchState(HAMMERSTONE, HARVESTABLE);
        if (v >= 11 && v <= 13) return new PatchState(HAMMERSTONE, DISEASED);
        if (v >= 14 && v <= 18) return new PatchState(ASGARNIAN, GROWING);
        if (v >= 19 && v <= 21) return new PatchState(ASGARNIAN, HARVESTABLE);
        if (v >= 22 && v <= 25) return new PatchState(ASGARNIAN, DISEASED);
        if (v >= 26 && v <= 31) return new PatchState(YANILLIAN, GROWING);
        if (v >= 32 && v <= 34) return new PatchState(YANILLIAN, HARVESTABLE);
        if (v >= 35 && v <= 39) return new PatchState(YANILLIAN, DISEASED);
        if (v >= 40 && v <= 46) return new PatchState(KRANDORIAN, GROWING);
        if (v >= 47 && v <= 49) return new PatchState(KRANDORIAN, HARVESTABLE);
        if (v >= 50 && v <= 55) return new PatchState(KRANDORIAN, DISEASED);
        if (v >= 56 && v <= 63) return new PatchState(WILDBLOOD, GROWING);
        if (v >= 64 && v <= 66) return new PatchState(WILDBLOOD, HARVESTABLE);
        if (v >= 67 && v <= 73) return new PatchState(WILDBLOOD, DISEASED);
        if (v >= 74 && v <= 77) return new PatchState(BARLEY, GROWING);
        if (v >= 78 && v <= 80) return new PatchState(BARLEY, HARVESTABLE);
        if (v >= 81 && v <= 83) return new PatchState(BARLEY, DISEASED);
        if (v >= 84 && v <= 88) return new PatchState(JUTE, GROWING);
        if (v >= 89 && v <= 91) return new PatchState(JUTE, HARVESTABLE);
        if (v >= 92 && v <= 95) return new PatchState(JUTE, DISEASED);
        if (v >= 96 && v <= 98) return new PatchState(FLAX, GROWING);
        if (v >= 99 && v <= 101) return new PatchState(FLAX, HARVESTABLE);
        if (v >= 102 && v <= 103) return new PatchState(FLAX, DISEASED);
        if (v >= 104 && v <= 107) return new PatchState(HEMP, GROWING);
        if (v >= 108 && v <= 110) return new PatchState(HEMP, HARVESTABLE);
        if (v >= 111 && v <= 113) return new PatchState(HEMP, DISEASED);
        if (v >= 114 && v <= 118) return new PatchState(COTTON, GROWING);
        if (v >= 119 && v <= 121) return new PatchState(COTTON, HARVESTABLE);
        if (v >= 122 && v <= 125) return new PatchState(COTTON, DISEASED);
        if (v >= 126 && v <= 131) return new PatchState(WEEDS, GROWING);
        if (v >= 132 && v <= 135) return new PatchState(HAMMERSTONE, GROWING);
        if (v >= 136 && v <= 138) return new PatchState(WEEDS, GROWING);
        if (v >= 139 && v <= 141) return new PatchState(HAMMERSTONE, DEAD);
        if (v >= 142 && v <= 146) return new PatchState(ASGARNIAN, GROWING);
        if (v >= 147 && v <= 149) return new PatchState(WEEDS, GROWING);
        if (v >= 150 && v <= 153) return new PatchState(ASGARNIAN, DEAD);
        if (v >= 154 && v <= 159) return new PatchState(YANILLIAN, GROWING);
        if (v >= 160 && v <= 162) return new PatchState(WEEDS, GROWING);
        if (v >= 163 && v <= 167) return new PatchState(YANILLIAN, DEAD);
        if (v >= 168 && v <= 174) return new PatchState(KRANDORIAN, GROWING);
        if (v >= 175 && v <= 177) return new PatchState(WEEDS, GROWING);
        if (v >= 178 && v <= 183) return new PatchState(KRANDORIAN, DEAD);
        if (v >= 184 && v <= 191) return new PatchState(WILDBLOOD, GROWING);
        if (v >= 192 && v <= 194) return new PatchState(WEEDS, GROWING);
        if (v >= 195 && v <= 201) return new PatchState(WILDBLOOD, DEAD);
        if (v >= 202 && v <= 205) return new PatchState(BARLEY, GROWING);
        if (v >= 206 && v <= 208) return new PatchState(WEEDS, GROWING);
        if (v >= 209 && v <= 211) return new PatchState(BARLEY, DEAD);
        if (v >= 212 && v <= 216) return new PatchState(JUTE, GROWING);
        if (v >= 217 && v <= 219) return new PatchState(WEEDS, GROWING);
        if (v >= 220 && v <= 223) return new PatchState(JUTE, DEAD);
        if (v >= 224 && v <= 226) return new PatchState(FLAX, GROWING);
        if (v >= 227 && v <= 229) return new PatchState(WEEDS, GROWING);
        if (v >= 230 && v <= 231) return new PatchState(FLAX, DEAD);
        if (v >= 232 && v <= 235) return new PatchState(HEMP, GROWING);
        if (v >= 236 && v <= 238) return new PatchState(WEEDS, GROWING);
        if (v >= 239 && v <= 240) return new PatchState(HEMP, DEAD);
        if (v == 241) return new PatchState(HEMP, DEAD);
        if (v >= 242 && v <= 246) return new PatchState(COTTON, GROWING);
        if (v >= 247 && v <= 249) return new PatchState(WEEDS, GROWING);
        if (v >= 250 && v <= 253) return new PatchState(COTTON, DEAD);
        if (v >= 254 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeTree(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 11) return new PatchState(OAK, GROWING);
        if (v == 12) return new PatchState(OAK, GROWING);
        if (v >= 13 && v <= 14) return new PatchState(OAK, HARVESTABLE);
        if (v >= 15 && v <= 20) return new PatchState(WILLOW, GROWING);
        if (v == 21) return new PatchState(WILLOW, GROWING);
        if (v >= 22 && v <= 23) return new PatchState(WILLOW, HARVESTABLE);
        if (v >= 24 && v <= 31) return new PatchState(MAPLE, GROWING);
        if (v == 32) return new PatchState(MAPLE, GROWING);
        if (v >= 33 && v <= 34) return new PatchState(MAPLE, HARVESTABLE);
        if (v >= 35 && v <= 44) return new PatchState(YEW, GROWING);
        if (v == 45) return new PatchState(YEW, GROWING);
        if (v >= 46 && v <= 47) return new PatchState(YEW, HARVESTABLE);
        if (v >= 48 && v <= 59) return new PatchState(MAGIC, GROWING);
        if (v == 60) return new PatchState(MAGIC, GROWING);
        if (v >= 61 && v <= 62) return new PatchState(MAGIC, HARVESTABLE);
        if (v >= 63 && v <= 72) return new PatchState(WEEDS, GROWING);
        if (v >= 73 && v <= 75) return new PatchState(OAK, DISEASED);
        if (v == 77) return new PatchState(OAK, DISEASED);
        if (v >= 78 && v <= 79) return new PatchState(WEEDS, GROWING);
        if (v >= 80 && v <= 84) return new PatchState(WILLOW, DISEASED);
        if (v == 86) return new PatchState(WILLOW, DISEASED);
        if (v >= 87 && v <= 88) return new PatchState(WEEDS, GROWING);
        if (v >= 89 && v <= 95) return new PatchState(MAPLE, DISEASED);
        if (v == 97) return new PatchState(MAPLE, DISEASED);
        if (v >= 98 && v <= 99) return new PatchState(WEEDS, GROWING);
        if (v >= 100 && v <= 108) return new PatchState(YEW, DISEASED);
        if (v == 110) return new PatchState(YEW, DISEASED);
        if (v >= 111 && v <= 112) return new PatchState(WEEDS, GROWING);
        if (v >= 113 && v <= 123) return new PatchState(MAGIC, DISEASED);
        if (v == 125) return new PatchState(MAGIC, DISEASED);
        if (v >= 126 && v <= 136) return new PatchState(WEEDS, GROWING);
        if (v >= 137 && v <= 139) return new PatchState(OAK, DEAD);
        if (v == 141) return new PatchState(OAK, DEAD);
        if (v >= 142 && v <= 143) return new PatchState(WEEDS, GROWING);
        if (v >= 144 && v <= 148) return new PatchState(WILLOW, DEAD);
        if (v == 150) return new PatchState(WILLOW, DEAD);
        if (v >= 151 && v <= 152) return new PatchState(WEEDS, GROWING);
        if (v >= 153 && v <= 159) return new PatchState(MAPLE, DEAD);
        if (v == 161) return new PatchState(MAPLE, DEAD);
        if (v >= 162 && v <= 163) return new PatchState(WEEDS, GROWING);
        if (v >= 164 && v <= 172) return new PatchState(YEW, DEAD);
        if (v == 174) return new PatchState(YEW, DEAD);
        if (v >= 175 && v <= 176) return new PatchState(WEEDS, GROWING);
        if (v >= 177 && v <= 187) return new PatchState(MAGIC, DEAD);
        if (v == 189) return new PatchState(MAGIC, DEAD);
        if (v >= 190 && v <= 191) return new PatchState(WEEDS, GROWING);
        if (v >= 192 && v <= 197) return new PatchState(WILLOW, HARVESTABLE);
        if (v >= 198 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeHardwoodTree(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 14) return new PatchState(TEAK, GROWING);
        if (v == 15) return new PatchState(TEAK, GROWING);
        if (v >= 16 && v <= 17) return new PatchState(TEAK, HARVESTABLE);
        if (v >= 18 && v <= 23) return new PatchState(TEAK, DISEASED);
        if (v >= 24 && v <= 29) return new PatchState(TEAK, DEAD);
        if (v >= 30 && v <= 37) return new PatchState(MAHOGANY, GROWING);
        if (v == 38) return new PatchState(MAHOGANY, GROWING);
        if (v >= 39 && v <= 40) return new PatchState(MAHOGANY, HARVESTABLE);
        if (v >= 41 && v <= 47) return new PatchState(MAHOGANY, DISEASED);
        if (v >= 48 && v <= 54) return new PatchState(MAHOGANY, DEAD);
        if (v >= 55 && v <= 62) return new PatchState(CAMPHOR, GROWING);
        if (v == 63) return new PatchState(CAMPHOR, GROWING);
        if (v >= 64 && v <= 65) return new PatchState(CAMPHOR, HARVESTABLE);
        if (v >= 66 && v <= 72) return new PatchState(CAMPHOR, DISEASED);
        if (v >= 73 && v <= 79) return new PatchState(CAMPHOR, DEAD);
        if (v >= 80 && v <= 87) return new PatchState(IRONWOOD, GROWING);
        if (v == 88) return new PatchState(IRONWOOD, GROWING);
        if (v >= 89 && v <= 90) return new PatchState(IRONWOOD, HARVESTABLE);
        if (v >= 91 && v <= 97) return new PatchState(IRONWOOD, DISEASED);
        if (v >= 98 && v <= 104) return new PatchState(IRONWOOD, DEAD);
        if (v >= 105 && v <= 113) return new PatchState(ROSEWOOD, GROWING);
        if (v == 114) return new PatchState(ROSEWOOD, GROWING);
        if (v >= 115 && v <= 116) return new PatchState(ROSEWOOD, HARVESTABLE);
        if (v >= 117 && v <= 124) return new PatchState(ROSEWOOD, DISEASED);
        if (v >= 125 && v <= 132) return new PatchState(ROSEWOOD, DEAD);
        if (v >= 133 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeRedwood(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 17) return new PatchState(REDWOOD, GROWING);
        if (v == 18) return new PatchState(REDWOOD, HARVESTABLE);
        if (v >= 19 && v <= 27) return new PatchState(REDWOOD, DISEASED);
        if (v >= 28 && v <= 36) return new PatchState(REDWOOD, DEAD);
        if (v == 37) return new PatchState(REDWOOD, GROWING);
        if (v >= 41 && v <= 55) return new PatchState(REDWOOD, HARVESTABLE);
        return null;
    }

    private static PatchState decodeSpiritTree(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 19) return new PatchState(SPIRIT_TREE, GROWING);
        if (v == 20) return new PatchState(SPIRIT_TREE, GROWING);
        if (v >= 21 && v <= 31) return new PatchState(SPIRIT_TREE, DISEASED);
        if (v >= 32 && v <= 43) return new PatchState(SPIRIT_TREE, DEAD);
        if (v == 44) return new PatchState(SPIRIT_TREE, GROWING);
        if (v >= 45 && v <= 63) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeAnima(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 16) return new PatchState(ATTAS, GROWING);
        if (v >= 17 && v <= 25) return new PatchState(IASOR, GROWING);
        if (v >= 26 && v <= 34) return new PatchState(KRONOS, GROWING);
        if (v >= 35 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeBelladonna(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 7) return new PatchState(BELLADONNA, GROWING);
        if (v == 8) return new PatchState(BELLADONNA, HARVESTABLE);
        if (v >= 9 && v <= 11) return new PatchState(BELLADONNA, DISEASED);
        if (v >= 12 && v <= 14) return new PatchState(BELLADONNA, DEAD);
        if (v >= 15 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeCactus(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 14) return new PatchState(CACTUS, GROWING);
        if (v >= 15 && v <= 18) return new PatchState(CACTUS, HARVESTABLE);
        if (v >= 19 && v <= 24) return new PatchState(CACTUS, DISEASED);
        if (v >= 25 && v <= 30) return new PatchState(CACTUS, DEAD);
        if (v == 31) return new PatchState(CACTUS, GROWING);
        if (v >= 32 && v <= 38) return new PatchState(POTATO_CACTUS, GROWING);
        if (v >= 39 && v <= 45) return new PatchState(POTATO_CACTUS, HARVESTABLE);
        if (v >= 46 && v <= 51) return new PatchState(POTATO_CACTUS, DISEASED);
        if (v >= 52 && v <= 57) return new PatchState(POTATO_CACTUS, DEAD);
        if (v == 58) return new PatchState(POTATO_CACTUS, GROWING);
        if (v >= 59 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeCoral(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 7) return new PatchState(ELKHORN_CORAL, GROWING);
        if (v == 8) return new PatchState(ELKHORN_CORAL, GROWING);
        if (v >= 9 && v <= 11) return new PatchState(ELKHORN_CORAL, DISEASED);
        if (v >= 12 && v <= 14) return new PatchState(ELKHORN_CORAL, DEAD);
        if (v >= 15 && v <= 18) return new PatchState(PILLAR_CORAL, GROWING);
        if (v == 19) return new PatchState(PILLAR_CORAL, GROWING);
        if (v >= 20 && v <= 22) return new PatchState(PILLAR_CORAL, DISEASED);
        if (v >= 23 && v <= 25) return new PatchState(PILLAR_CORAL, DEAD);
        if (v >= 26 && v <= 29) return new PatchState(UMBRAL_CORAL, GROWING);
        if (v == 30) return new PatchState(UMBRAL_CORAL, GROWING);
        if (v >= 31 && v <= 33) return new PatchState(UMBRAL_CORAL, DISEASED);
        if (v >= 34 && v <= 36) return new PatchState(UMBRAL_CORAL, DEAD);
        if (v >= 37 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeSeaweed(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 7) return new PatchState(SEAWEED, GROWING);
        if (v >= 8 && v <= 10) return new PatchState(SEAWEED, HARVESTABLE);
        if (v >= 11 && v <= 13) return new PatchState(SEAWEED, DISEASED);
        if (v >= 14 && v <= 16) return new PatchState(SEAWEED, DEAD);
        if (v >= 17 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeCalquat(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 4 && v <= 11) return new PatchState(CALQUAT, GROWING);
        if (v >= 12 && v <= 18) return new PatchState(CALQUAT, HARVESTABLE);
        if (v >= 19 && v <= 25) return new PatchState(CALQUAT, DISEASED);
        if (v >= 26 && v <= 33) return new PatchState(CALQUAT, DEAD);
        if (v == 34) return new PatchState(CALQUAT, GROWING);
        if (v >= 35 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeCelastrus(int v) {
        if ((v >= 0 && v <= 3) || (v >= 4 && v <= 7)) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 12) return new PatchState(CELASTRUS, GROWING);
        if (v == 13) return new PatchState(CELASTRUS, GROWING);
        if (v >= 14 && v <= 16) return new PatchState(CELASTRUS, HARVESTABLE);
        if (v == 17) return new PatchState(CELASTRUS, HARVESTABLE);
        if (v >= 18 && v <= 22) return new PatchState(CELASTRUS, DISEASED);
        if (v >= 23 && v <= 27) return new PatchState(CELASTRUS, DEAD);
        if (v == 28) return new PatchState(CELASTRUS, HARVESTABLE);
        if (v >= 29 && v <= 255) return new PatchState(WEEDS, GROWING);
        return null;
    }

    private static PatchState decodeGrapes(int v) {
        if (v >= 0 && v <= 1) return new PatchState(WEEDS, GROWING);
        if (v >= 2 && v <= 9) return new PatchState(GRAPE, GROWING);
        if (v == 10) return new PatchState(GRAPE, GROWING);
        if (v >= 11 && v <= 15) return new PatchState(GRAPE, HARVESTABLE);
        return null;
    }

    private static PatchState decodeCrystalTree(int v) {
        if (v >= 0 && v <= 3) return new PatchState(WEEDS, GROWING);
        if (v >= 8 && v <= 13) return new PatchState(CRYSTAL_TREE, GROWING);
        if (v == 14) return new PatchState(CRYSTAL_TREE, GROWING);
        if (v == 15) return new PatchState(CRYSTAL_TREE, HARVESTABLE);
        return null;
    }

    private static PatchState decodeCompost(int v) {
        if (v == 0) return new PatchState(EMPTY_COMPOST_BIN, EMPTY);
        if (v >= 1 && v <= 15) return new PatchState(COMPOST, FILLING);
        if (v >= 16 && v <= 30) return new PatchState(COMPOST, HARVESTABLE);
        if (v == 31 || v == 32) return new PatchState(COMPOST, GROWING);
        if (v >= 33 && v <= 47) return new PatchState(SUPERCOMPOST, FILLING);
        if (v >= 48 && v <= 62) return new PatchState(SUPERCOMPOST, HARVESTABLE);
        if (v == 94) return new PatchState(COMPOST, GROWING);
        if (v == 95 || v == 96) return new PatchState(SUPERCOMPOST, GROWING);
        if (v == 126) return new PatchState(SUPERCOMPOST, GROWING);
        if (v >= 129 && v <= 143) return new PatchState(ROTTEN_TOMATO, FILLING);
        if (v >= 144 && v <= 158) return new PatchState(ROTTEN_TOMATO, HARVESTABLE);
        if (v >= 159 && v <= 160) return new PatchState(ROTTEN_TOMATO, GROWING);
        if (v >= 176 && v <= 190) return new PatchState(ULTRACOMPOST, HARVESTABLE);
        return null;
    }

    private static PatchState decodeBigCompost(int v) {
        if (v == 0) return new PatchState(EMPTY_BIG_COMPOST_BIN, EMPTY);
        if (v >= 1 && v <= 15) return new PatchState(BIG_COMPOST, FILLING);
        if (v >= 16 && v <= 30) return new PatchState(BIG_COMPOST, HARVESTABLE);
        if (v >= 33 && v <= 47) return new PatchState(BIG_SUPERCOMPOST, FILLING);
        if (v >= 48 && v <= 62) return new PatchState(BIG_SUPERCOMPOST, HARVESTABLE);
        if (v >= 63 && v <= 77) return new PatchState(BIG_COMPOST, FILLING);
        if (v >= 78 && v <= 92) return new PatchState(BIG_COMPOST, HARVESTABLE);
        if (v == 93) return new PatchState(BIG_COMPOST, GROWING);
        if (v >= 97 && v <= 99) return new PatchState(BIG_SUPERCOMPOST, GROWING);
        if (v >= 100 && v <= 114) return new PatchState(BIG_SUPERCOMPOST, HARVESTABLE);
        if (v >= 127 && v <= 128) return new PatchState(BIG_COMPOST, GROWING);
        if (v >= 129 && v <= 143) return new PatchState(BIG_ROTTEN_TOMATO, FILLING);
        if (v >= 144 && v <= 158) return new PatchState(BIG_ROTTEN_TOMATO, HARVESTABLE);
        if (v >= 159 && v <= 160) return new PatchState(BIG_ROTTEN_TOMATO, GROWING);
        if (v >= 161 && v <= 175) return new PatchState(BIG_SUPERCOMPOST, FILLING);
        if (v >= 176 && v <= 205) return new PatchState(BIG_ULTRACOMPOST, HARVESTABLE);
        if (v >= 207 && v <= 221) return new PatchState(BIG_ROTTEN_TOMATO, HARVESTABLE);
        if (v == 222) return new PatchState(BIG_ROTTEN_TOMATO, GROWING);
        if (v >= 223 && v <= 237) return new PatchState(BIG_ROTTEN_TOMATO, FILLING);
        return null;
    }
}
