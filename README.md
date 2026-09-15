# 安卓 APK 工程

这是“研一课程表”的原生安卓壳工程，内部页面完全离线运行，课程修改保存在手机本地。

## 构建

需要 Android SDK 35、JDK 17 和 Gradle 8.11.1。在本目录运行：

```bash
gradle assembleDebug
```

生成文件：`app/build/outputs/apk/debug/app-debug.apk`。

也可以将本目录推送至 GitHub，仓库自带的 Actions 工作流会生成可下载 APK。
