package com.oldstats.tracking

/**
 * Best-effort set of boss/raid-boss NPC names used to flag kills as boss
 * kills. Not exhaustive — extend as needed.
 */
object BossList {
    private val NAMES: Set<String> = setOf(
        "Abyssal Sire", "Alchemical Hydra", "Amoxliatl", "Aroxsomeone",
        "Barrows brothers", "Dharok the Wretched", "Ahrim the Blighted",
        "Guthan the Infested", "Karil the Tainted", "Torag the Corrupted",
        "Verac the Defiled",
        "Callisto", "Artio", "Cerberus", "Chaos Elemental", "Chaos Fanatic",
        "Commander Zilyana", "Corporeal Beast", "Crazy Archaeologist",
        "Dagannoth Prime", "Dagannoth Rex", "Dagannoth Supreme",
        "Duke Sucellus", "General Graardor", "Giant Mole",
        "Grotesque Guardians", "Dawn", "Dusk", "Kalphite Queen",
        "Kree'arra", "K'ril Tsutsaroth", "Kraken", "Nex", "Obor", "Bryophyta",
        "Phantom Muspah", "Sarachnis", "Scorpia", "Scurrius", "Skotizo",
        "Tempoross", "The Hueycoatl", "The Leviathan", "The Nightmare",
        "Phosani's Nightmare", "The Whisperer", "Thermonuclear Smoke Devil",
        "TzKal-Zuk", "TzTok-Jad", "Vardorvis", "Venenatis", "Spindel",
        "Vet'ion", "Calvar'ion", "Vorkath", "Yama", "Zalcano", "Zulrah",
        "Great Olm", "Verzik Vitur", "Sotetseg", "Vespula",
        "The Ambassador",
    )

    fun isBoss(npcName: String): Boolean = NAMES.contains(npcName)
}
