# qsd Admin Frontend

## 技术栈

- Vue 3
- TypeScript
- Vite
- Vue Router
- Pinia
- Element Plus

## 本地开发

- 安装依赖：`npm install`
- 启动开发环境：`npm run dev`
- 生产构建：`npm run build`

## 路由约定

- `/` 默认重定向到 `/dashboard`
- 未登录访问 `/dashboard` 或其他后台路由时，统一跳转到 `/login?redirect=当前地址`
- 已登录访问 `/login` 时，自动回到 `/dashboard`
- `404` 只用于动态菜单路由注入后仍然无法匹配的地址，不能作为未登录用户访问后台地址时的默认落点

## 关键文件

- `src/router/index.ts`：路由定义与守卫
- `src/stores/auth.ts`：认证状态
- `src/views/LoginView.vue`：登录页

## 动态菜单

- 后台菜单和动态路由依赖 `/api/auth/me`
- 登录成功后需要先完成用户信息初始化，再注入菜单路由
- 动态路由注入后应重新匹配当前地址，避免把合法菜单地址误判为 `404`
