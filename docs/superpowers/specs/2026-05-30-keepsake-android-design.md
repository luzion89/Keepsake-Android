# Keepsake Android — 设计规范文档

> 日期: 2026-05-30
> 项目: Keepsake Android APK (纯离线版)
> 灵感来源: Keepsake Web PWA (家庭物品管理)
> 技术栈: Kotlin + Jetpack Compose + Material Design 3 + Room

---

## 1. 项目概述

### 1.1 背景

[Keepsake Web PWA](https://github.com/luzion89/keepsake) 是一个功能完善的家庭物品管理系统，支持房间/区域/物品层级管理、AI 文字解析录入、智能搜索、拍照存档和提醒等功能。

在实际使用中，Web 版存在两个不便之处：

1. **每次使用需要手动启动服务器** — 需在 PC 上运行 `npm start` 启动 Keepsake Server，服务器关闭后无法访问
2. **浏览器访问体验不佳** — 通过局域网 IP + 端口在手机上访问，浏览器地址栏和工具栏占据屏幕空间，且无法利用原生系统特性（如系统级通知、桌面快捷操作等）

因此决定开发一个**纯原生 Android APK 离线版本**，所有数据存储在手机本地，无需依赖任何服务端，开箱即用。

**关于 iOS 版本**：未来可能考虑开发 iOS 版本，但目前因 Apple 开发者账号费用问题暂缓。

### 1.2 目标

将 Keepsake Web PWA 移植为原生 Android APK。第一个版本为纯离线版，所有数据存储在手机本地 Room 数据库中。后续版本可添加与 Keepsake Server 的同步功能。

### 核心功能
- 房间 → 区域 → 物品 层级管理
- 拍照 + 图片压缩管理
- AI 文字解析批量录入物品（DeepSeek / OpenRouter）
- AI 智能搜索问答
- 物品过期提醒 / 低库存提醒
- 多语言（中文 / 英文）
- Material Design 3 UI
- 数据导出 (JSON)

---

## 2. 技术架构

### 2.1 技术栈

| 层面 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin | Android 官方推荐 |
| UI | Jetpack Compose + Material 3 | 声明式 UI |
| 数据库 | Room (SQLite) | 对应 web 版 Dexie + SQLite |
| 图片加载 | Coil | Compose 原生支持 |
| 相机 | CameraX | Android 官方相机 API |
| 导航 | Navigation Compose | 类型安全导航 |
| DI | Hilt | 依赖注入 |
| KV 存储 | DataStore Preferences | 对应 web 版 kv 表 |
| AI 调用 | OkHttp + Gson | 直接调用 DeepSeek/OpenRouter API |
| 异步 | Kotlin Coroutines + Flow | 响应式数据流 |
| 测试 | JUnit 5, Mockito, Compose UI Test | 多层测试 |

### 2.2 架构模式：MVVM + Repository

```
┌─────────────────────────────────────────────────┐
│  UI Layer (Composables)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │  Screens │  │Components│  │ ViewModels│     │
│  └──────────┘  └──────────┘  └──────────┘     │
├───────────StateFlow (观察)─────────────────────┤
│  Repository Layer                              │
│  ┌─────────────────────────────────────────┐   │
│  │  Repositories (抽象数据访问接口)           │   │
│  └─────────────────────────────────────────┘   │
├────────────────────────────────────────────────┤
│  Data Layer                                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐     │
│  │Room DAO  │  │DataStore │  │OkHttp AI│      │
│  └──────────┘  └──────────┘  └──────────┘     │
└────────────────────────────────────────────────┘
```

### 2.3 最小支持版本
- **minSdk**: 31 (Android 12)
- **targetSdk**: 最新版
- **compileSdk**: 最新版

---

## 3. 数据模型

### 3.1 Room Entities（对应 SQLite 表）

所有实体继承 `SyncMeta` 基础字段（为后续同步预留）：

```kotlin
@Entity
data class SyncMeta(
    @PrimaryKey val id: String,           // UUID
    val updatedAt: Long = 0,              // 时间戳
    val updatedBy: String = "",           // 设备 ID
    val deleted: Boolean = false,         // 软删除
    val version: Int = 0                  // 版本号
)
```

### 3.2 数据表

| 表名 | 特有字段 | 用途 |
|------|---------|------|
| `rooms` | `name`, `icon`, `photoIds: List<String>`, `note` | 房间 |
| `areas` | `roomId`, `name`, `photoIds: List<String>`, `note` | 区域，属于房间 |
| `items` | `areaId`, `name`, `qty`, `unit`, `tags: List<String>`, `photoIds: List<String>`, `expiresAt`, `source`, `confidence`, `notes`, `createdAt` | 物品，属于区域 |
| `photos` | `parentType`, `parentId`, `takenAt`, `blobUri`, `recognitionStatus` | 照片 |
| `snapshots` | `areaId`, `takenAt`, `itemIds: List<String>`, `note` | 区域快照 |
| `reminder_rules` | `itemId`, `kind`, `thresholdAt`, `thresholdQty`, `note`, `lastFiredAt` | 提醒规则 |
| `kv` | `key: String`(PK), `value: String`, `updatedAt` | 键值配置存储 |

### 3.3 类型转换器

Room 需要 TypeConverter 处理复杂类型：
- `List<String>` ↔ JSON String
- `RecognitionStatus` ↔ String
- `ItemSource` ↔ String

---

## 4. 导航结构

```kotlin
NavHost(startDestination = "home") {
    composable("home")               → HomeScreen       // 房间列表
    composable("rooms/{roomId}")     → RoomScreen       // 区域列表
    composable("areas/{areaId}")     → AreaScreen       // 物品列表
    composable("areas/{areaId}/capture") → CaptureScreen // 拍照
    composable("areas/{areaId}/text") → TextInputScreen  // AI文字录入
    composable("items/{itemId}")     → ItemScreen       // 物品详情
    composable("search")             → SearchScreen     // 搜索+AI问答
    composable("settings")           → SettingsScreen   // 设置
    composable("reminders")          → RemindersScreen  // 提醒
}
```

底部导航栏 4 个 Tab：房间、搜索、提醒、设置

---

## 5. 页面详细设计

### 5.1 HomeScreen（房间列表）

- **布局**: LazyColumn + RoomCard
- **交互**:
  - 点击卡片 → 进入 RoomScreen
  - 点击名称 → 内联重命名（TextField）
  - 长按/滑动 → 删除确认对话框
  - FAB → 展开 BottomSheet 表单（名称输入 + 预设图标列表）
  - 预设名称复用 web 版 `PRESET_NAMES`：厨房、客厅、卧室、阳台等
- **状态**: 房间列表、加载中、空状态
- **ViewModel**: `HomeViewModel` — 调用 `RoomRepository`

### 5.2 RoomScreen（区域列表）

- **Breadcrumb**: "房间 > {room_name}"
- **布局**: LazyColumn + AreaCard
- **交互**: 同 HomeScreen（内联重命名、滑动删除、FAB 添加）
- **显示**: 每个区域下显示物品数量统计
- **ViewModel**: `RoomViewModel`

### 5.3 AreaScreen（物品列表 + 照片）

- **Breadcrumb**: "房间 > {room_name} > {area_name}"
- **CTA 按钮**: "文字录入" → TextInputScreen, "拍照" → CaptureScreen
- **手动添加表单**: 可折叠 Card，名称 + 数量
- **物品列表**:
  - 名称、数量、单位、过期标签（颜色编码）
  - +/- 数量按钮（qtyDelta）
  - 来源标签（AI/手动）、置信度
  - 滑动删除
  - _点击进入 ItemScreen 详细编辑_
- **照片时间线**: 按月分组的照片缩略图
- **灯箱 (Lightbox)**: 全屏查看、左右滑动切换、下载/删除
- **ViewModel**: `AreaViewModel`

### 5.4 ItemScreen（物品详情/编辑）

- **双模式**: 查看模式 / 编辑模式
- **查看模式**:
  - 大号衬线字体名称
  - 过期标签（绿色 ≤30d, 黄色 <7d, 红色已过期）
  - +/- 数量调整
  - 创建时间、来源、置信度
  - 备注、标签（FlowRow）
  - 关联照片
- **编辑模式**（SectionCard 分段）:
  - 基本信息: 名称、数量、单位
  - 时间: 日期选择器（过期日）+ 实时 ExpiryBadge
  - 描述: 备注 TextField、逗号分隔标签输入
  - 提醒: 添加/编辑提醒规则（过期/补货/复查）
- **草稿编辑入口**（从 TextInputScreen 跳转来时）:
  - 完整编辑界面，保存后返回草稿列表
- **ViewModel**: `ItemViewModel`

### 5.5 CaptureScreen（拍照）

- CameraX + 文件选择器双入口
- 图片压缩（参考 web 版参数：max 0.8MB, 1600px）
- 预览网格（3列）+ 逐张删除
- 保存: 存入 Room (photos 表) + 内部存储

### 5.6 TextInputScreen（AI 文字录入）

- **输入**: 多行 TextField（自由文本描述）
- **AI 解析**: 调用 DeepSeek/OpenRouter API
  - System prompt 要求：
    - 提取物品名称到 `name`
    - 提取数量到 `qty`
    - **提取修饰性/位置性/描述性信息到 `notes`**
    - 示例: "白色沐浴露在牙刷旁边" → `{name:"沐浴露", qty:1, notes:"白色，在牙刷旁边"}`
  - Merge 模式：AI 结合当前物品列表进行合并
  - Replace 模式：完全替换
- **草稿列表**: 每个草稿项可点击 → 跳转 ItemScreen 详细编辑
- **确认**: 批量保存到 Room

### 5.7 SearchScreen（搜索 + AI 问答）

- **本地搜索**: 实时过滤 Room 数据库（name/notes/tags）
- **结果分组**: 按 "房间 > 区域" 路径分组
- **AI 模式**: 发送查询 + 上下文（最多30条）到 AI
  - 返回自然语言答案 + 引用物品 ID 列表
  - 引用物品高亮显示，可点击跳转

### 5.8 SettingsScreen（设置）

| 设置项 | 类型 | 说明 |
|--------|------|------|
| 主题 | 枚举 | 浅色/深色/跟随系统 |
| 语言 | 枚举 | 中文/English |
| AI 开关 | Switch | 启用/禁用 AI |
| AI Provider | 选择 | DeepSeek / OpenRouter |
| API Key | 密码输入 | 各自服务的 API Key |
| 模型名 | TextField | 可选的模型覆盖 |
| 数据统计 | 只读 | 房间/区域/物品/照片 数量 |
| 导出 JSON | 按钮 | 导出全部数据 |
| 清理图片 | 按钮 | GC 未引用图片 |
| 设备 ID | 只读 | 显示当前设备标识 |

### 5.9 RemindersScreen（提醒列表）

- 启动时 + 每60秒扫描 reminder_rules
- 三种触发条件：
  - `expiry`: `item.expires_at - now <= threshold_at`
  - `low_stock`: `item.qty <= threshold_qty`
  - `recheck`: `now - last_fired_at >= threshold_at`
- 防重复: 1小时内不重复触发
- 卡片列表: 原因 + 物品名 + "查看物品"/"已读" 按钮
- 空状态: 大铃铛图标 + "暂无提醒"

---

## 6. AI 集成

### 6.1 支持 Provider

| Provider | 默认模型 | API 地址 |
|----------|---------|---------|
| DeepSeek | `deepseek-chat` | `https://api.deepseek.com/v1/chat/completions` |
| OpenRouter | `google/gemini-2.5-flash-lite` | `https://openrouter.ai/api/v1/chat/completions` |

### 6.2 三个 AI 功能

1. **parseItemsFromText(text, existingItems?, mode?)**: 文字解析为结构化物品列表
   - 复用 web 版的 JSON schema prompt
   - 新增：将描述性信息提取到 notes 字段

2. **searchAnswer(query, contextItems)**: 自然语言问答
   - 返回 `{answer: String, citedIds: List<String>}`

3. **pingProvider(provider, apiKey)**: 验证 API Key

### 6.3 AI 调用方式

- 使用 OkHttp 直接发送 HTTP POST
- 请求体 JSON: `{model, messages: [...]}`
- 响应解析: Gson 解析 AI 返回的 JSON
- 全部在手机端完成，不经过中间服务器

---

## 7. 图片管理

- **存储位置**: App 内部存储 (`context.filesDir/photos/`)
- **压缩**: Bitmap.compress(WebP, 80) 或类似，目标 < 800KB
- **关联**: photos 表记录 URI + parent_type + parent_id
- **GC**: 删除物品时同时删除关联图片文件

---

## 8. 项目结构

```
F:\Keepsake-Android\
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/keepsake/app/
│   │   │   │   ├── KeepsakeApp.kt          # Application class
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── dao/            # Room DAOs
│   │   │   │   │   │   ├── entity/         # Room entities
│   │   │   │   │   │   ├── database/       # Room DB
│   │   │   │   │   │   └── datastore/      # DataStore
│   │   │   │   │   └── repository/         # Repo implementations
│   │   │   │   ├── domain/
│   │   │   │   │   ├── model/              # Domain models
│   │   │   │   │   └── repository/         # Repo interfaces
│   │   │   │   ├── ai/
│   │   │   │   │   ├── AiRouter.kt
│   │   │   │   │   ├── ParseService.kt
│   │   │   │   │   └── SearchService.kt
│   │   │   │   └── ui/
│   │   │   │       ├── theme/              # MD3 Theme
│   │   │   │       ├── components/         # Shared composables
│   │   │   │       ├── navigation/         # NavHost
│   │   │   │       ├── home/
│   │   │   │       ├── room/
│   │   │   │       ├── area/
│   │   │   │       ├── item/
│   │   │   │       ├── capture/
│   │   │   │       ├── textinput/
│   │   │   │       ├── search/
│   │   │   │       ├── settings/
│   │   │   │       └── reminders/
│   │   │   └── res/                        # Resources
│   │   ├── test/                           # Unit tests
│   │   └── androidTest/                    # Instrumented tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts                        # Root build file
├── settings.gradle.kts
├── gradle.properties
├── .github/
│   └── workflows/
│       └── build.yml                       # GitHub Actions
└── docs/superpowers/specs/
    └── 2026-05-30-keepsake-android-design.md  # 本文档
```

---

## 9. 测试策略

### 9.1 单元测试 (JUnit 5 + Mockito)
- Repository 逻辑
- AI Prompt 解析/响应处理
- 数据合并逻辑
- 日期工具类

### 9.2 数据库测试 (Room 内存数据库)
- DAO 增删改查
- 复杂查询（搜索、按区域列表）

### 9.3 UI 测试 (Compose UI Test)
- 每个页面的核心交互流程
- 导航测试
- 表单输入验证

### 9.4 端到端测试清单
- 创建房间 → 添加区域 → 添加物品
- AI:"三瓶洗发水" → 正确解析 `{name:"洗发水", qty:3, unit:"瓶"}`
- AI:"白色沐浴露在牙刷旁边" → `{name:"沐浴露", notes:"白色，在牙刷旁边"}`
- 拍照 → 压缩 → 关联物品
- 物品过期 → 自动提醒
- 滑动删除 → 确认 → 数据移除
- 搜索 → 过滤 → 结果点击
- 数据导出 JSON → 格式正确
- 中英文切换 → 界面语言变更

---

## 10. GitHub Actions 编译流水线

```yaml
name: Build Keepsake Android

on:
  push:
    branches: [ main, dev ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 17
        uses: actions/setup-java@v4
        with:
          java-version: 17
          distribution: 'temurin'

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v3

      - name: Build Debug APK
        run: ./gradlew assembleDebug

      - name: Run Unit Tests
        run: ./gradlew test

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: keepsake-android-debug
          path: app/build/outputs/apk/debug/*.apk
```

每次 `push` 自动：
1. 编译 Debug APK
2. 运行单元测试
3. 上传 APK 为 artifact（可直接下载安装）

---

## 11. 后续规划（不在 v1 范围内）

- [ ] 服务端同步（与现有 Keepsake Server 双向同步）
- [ ] 语音输入（Android Speech-to-Text）
- [ ] 桌面 Widget（快速查看/搜索）
- [ ] 系统通知（提醒推送）
- [ ] Material You 动态取色

---

## 12. 设计决策记录

| 决策 | 选项 | 选择 | 原因 |
|------|------|------|------|
| 同步方式 | 纯离线 / 离线+同步 | 纯离线 v1 | 降低初始复杂度 |
| UI 框架 | MD3 / 复刻 Muji | MD3 | 更原生的 Android 体验 |
| 最低 API | 26 / 31 | 31 | 可使用最新 Compose 特性 |
| AI 部署 | 本地 API / 代理 | 本地 API | 复用 web 版直接调用模式 |
| DI | Hilt / Koin / Manual | Hilt | 官方推荐，编译时安全 |
