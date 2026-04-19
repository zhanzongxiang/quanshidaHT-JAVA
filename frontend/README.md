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
- 未使用的脚手架组件和资源应及时删除，避免继续占用构建产物和维护成本

## 页面管理约束

### 首页配置

- 页面位于 `src/views/ContentView.vue`
- 对接后端接口：
  - `GET /api/content/home`
  - `PUT /api/content/home/draft`
  - `PUT /api/content/home/publish`

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

### 运单管理

- 页面位于 `src/views/WaybillView.vue`
- 后端接口位于 `/api/waybills/*`
- 页面所有用户可见文案统一使用中文
- 状态和线路类型通过字典接口读取，不再长期硬编码在页面内

### 字典管理

- 页面位于 `src/views/DictionarySettingsView.vue`
- 后端接口位于 `/api/dictionaries/*`
- 当前主要用于维护运单状态、线路类型、分段状态等基础字典

## 接口使用约束

- 管理端继续使用：
  - `/api/auth/*`
  - `/api/content/*`
  - `/api/news/*`
  - `/api/waybills/*`
  - `/api/dictionaries/*`
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
