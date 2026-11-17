package com.v2ray.ang.dto

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TrafficSnapshotTest {
    @Test
    fun `first sample measures bytes and transfer rates from the Xray interval`() {
        assertEquals(
            TrafficSnapshot(200L, 400L, 600L, 1200L, 3000L),
            trafficSnapshot(null, 600L, 1200L, 3000L, 3000L)
        )
    }

    @Test
    fun `subsequent samples accumulate totals without accumulating instantaneous speed`() {
        val first = trafficSnapshot(null, 600L, 1200L, 3000L, 3000L)
        assertEquals(
            TrafficSnapshot(100L, 200L, 700L, 1400L, 4000L),
            trafficSnapshot(first, 100L, 200L, 1000L, 4000L)
        )
    }

    @Test
    fun `invalid interval and negative native counters cannot create negative values`() {
        assertEquals(
            TrafficSnapshot(0L, 0L, 0L, 0L, 0L),
            trafficSnapshot(null, -10L, -20L, 0L, -1L)
        )
    }
}
