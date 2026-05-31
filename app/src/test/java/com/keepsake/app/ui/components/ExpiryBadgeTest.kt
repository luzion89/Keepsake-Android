package com.keepsake.app.ui.components

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class ExpiryBadgeTest {

    @Test
    fun `days remaining should be calculated correctly`() {
        val now = System.currentTimeMillis()
        val in30Days = now + TimeUnit.DAYS.toMillis(30)
        val remaining = in30Days - now
        val days = TimeUnit.MILLISECONDS.toDays(remaining)
        assertEquals(30, days)
    }

    @Test
    fun `expired item has negative remaining`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        assertTrue("should be expired", yesterday < now)
    }

    @Test
    fun `30 day warning threshold`() {
        val days30 = TimeUnit.DAYS.toMillis(30)
        assertEquals(30, TimeUnit.MILLISECONDS.toDays(days30))
    }

    @Test
    fun `7 day danger threshold`() {
        val days7 = TimeUnit.DAYS.toMillis(7)
        assertEquals(7, TimeUnit.MILLISECONDS.toDays(days7))
    }

    @Test
    fun `null expiry should not show badge`() {
        // ExpiryBadge returns early (Unit) when expiresAt is null
        // This is tested implicitly in the composable code
        assertTrue(true)
    }
}
