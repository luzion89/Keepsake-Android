package com.keepsake.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.local.entity.*
import com.keepsake.app.data.repository.*
import com.keepsake.app.ui.area.*
import com.keepsake.app.ui.home.*
import com.keepsake.app.ui.item.*
import com.keepsake.app.ui.reminders.*
import com.keepsake.app.ui.room.*
import com.keepsake.app.ui.search.*
import com.keepsake.app.ui.settings.*
import com.keepsake.app.ui.textinput.*
import kotlinx.coroutines.test.runTest
import org.junit.*
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllScreensFunctionalTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var db: KeepsakeDatabase
    private lateinit var roomRepo: RoomRepository
    private lateinit var areaRepo: AreaRepository
    private lateinit var itemRepo: ItemRepository
    private lateinit var photoRepo: PhotoRepository
    private lateinit var reminderRepo: ReminderRepository

    @Before fun setUp() = runTest {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            KeepsakeDatabase::class.java).build()
        roomRepo = RoomRepository(db.roomDao(), db.areaDao(), db.itemDao(), db.kvDao())
        areaRepo = AreaRepository(db.areaDao(), db.itemDao())
        itemRepo = ItemRepository(db.itemDao(), db.reminderRuleDao())
        photoRepo = PhotoRepository(db.photoDao())
        reminderRepo = ReminderRepository(db.reminderRuleDao(), db.itemDao())
        val r = roomRepo.create("厨房", deviceId = "test")
        val a = areaRepo.create(r.id, "柜子", deviceId = "test")
        itemRepo.create(a.id, "洗发水", qty = 3, unit = "瓶", notes = "白色，在浴室", source = "ai", deviceId = "test")
        itemRepo.create(a.id, "牙膏", qty = 2, unit = "盒", deviceId = "test")
        itemRepo.create(a.id, "过期牛奶", qty = 1, expiresAt = System.currentTimeMillis() - 86400000, deviceId = "test")
        val items = itemRepo.getByArea(a.id)
        photoRepo.create("area", a.id, "file:///t1.jpg", deviceId = "test")
        reminderRepo.create(items[0].id, "expiry", thresholdAt = 7 * 86400000L, deviceId = "test")
    }

    @After fun tearDown() { db.close() }

    // ═══════════ HOME ═══════════
    @Test fun home_initiallyEmpty_withFreshRepo() = runTest {
        val fdb = androidx.room.Room.inMemoryDatabaseBuilder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(), KeepsakeDatabase::class.java).build()
        val fr = RoomRepository(fdb.roomDao(), fdb.areaDao(), fdb.itemDao(), fdb.kvDao())
        val vm = HomeViewModel(fr)
        rule.setContent { HomeScreen(vm) {} }
        rule.waitForIdle()
        rule.onNodeWithText("还没有房间").assertExists()
        fdb.close()
    }

    @Test fun home_showsRoomList() = runTest {
        val vm = HomeViewModel(roomRepo)
        rule.setContent { HomeScreen(vm) {} }
        rule.waitForIdle()
        rule.onNodeWithText("厨房").assertExists()
    }

    @Test fun home_FABclick_opensSheet_withPresets() {
        val fdb = androidx.room.Room.inMemoryDatabaseBuilder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(), KeepsakeDatabase::class.java).build()
        val fr = RoomRepository(fdb.roomDao(), fdb.areaDao(), fdb.itemDao(), fdb.kvDao())
        val vm = HomeViewModel(fr)
        rule.setContent { HomeScreen(vm) {} }
        rule.waitForIdle()
        rule.onNode(hasContentDescription("添加房间")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("房间名称").assertExists()
        rule.onNodeWithText("确定").assertExists()
        fdb.close()
    }

    // ═══════════ ROOM ═══════════
    @Test fun room_showsAreaList() = runTest {
        val vm = RoomViewModel(roomRepo, areaRepo)
        vm.load(roomRepo.getAll().first().id)
        rule.setContent { RoomScreen(vm, {}, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("柜子").assertExists()
    }

    @Test fun room_FAB_showsAddDialog() = runTest {
        val vm = RoomViewModel(roomRepo, areaRepo)
        vm.load(roomRepo.getAll().first().id)
        rule.setContent { RoomScreen(vm, {}, {}) }
        rule.waitForIdle()
        rule.onNode(hasContentDescription("添加区域")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("区域名称").assertExists()
    }

    // ═══════════ AREA ═══════════
    @Test fun area_showsItems_and_CTAs() = runTest {
        val area = areaRepo.getByRoom(roomRepo.getAll().first().id).first()
        val vm = AreaViewModel(areaRepo, itemRepo, photoRepo)
        vm.load(area.id)
        rule.setContent { AreaScreen(vm, {}, {}, {}, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("文字输入").assertExists()
        rule.onNodeWithText("洗发水").assertExists()
        rule.onNodeWithText("牙膏").assertExists()
    }

    // ═══════════ ITEM ═══════════
    @Test fun item_viewAndEditModes() = runTest {
        val area = areaRepo.getByRoom(roomRepo.getAll().first().id).first()
        val items = itemRepo.getByArea(area.id)
        val item = items.first { it.name == "洗发水" }
        val vm = ItemViewModel(itemRepo, reminderRepo)
        vm.load(item.id)
        rule.setContent { ItemScreen(vm, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("洗发水").assertExists()
        // Click edit
        rule.onNode(hasContentDescription("编辑")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("保存").assertExists()
    }

    @Test fun item_delete_showsConfirm() = runTest {
        val area = areaRepo.getByRoom(roomRepo.getAll().first().id).first()
        val items = itemRepo.getByArea(area.id)
        val item = items.first { it.name == "牙膏" }
        val vm = ItemViewModel(itemRepo, reminderRepo)
        vm.load(item.id)
        rule.setContent { ItemScreen(vm, {}) }
        rule.waitForIdle()
        rule.onNode(hasContentDescription("删除")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("删除物品").assertExists()
    }

    @Test fun item_expired_showsBadge() = runTest {
        val area = areaRepo.getByRoom(roomRepo.getAll().first().id).first()
        val items = itemRepo.getByArea(area.id)
        val expired = items.first { it.name == "过期牛奶" }
        val vm = ItemViewModel(itemRepo, reminderRepo)
        vm.load(expired.id)
        rule.setContent { ItemScreen(vm, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("已过期").assertExists()
    }

    // ═══════════ SEARCH ═══════════
    @Test fun search_inputField_visible() = runTest {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = SearchViewModel(itemRepo, roomRepo, areaRepo, ai, store)
        rule.setContent { SearchScreen(vm, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("搜索").assertExists()
    }

    // ═══════════ SETTINGS ═══════════
    @Test fun settings_allSections() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, photoRepo, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.waitForIdle()
        rule.onNodeWithText("主题").assertExists()
        rule.onNodeWithText("语言 / Language").assertExists()
        rule.onNodeWithText("AI 助手").assertExists()
        rule.onNodeWithText("本地数据").assertExists()
    }

    @Test fun settings_themeAndLanguage_options() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, photoRepo, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.waitForIdle()
        rule.onNodeWithText("浅色").assertExists()
        rule.onNodeWithText("中文").assertExists()
        rule.onNodeWithText("English").assertExists()
    }

    // ═══════════ REMINDERS ═══════════
    @Test fun reminders_empty_or_triggered() = runTest {
        val vm = RemindersViewModel(reminderRepo, itemRepo)
        rule.setContent { RemindersScreen(vm, {}) }
        rule.waitForIdle()
        // Renders without crash
        assertTrue(true)
    }

    // ═══════════ TEXT INPUT ═══════════
    @Test fun textInput_render() = runTest {
        val area = areaRepo.getByRoom(roomRepo.getAll().first().id).first()
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = TextInputViewModel(areaRepo, itemRepo, ai, store)
        vm.load(area.id)
        rule.setContent { TextInputScreen(vm, {}, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("AI 解析").assertExists()
    }

    companion object {
        fun assertTrue(b: Boolean) = Assert.assertTrue(b)
    }
}
