# qsd Admin Frontend

## 技术栈

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus
- Tailwind CSS

## 本地开发

- 安装依赖：`npm install`
- 启动开发环境：`npm run dev`
- 生产构建：`npm run build`

如果修改了 `vite.config.ts`、`postcss.config.js`、`tailwind.config.js` 或 `.env.development`，需要重启开发服务。

## UI 方案

- Element Plus 负责交互组件
- Tailwind CSS 负责布局、间距、排版、响应式和主要视觉样式
- `scoped CSS` 只保留少量组件深层覆盖

## 路由约定

- `/` 默认重定向到 `/dashboard`
- 未登录访问后台路由时，统一跳转到 `/login?redirect=当前地址`
- 已登录访问 `/login` 时，自动回到 `/dashboard`
- 登录成功后先拉取 `/api/auth/me`，再注入动态菜单路由并跳转
- `404` 只用于动态路由注入后仍无法匹配的地址
- 退出登录时重置动态菜单注入状态

## 接口代理约定

- 前端请求统一走 `/api`
- 开发代理由 `vite.config.ts` 配置
- 代理目标由 `.env.development` 中的 `VITE_API_PROXY_TARGET` 指定
- 业务代码中不写死后端绝对地址

## 首页内容管理约定

- 页面位置：`src/views/ContentView.vue`
- 数据结构：`src/types/content.ts`
- 当前读写实现：`src/api/content.ts`
- 当前保存草稿和发布使用本地存储 mock

当前表单覆盖：

- Banner 图片组
- 首屏主标题和次要内容
- 两个 CTA 按钮名称和链接
- 运单追踪模块
- 主营业务模块
- 联系转化区
- SEO 设置

Banner 图片组支持：

- 上传
- 预览
- 删除
- 设为首图

运单追踪模块支持：

- 开关控制显示
- 标题修改
- 占位文案修改
- 查询按钮名称修改

主营业务模块支持：

- 开关控制显示
- 模块标题修改
- 模块描述修改
- 新增多个业务小模块
- 每个业务小模块上传或移除图标
- 每个业务小模块维护名称、描述和查看更多路由

渲染要求：

- 页面进入后必须可见顶部摘要、左侧表单区和右侧预览区
- 首图变更后右侧预览必须同步更新
- 按钮名称和链接修改后预览区即时更新
- 运单追踪模块开关或标题修改后预览区即时更新
- 主营业务模块及业务小模块修改后预览区即时更新
- 读取本地草稿时必须兼容旧版数据结构，不能因字段升级导致页面渲染中断
- 后台布局只允许右侧内容区出现滚动条，不使用全局滚动条

## 关键文件

- `src/router/index.ts`
- `src/stores/auth.ts`
- `src/api/http.ts`
- `src/api/content.ts`
- `src/types/content.ts`
- `src/views/LoginView.vue`
- `src/views/ContentView.vue`
- `src/layouts/AdminLayout.vue`
- `src/style.css`
- `vite.config.ts`
- `tailwind.config.js`
- `postcss.config.js`
- `.env.development`
