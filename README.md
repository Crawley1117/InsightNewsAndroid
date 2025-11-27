好的，这是根据本次更新生成的 0.2 版本的 README。

---

# InsightNews for Android

## 项目文件结构

```
InsightNewsAndroid/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/
│   │       │       └── example/
│   │       │           └── insightnewsandroid/
│   │       │               ├── auth/                 # 认证相关 (登录/注册)
│   │       │               │   ├── AuthRepository.java
│   │       │               │   ├── LoginActivity.java
│   │       │               │   └── RegisterActivity.java
│   │       │               ├── data/                 # 数据层 (网络)
│   │       │               │   └── manager/          # 网络管理
│   │       │               │       ├── ApiManager.java
│   │       │               │       └── AuthService.java
│   │       │               ├── data.model/           # 数据模型 (网络响应)
│   │       │               │   ├── AnalysisReport.java
│   │       │               │   ├── BaseResponse.java
│   │       │               │   ├── DetectionHistoryItem.java
│   │       │               │   └── UploadTextResponse.java
│   │       │               ├── db/                   # 数据库相关 (本地)
│   │       │               │   ├── AppDatabase.java
│   │       │               │   ├── DetectionDao.java
│   │       │               │   ├── DetectionRecordEntity.java
│   │       │               │   └── SuspiciousSpan.java
│   │       │               ├── ui/                   # UI 相关
│   │       │               │   ├── credibility/      # 可信度检测界面
│   │       │               │   │   ├── CredibilityFragment.java
│   │       │               │   │   ├── CredibilityViewModel.java
│   │       │               │   │   ├── ChatMessage.java
│   │       │               │   │   └── CredibilityResult.java
│   │       │               │   ├── history/          # 历史记录界面
│   │       │               │   │   └── HistoryAdapter.java
│   │       │               │   └── profile/          # 个人主页
│   │       │               │       └── EditProfileActivity.java
│   │       │               ├── ui.detail/            # 话题详情界面
│   │       │               │   └── DetailTopicActivity.java
│   │       │               ├── ui.topic/             # 话题相关
│   │       │               │   └── TopicCollectionActivity.java
│   │       │               ├── EnhancedReportActivity.java
│   │       │               ├── HistoryActivity.java
│   │       │               ├── MainActivity.java
│   │       │               ├── ReportDetailActivity.java
│   │       │               └── SplashActivity.java
│   │       ├── res/
│   │       │   ├── drawable/                         # 图片和形状资源
│   │       │   ├── layout/                           # 布局文件
│   │       │   ├── menu/                             # 菜单资源
│   │       │   ├── mipmap/                           # 应用图标
│   │       │   ├── values/                           # 字符串、颜色、样式等
│   │       │   └── xml/                              # 配置文件 (如网络配置)
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts                              # 模块级构建脚本
├── gradle/
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── build.gradle.kts                                  # 项目级构建脚本
├── gradle.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── README.md
```

## 更新日志

### 最新版本：0.2 (邮箱验证 & 接口接入)

#### 🔐 认证系统核心改进 (邮箱验证)
- **登录/注册方式**: 将原有的手机号验证方式替换为**邮箱验证**。
- **UI 更新**:
  - `LoginActivity`: 输入框提示从“手机号”改为“邮箱地址”，输入类型改为 `textEmailAddress`。登录逻辑已更新以调用新的邮箱认证接口。
  - `RegisterActivity`: 输入框提示从“手机号”改为“邮箱地址”，输入类型改为 `textEmailAddress`，并**新增了密码输入框**。注册逻辑已更新以调用新的邮箱注册接口。
- **Token 管理**: `AuthRepository` 中的用户标识存储从 `KEY_PHONE` 改为 `KEY_EMAIL`，相关方法名 (`getPhone` -> `getEmail`) 和字段映射 (`@SerializedName`) 已更新。`getAuthToken()` 方法用于获取存储的 Token。
- **调试模式**: `LoginActivity` 中的密码调试模式 `1117` 保持不变。

