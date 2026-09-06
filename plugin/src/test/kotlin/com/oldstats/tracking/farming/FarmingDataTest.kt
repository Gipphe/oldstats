package com.oldstats.tracking.farming

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FarmingDataTest {

    @Test
    fun `every region has at least one patch`() {
        for (region in FarmingWorldData.regions) {
            assertTrue("${region.name} has no patches", region.patches.isNotEmpty())
        }
    }

    @Test
    fun `catherby region resolves and has the expected patch set`() {
        val candidates = FarmingWorldData.regionsById.getValue(11062)
        val region = candidates.single()
        assertEquals("Catherby", region.name)
        val implementations = region.patches.map { it.implementation }.toSet()
        assertEquals(
            setOf(PatchImplementation.ALLOTMENT, PatchImplementation.FLOWER, PatchImplementation.HERB, PatchImplementation.COMPOST),
            implementations,
        )
    }

    @Test
    fun `catherby north and south allotment disambiguation uses bounds check`() {
        val candidates = FarmingWorldData.regionsById.getValue(11317)
        assertEquals(2, candidates.size)

        // Inside the allotment/herb/flower area (south of y=3440 within the x>=2816 band)
        val allotmentRegion = candidates.first { it.boundsCheck?.invoke(2820, 3441, 0) == true }
        assertTrue(allotmentRegion.patches.any { it.implementation == PatchImplementation.HERB })

        // The fruit tree patch sits north of that line
        val fruitTreeRegion = candidates.first { it.boundsCheck?.invoke(2820, 3300, 0) == true }
        assertTrue(fruitTreeRegion.patches.any { it.implementation == PatchImplementation.FRUIT_TREE })
    }

    @Test
    fun `falador allotment only resolves south of the y bounds check`() {
        val candidates = FarmingWorldData.regionsById.getValue(12083)
        val region = candidates.single { it.patches.any { p -> p.implementation == PatchImplementation.ALLOTMENT } }
        assertEquals(true, region.boundsCheck?.invoke(0, 3272, 0))
        assertEquals(false, region.boundsCheck?.invoke(0, 3271, 0))
    }

    @Test
    fun `farming guild has thirteen distinct patches`() {
        val region = FarmingWorldData.regionsById.getValue(4922).single()
        assertEquals(13, region.patches.size)
    }

    @Test
    fun `herb patch decodes ranarr growing and harvestable correctly`() {
        // Ranarr: growing 32-35, harvestable 36-38 (see PatchDecode.kt / real HERB table)
        assertEquals(PatchState(Produce.RANARR, CropState.GROWING), decodePatchState(PatchImplementation.HERB, 33))
        assertEquals(PatchState(Produce.RANARR, CropState.HARVESTABLE), decodePatchState(PatchImplementation.HERB, 37))
        assertEquals(PatchState(Produce.RANARR, CropState.DISEASED), decodePatchState(PatchImplementation.HERB, 141))
    }

    @Test
    fun `allotment patch decodes potato lifecycle correctly`() {
        assertEquals(PatchState(Produce.WEEDS, CropState.GROWING), decodePatchState(PatchImplementation.ALLOTMENT, 0))
        assertEquals(PatchState(Produce.POTATO, CropState.GROWING), decodePatchState(PatchImplementation.ALLOTMENT, 7))
        assertEquals(PatchState(Produce.POTATO, CropState.HARVESTABLE), decodePatchState(PatchImplementation.ALLOTMENT, 11))
        assertEquals(PatchState(Produce.POTATO, CropState.DISEASED), decodePatchState(PatchImplementation.ALLOTMENT, 136))
        assertEquals(PatchState(Produce.POTATO, CropState.DEAD), decodePatchState(PatchImplementation.ALLOTMENT, 200))
    }

    @Test
    fun `compost bin decodes empty and filling correctly`() {
        assertEquals(PatchState(Produce.EMPTY_COMPOST_BIN, CropState.EMPTY), decodePatchState(PatchImplementation.COMPOST, 0))
        assertEquals(PatchState(Produce.COMPOST, CropState.FILLING), decodePatchState(PatchImplementation.COMPOST, 5))
        assertEquals(PatchState(Produce.COMPOST, CropState.HARVESTABLE), decodePatchState(PatchImplementation.COMPOST, 20))
    }

    @Test
    fun `every patch varbit referenced by a region has decode coverage for at least value 0`() {
        for (region in FarmingWorldData.regions) {
            for (patch in region.patches) {
                assertNotNull(
                    "${region.name} ${patch.name} (${patch.implementation}) has no decode for value 0",
                    decodePatchState(patch.implementation, 0),
                )
            }
        }
    }

    @Test
    fun `unknown region resolves to nothing`() {
        assertNull(FarmingWorldData.regionsById[999999])
    }
}
