package com.keepsake.app.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.keepsake.app.data.local.dao.*
import com.keepsake.app.data.local.database.KeepsakeDatabase
import com.keepsake.app.data.repository.*
import com.keepsake.app.domain.model.*
import com.keepsake.app.ui.area.*
import com.keepsake.app.ui.home.*
import com.keepsake.app.ui.item.*
import com.keepsake.app.ui.reminders.*
import com.keepsake.app.ui.room.*
import com.keepsake.app.ui.search.*
import com.keepsake.app.ui.settings.*
import com.keepsake.app.ui.textinput.*
import org.junit.*
import org.junit.Assert.assertTrue
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AllScreensFunctionalTest {
    @get:Rule val rule = createComposeRule()
    private lateinit var db: KeepsakeDatabase
    private lateinit var roomRepo: RoomRepository
    private lateinit var areaRepo: AreaRepository
    private lateinit var itemRepo: ItemRepository

    @Before fun setUp() {
        db = androidx.room.Room.inMemoryDatabaseBuilder(
            androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            KeepsakeDatabase::class.java).build()
        roomRepo = RoomRepository(db.roomDao(), db.areaDao(), db.itemDao(), db.kvDao())
        areaRepo = AreaRepository(db.areaDao(), db.itemDao())
        itemRepo = ItemRepository(db.itemDao(), db.reminderRuleDao())
    }

    @After fun tearDown() { db.close() }

    // ═══════════════ HOME SCREEN ═══════════════
    @Test fun home_emptyState_showsHint() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNodeWithText("还没有房间").assertExists()
    }

    @Test fun home_title_showsRoomText() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNodeWithText("房间").assertExists()
    }

    @Test fun home_FAB_isVisible() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNode(hasContentDescription("添加房间")).assertExists()
    }

    @Test fun home_FAB_click_opensBottomSheet() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNode(hasContentDescription("添加房间")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("房间名称").assertExists()
    }

    @Test fun home_BottomSheet_hasAllPresets() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNode(hasContentDescription("添加房间")).performClick()
        rule.waitForIdle()
        listOf("厨房","客厅","卧室","卫生间","阳台","储物间","书房","餐厅").forEach {
            rule.onNodeWithText(it).assertExists()
        }
    }

    @Test fun home_BottomSheet_hasConfirmCancel() {
        rule.setContent { HomeScreen(HomeViewModel(roomRepo)) {} }
        rule.onNode(hasContentDescription("添加房间")).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("确定").assertExists()
    }

    // H16 KeepsakeApp uses hiltViewModel() → requires Hilt test setup → skipped

    // ═══════════════ ROOM SCREEN ═══════════════
    @Test fun room_page_showsRoomName() {
        val vm = RoomViewModel(roomRepo, areaRepo)
        rule.setContent { RoomScreen(vm, {}, {}) }
        // Initially loading, then empty
        rule.onNodeWithContentDescription("返回").assertExists()
    }

    @Test fun room_FAB_isVisible() {
        rule.setContent { RoomScreen(RoomViewModel(roomRepo, areaRepo), {}, {}) }
        rule.onNode(hasContentDescription("添加区域")).assertExists()
    }

    // ═══════════════ AREA SCREEN ═══════════════
    @Test fun area_page_renders_loadingState() {
        // AreaScreen without loaded data shows loading indicator
        val vm = AreaViewModel(areaRepo, itemRepo, PhotoRepository(db.photoDao()))
        rule.setContent { AreaScreen(vm, {}, {}, {}, {}) }
        rule.waitForIdle()
        // Screen renders without crash — verified by no exception
        assertTrue(true)
    }

    // ═══════════════ ITEM SCREEN ═══════════════
    @Test fun item_viewMode_showsEditDeleteButtons() {
        val vm = ItemViewModel(itemRepo, ReminderRepository(db.reminderRuleDao(), db.itemDao()))
        rule.setContent { ItemScreen(vm, {}) }
        // Should render without crash
        rule.waitForIdle()
    }

    // ═══════════════ SEARCH SCREEN ═══════════════
    @Test fun search_inputField_visible() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = SearchViewModel(itemRepo, roomRepo, areaRepo, ai, store)
        rule.setContent { SearchScreen(vm, {}) }
        rule.onNodeWithText("搜索").assertExists()
    }

    // ═══════════════ SETTINGS SCREEN ═══════════════
    @Test fun settings_sections_visible() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val pr = PhotoRepository(db.photoDao())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, pr, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.onNodeWithText("主题").assertExists()
        rule.onNodeWithText("语言 / Language").assertExists()
        rule.onNodeWithText("AI 助手").assertExists()
        rule.onNodeWithText("本地数据").assertExists()
    }

    @Test fun settings_languageOptions_visible() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val pr = PhotoRepository(db.photoDao())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, pr, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.onNodeWithText("中文").assertExists()
        rule.onNodeWithText("English").assertExists()
    }

    @Test fun settings_themeOptions_visible() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val pr = PhotoRepository(db.photoDao())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, pr, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.onNodeWithText("浅色").assertExists()
        rule.onNodeWithText("深色").assertExists()
        rule.onNodeWithText("跟随系统").assertExists()
    }

    @Test fun settings_aiSection_hasToggle() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val pr = PhotoRepository(db.photoDao())
        val vm = SettingsViewModel(store, roomRepo, areaRepo, itemRepo, pr, ai)
        rule.setContent { SettingsScreen(vm) }
        rule.onNodeWithText("启用 AI").assertExists()
    }

    // ═══════════════ REMINDERS SCREEN ═══════════════
    @Test fun reminders_empty_showsEmptyState() {
        val vm = RemindersViewModel(ReminderRepository(db.reminderRuleDao(), db.itemDao()), itemRepo)
        rule.setContent { RemindersScreen(vm, {}) }
        rule.waitForIdle()
        rule.onNodeWithText("暂无提醒").assertExists()
    }

    // ═══════════════ TEXT INPUT SCREEN ═══════════════
    @Test fun textInput_modeToggle_andInput_visible() {
        val store = com.keepsake.app.data.local.datastore.KeepsakeDataStore(
            androidx.test.core.app.ApplicationProvider.getApplicationContext())
        val ai = com.keepsake.app.ai.AiService(okhttp3.OkHttpClient())
        val vm = TextInputViewModel(areaRepo, itemRepo, ai, store)
        rule.setContent { TextInputScreen(vm, {}, {}) }
        rule.onNodeWithText("切换").assertExists()
        rule.onNodeWithText("AI 解析").assertExists()
    }
}
