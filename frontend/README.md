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

```bash
cd frontend
npm install
npm run dev
```

默认开发地址：

- `http://localhost:5174`

如果修改了以下文件，需要重启前端开发服务：

- `vite.config.ts`
- `.env.development`
- `postcss.config.js`
- `tailwind.config.js`

## 目录关注点

- 路由：`src/router`
- 页面：`src/views`
- 布局：`src/layouts`
- API：`src/api`
- 类型：`src/types`

## 页面管理约束

### 首页配置

- 页面位于 `src/views/ContentView.vue`
- 对接后端接口：
  - `GET /api/content/home`
  - `PUT /api/content/home/draft`
  - `PUT /api/content/home/publish`
- 首页数据结构需要和后端 schema 对齐

### 线路页面

- 列表页位于 `src/views/ServiceLinesView.vue`
- 编辑页位于 `src/views/ServiceLineEditorView.vue`
- 使用固定模板表单，不允许自由拼版

### 新闻资讯

- 页面位于 `src/views/NewsView.vue`
- 使用区块化表单
- 当前支持的区块类型：
  - `paragraph`
  - `heading`
  - `image`
  - `image_caption`

## 全局配置约束

- 导航设置、页脚设置、联系方式属于全局配置
- 当前联系方式页是结构化表单，不是简单键值输入
- 全局设置当前部分内容仍可能使用前端本地存储，后续需要逐步迁移到后端

## 接口使用约束

- 管理端继续使用：
  - `/api/auth/*`
  - `/api/content/*`
  - `/api/news/*`
- 官网前台不要直接调用管理端 JWT 接口
- 官网前台应使用公开只读接口：
  - `/api/site`
  - `/api/pages/*`
  - `/api/tracking/*`

## UI 约束

- 后台统一使用 `Element Plus + Tailwind CSS`
- 表单、按钮、弹窗、消息提示优先使用 Element Plus
- 布局、间距、栅格、响应式样式优先使用 Tailwind CSS
- 后台整体布局只允许右侧内容区域滚动
