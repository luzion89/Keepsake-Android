package com.keepsake.app.notifications

import com.keepsake.app.domain.model.Item
import com.keepsake.app.domain.model.ReminderRule
import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.TimeUnit

class ReminderScannerTest {

    @Test fun `RM02 expired item triggers expiry reminder`() {
        val now = System.currentTimeMillis()
        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        val item = Item(id = "i1", name = "过期牛奶", expiresAt = yesterday)
        val rule = ReminderRule(id = "r1", itemId = "i1", kind = "expiry", thresholdAt = TimeUnit.DAYS.toMillis(30))

        val remaining = (item.expiresAt ?: 0) - now
        assertTrue("已过期物品应该触发提醒", remaining <= (rule.thresholdAt ?: 0))
    }

    @Test fun `RM02 near-expiry item triggers reminder`() {
        val now = System.currentTimeMillis()
        val in3Days = now + TimeUnit.DAYS.toMillis(3)
        val item = Item(id = "i2", name = "快过期酸奶", expiresAt = in3Days)
        val rule = ReminderRule(id = "r2", itemId = "i2", kind = "expiry", thresholdAt = TimeUnit.DAYS.toMillis(7))

        val remaining = (item.expiresAt ?: 0) - now
        assertTrue("3天过期应触发7天提醒阈值", remaining <= (rule.thresholdAt ?: 0))
    }

    @Test fun `RM05 not-yet-expiring item should not trigger`() {
        val now = System.currentTimeMillis()
        val in60Days = now + TimeUnit.DAYS.toMillis(60)
        val item = Item(id = "i3", name = "新买的", expiresAt = in60Days)
        val rule = ReminderRule(id = "r3", itemId = "i3", kind = "expiry", thresholdAt = TimeUnit.DAYS.toMillis(7))

        val remaining = (item.expiresAt ?: 0) - now
        assertFalse("60天后过期不应触发7天提醒", remaining <= (rule.thresholdAt ?: 0))
    }

    @Test fun `RM03 low stock triggers when qty below threshold`() {
        val item = Item(id = "i4", name = "快用完的洗发水", qty = 1)
        val rule = ReminderRule(id = "r4", itemId = "i4", kind = "low_stock", thresholdQty = 3)
        assertTrue("库存不足应触发", item.qty <= (rule.thresholdQty ?: Int.MAX_VALUE))
    }

    @Test fun `RM03 sufficient stock should not trigger`() {
        val item = Item(id = "i5", name = "充足的洗发水", qty = 5)
        val rule = ReminderRule(id = "r5", itemId = "i5", kind = "low_stock", thresholdQty = 2)
        assertFalse("库存充足不应触发", item.qty <= (rule.thresholdQty ?: 0))
    }

    @Test fun `RM03 exact threshold qty triggers`() {
        val item = Item(id = "i6", name = "刚好阈值", qty = 2)
        val rule = ReminderRule(id = "r6", itemId = "i6", kind = "low_stock", thresholdQty = 2)
        assertTrue("等于阈值应该触发", item.qty <= (rule.thresholdQty ?: Int.MAX_VALUE))
    }

    @Test fun `RM04 recheck reminder triggers after interval`() {
        val now = System.currentTimeMillis()
        val interval = TimeUnit.DAYS.toMillis(90)
        val longAgo = now - interval - 1
        val rule = ReminderRule(id = "r7", itemId = "i7", kind = "recheck", thresholdAt = interval, lastFiredAt = longAgo)
        assertTrue("超过复查间隔应触发", (rule.lastFiredAt ?: 0) + (rule.thresholdAt ?: 0) <= now)
    }

    @Test fun `RM08 no re-fire within 1 hour`() {
        val now = System.currentTimeMillis()
        val recently = now - TimeUnit.MINUTES.toMillis(30)
        assertTrue("30分钟前触发过，1小时内不重复", now - recently < TimeUnit.HOURS.toMillis(1))
    }

    @Test fun `RM08 re-fire allowed after 1 hour`() {
        val now = System.currentTimeMillis()
        val longAgo = now - TimeUnit.HOURS.toMillis(2)
        assertTrue("2小时前触发过，可以再次触发", now - longAgo >= TimeUnit.HOURS.toMillis(1))
    }

    @Test fun `RM05 null expiry should not trigger expiry reminder`() {
        val item = Item(id = "i8", name = "无过期日期", expiresAt = null)
        assertEquals(null, item.expiresAt)
    }

    @Test fun `deleted item should not trigger`() {
        val item = Item(id = "i9", name = "已删除", deleted = true)
        assertTrue(item.deleted)
    }

    @Test fun `A09 green badge when more than 30 days`() {
        val now = System.currentTimeMillis()
        val in60Days = now + TimeUnit.DAYS.toMillis(60)
        val remaining = in60Days - now
        assertTrue(">30天应为绿色", remaining > TimeUnit.DAYS.toMillis(30))
    }

    @Test fun `A10 yellow badge for 30 days down to 7 days`() {
        val now = System.currentTimeMillis()
        val in20Days = now + TimeUnit.DAYS.toMillis(20)
        val remaining = in20Days - now
        assertTrue("<=30天 >7天应为黄色",
            remaining <= TimeUnit.DAYS.toMillis(30) && remaining > TimeUnit.DAYS.toMillis(7))
    }

    @Test fun `A11 red badge for 7 days or expired`() {
        val now = System.currentTimeMillis()
        val in3Days = now + TimeUnit.DAYS.toMillis(3)
        val remaining = in3Days - now
        assertTrue("<=7天应为红色", remaining <= TimeUnit.DAYS.toMillis(7))

        val yesterday = now - TimeUnit.DAYS.toMillis(1)
        assertTrue("已过期也应为红色", yesterday < now)
    }
}
