# Resource Module

统一的资源管理模块，包含 App 所有的公共资源文件。

## 包含内容

- **drawable/**: 图片资源
- **mipmap-*/**: 应用图标（不同分辨率）
- **values/**: 字符串、颜色、样式等资源
- **xml/**: XML 配置文件

## 使用方式

所有业务模块（app、base、login、main）都依赖此模块来访问资源文件。

```kotlin
// 在 build.gradle.kts 中添加依赖
dependencies {
    implementation(project(":resource"))
}
```
