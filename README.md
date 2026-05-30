# Keepsake Android

家庭物品管理的纯离线 Android 应用。知道家里有什么、放在哪里。

## 背景

[Keepsake Web PWA](https://github.com/luzion89/keepsake) 是一个功能完善的家庭物品管理系统。但在实际使用中存在两个痛点：

- **每次需要手动启动服务器** — PC 上运行 `npm start`，关机即不可用
- **浏览器访问体验差** — 手机浏览器地址栏/工具栏挤占空间，无法利用原生系统特性

因此开发了这个纯原生 Android APK 离线版本。所有数据存储在手机本地，无需任何服务端，即开即用。

> iOS 版本未来可能考虑，目前因 Apple 开发者账号费用问题暂缓。

## 功能

- 🏠 房间 → 区域 → 物品 三级层级管理
- 📸 拍照 + 自动压缩关联物品
- 🤖 AI 文字解析批量录入（DeepSeek / OpenRouter，手机端直连）
- 🔍 AI 自然语言搜索问答
- ⏰ 物品过期提醒 / 低库存提醒
- 🌐 中英文多语言
- 🎨 Material Design 3
- 📦 JSON 数据导出

## 技术栈

| 层面 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 数据库 | Room (SQLite) |
| 图片 | CameraX + Coil |
| DI | Hilt |
| AI | OkHttp 直连 DeepSeek/OpenRouter |
| 最低版本 | Android 12 (API 31) |

## 构建

```bash
./gradlew assembleDebug    # 编译 Debug APK
./gradlew test             # 运行单元测试
./gradlew connectedCheck   # 运行仪器化测试
```

GitHub Actions 会在每次 push 时自动编译 APK 并上传为 artifact。

## 项目结构

```
app/src/main/java/com/keepsake/app/
├── data/                   # 数据层 (Room DAO, DataStore, Repository)
├── domain/                 # 领域层 (模型 + Repository 接口)
├── ai/                     # AI 路由 (DeepSeek / OpenRouter)
├── ui/
│   ├── theme/              # MD3 主题
│   ├── components/         # 共享组件
│   ├── navigation/         # 导航图
│   ├── home/               # 房间列表
│   ├── room/               # 区域列表
│   ├── area/               # 物品列表 + 照片
│   ├── item/               # 物品详情/编辑
│   ├── capture/            # 拍照
│   ├── textinput/          # AI 文字录入
│   ├── search/             # 搜索 + AI 问答
│   ├── settings/           # 设置
│   └── reminders/          # 提醒
└── KeepsakeApp.kt          # Application
```

## 许可证

MIT License