#### 📝 新闻内容检测接口接入
- **文本检测**:
  - `CredibilityViewModel`: `addUserMessage` 方法不再使用本地模拟分析，而是调用后端 `POST /detection/upload/text` 接口。UI 会显示“正在分析...”状态。
  - `CredibilityFragment`: `addButton` 点击事件触发文本上传。
- **图片检测**:
  - `CredibilityViewModel`: 新增 `addUserMessageWithImage` 和 `callUploadImageDetection` 方法，用于调用后端 `POST /detection/upload/file` 接口。UI 会显示“正在分析...”状态。
  - `CredibilityFragment`: `photoButton` 和 `cameraButton` 点击事件已实现图片选择和拍照功能，并调用 ViewModel 上传图片。

#### 📋 检测历史与分析报告接口预留
- **接口定义**:
  - `AuthService`: 新增 `GET /detection/history` (查看历史) 和 `GET /detection/report/{id}` (查看报告)、`GET /detection/report/download/{id}?format=...` (下载报告) 的接口定义。
- **本地数据优先**:
  - `HistoryActivity` 和 `ReportDetailActivity` 当前**仍使用本地数据库或 Intent 传入的数据**，未切换到新的网络接口。相关逻辑保持不变。

#### 📦 数据模型更新
- **新增**:
  - `AnalysisReport.java`: 用于接收后端“查看分析报告”接口的完整响应。
  - `DetectionHistoryItem.java`: 用于接收后端“查看检测历史”接口返回的单条记录。
  - `UploadTextResponse.java`: 用于接收后端“上传文本/图片检测”接口的响应（当前为空）。
- **修改**:
  - `SuspiciousSpan.java` (在 `db` 包): 为所有字段添加了 `@SerializedName` 注解，以确保与后端 JSON 字段正确映射 (`credibility_score` <-> `credibilityScore`)。
  - `BaseResponse.java`: 更新了字段名以匹配后端实际返回 (`code`, `msg`, `data`)。

#### 🌐 网络层配置
- **依赖**: 项目已引入 `Retrofit2`, `Gson Converter`, `OkHttp` 及其日志拦截器。
- **配置**: `network_security_config.xml` 已配置允许对后端服务器 IP `120.79.169.214` 进行 HTTP 通信。

---

### 历史版本

- **0.1.5.8**：实现 Token 存储与管理，优化登录注册流程，调整调试模式。
- **0.1.5.7**：实现新闻收藏功能（修改了credibility界面的收藏按钮，同步收藏功能）
- **0.1.5.6**：修复部分遗留的问题
- **0.1.5.5**：修复了credibility界面显示严重扭曲的问题，优化了credibility history enhancedReport页面的布局; 修复了history对应数据库空搜索导致崩溃的问题；现在credibility默认显示最新的history记录
- **0.1.5.4**：修复评论的部分问题（包括时间更新、输入框弹出、用户信息实时更新），修复资料编辑部分问题
- **0.1.5.3**：重新设计了credibility界面，完善了history和credibility贴图，修复了部分恶性崩溃问题
- **0.1.5.2**：重构了部分UI界面，修复少量BUG
- **0.1.5.1**：连接个人主页"历史记录"正确跳转历史记录页面,点击话题详情页的新闻可以正确跳转详细报告分析页,个人主页"退出登录"按键可以正常使用,探究针真相右上角heart可以正常跳转话题收藏界面
- **0.1.5**：完善个人主页模块，包括资料编辑、新闻收藏、话题收藏;构建探究真相界面，实现话题详情页面、话题收藏功能、评论系统
- **0.1.4.2**：添加了验证码的网络接口，留下了调试入口（登陆时验证码输入041122）,修复了少量导致崩溃的问题。
- **0.1.4.1**：修正了对话页面的图标显示错误,修复了加载路径错误导致崩溃的问题。
- **0.1.4**：新增登入登出功能，美化了界面，修复了一些错误。
- **0.1.3**：新增了历史搜索功能，美化了界面。
- **0.1.2**：新增了分析UI。
- **0.1**：构建了主要界面和事件。

---

# TODO LIST

- 注册登陆界面的返回按钮
- 夜间模式XML更新
- 接入请求接口 (部分已实现)
