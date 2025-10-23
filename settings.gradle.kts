// settings.gradle.kts
pluginManagement {
    repositories {
        // google() // 保留 google() 用于依赖项，但 pluginManagement 不需要 content 块
        // 如果你想保留 google() 用于插件管理，但允许所有内容，可以这样写：
        google() // <--- 移除下面的 content 块
        mavenCentral()
        gradlePluginPortal() // 这个仓库包含 Kotlin 插件
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google() // 保留 google() 用于常规依赖项
        mavenCentral()
    }
}

rootProject.name = "InsightNewsAndroid"
include(":app")