# uni-app 小程序在微信开发者工具中的使用说明

更新时间：2026-05-09

## 当前状态

- `miniapp/` 依赖已安装完成
- `vue-tsc --noEmit` 已通过
- `npm run build:mp-weixin` 已通过
- 微信小程序构建产物目录已确认：
  - `miniapp/dist/build/mp-weixin`

## 目录

- 小程序工程目录：[miniapp/](</E:/me/quanshidaHT-JAVA/miniapp>)
- 微信开发者工具项目配置：[project.config.json](/E:/me/quanshidaHT-JAVA/miniapp/project.config.json:1)

## 启动前准备

1. 启动后端

```powershell
cd E:\me\quanshidaHT-JAVA
.\backend\start-dev.ps1
```

2. 设置小程序请求地址

```powershell
$env:VITE_API_BASE_URL="http://127.0.0.1:8080"
```

如果用真机联调，这里需要替换成手机可访问的局域网地址或测试域名。

## 本地命令

在 `miniapp/` 目录下执行：

```powershell
npm run dev:mp-weixin
```

如果要生成可导入微信开发者工具的构建产物：

```powershell
npm run build:mp-weixin
```

## 微信开发者工具导入方式

### 方式一：导入构建产物

适合当前仓库的稳定联调方式。

1. 执行 `npm run build:mp-weixin`
2. 打开微信开发者工具
3. 选择“导入项目”
4. 项目目录选择：

```text
E:\me\quanshidaHT-JAVA\miniapp\dist\build\mp-weixin
```

5. `AppID` 当前可先使用测试值，正式联调前再切换真实小程序 `appid`

### 方式二：开发模式配合热更新

1. 执行 `npm run dev:mp-weixin`
2. 观察 `miniapp/dist/dev/mp-weixin` 是否持续输出
3. 在微信开发者工具中导入对应 dev 输出目录

如果团队后续固定使用 build 目录联调，建议统一只导入 `dist/build/mp-weixin`，降低环境差异。

## 当前必须确认的配置

- `miniapp/src/manifest.json` 中 `mp-weixin.appid` 目前仍是 `touristappid`
- 正式联调前必须替换为真实微信小程序 `appid`
- 如果后端切真实微信支付，后端支付商户配置中的 `appId` 必须和小程序 `appid` 对齐

## 真机联调前自检

- 后端接口地址可从手机访问
- 小程序登录接口可用
- 支付准备接口可用
- 后台当前生效商户状态为 `Ready`
- 支付回调地址不是 `localhost`
- 微信后台已配置合法服务器域名

## 当前剩余事项

- 已通过 CLI 尝试拉起微信开发者工具并打开 `miniapp/dist/build/mp-weixin`
- 尚未补页面预览截图留档
- 尚未做真机支付联调
- 尚未替换正式 `appid`
- 尚未完成微信后台域名白名单和支付商户正式配置

## 联调留档

建议每次开发者工具联调结束后，按模板补一份记录：

- [小程序开发者工具联调记录模板](./miniapp-devtools-test-record.md)
