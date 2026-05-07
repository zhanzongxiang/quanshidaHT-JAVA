# qsd Admin 开发计划

## 阶段 0：基础设施

- 搭建 Vue 3 + TypeScript + Vite 管理端
- 接入 Element Plus 和 Tailwind CSS
- 搭建 Spring Boot 3 + Spring Security + JWT + MyBatis-Plus + Flyway
- 打通登录、鉴权、动态菜单、基础布局
- 配置前端 `/api` 代理和本地开发环境

## 阶段 1：后台管理基础能力

- 用户、角色、菜单、权限
- 登录态恢复与路由守卫
- 操作日志与登录日志基础能力
- 后台菜单结构调整为中文分组

## 阶段 2：页面与内容管理

### 首页配置

- 首页表单结构与后端存储对齐
- 支持草稿保存与发布
- 支持 Banner、运单查询、主营业务、一站式服务、我们承诺、新闻预览、SEO

### 线路页面

- 使用固定模板表单
- 支持多条线路独立编辑
- 支持草稿保存与发布

### 新闻资讯

- 使用区块化表单
- 支持段落、小标题、图片、图片说明
- 支持列表、详情、发布态管理

### 运单管理

- 支持后台维护运单、分段、轨迹事件
- 后台和公开查询统一显示中文状态
- 运单状态、线路类型、分段状态统一从字典模块读取

## 阶段 3：全局配置与字典

- 导航设置
- 页脚设置
- 联系方式设置
- 通用字典管理
- 让业务模块统一消费字典接口

## 阶段 4：官网公开接口

- 将官网读取能力与后台管理接口彻底分离
- 提供以下只读接口：
  - `/api/site`
  - `/api/pages/home`
  - `/api/pages/about`
  - `/api/pages/contact`
  - `/api/pages/service-line/{key}`
  - `/api/pages/news`
  - `/api/pages/news/{id}`
  - `/api/tracking/{trackingNo}`
- 所有公开接口只返回已发布数据

## 阶段 5：会员系统与小程序接入

### 后端开发

- 新增会员数据模型：
  - `member_user`
  - `member_waybill_relation`
- 新增会员认证与资料接口：
  - `POST /api/member/auth/register`
  - `POST /api/member/auth/login`
  - `GET /api/member/profile`
  - `PUT /api/member/profile`
- 新增会员运单接口：
  - `GET /api/member/waybills`
  - `GET /api/member/waybills/{id}`
- 新增后台会员管理接口：
  - `GET /api/admin/members`
  - `GET /api/admin/members/{id}`
  - `POST /api/admin/members`
  - `PUT /api/admin/members/{id}`
  - `PUT /api/admin/members/{id}/status`
- Spring Security 增加会员 token 解析链路，并隔离管理员与会员身份
- 新增 Flyway 迁移，补齐会员表、关联表、菜单、权限、字典种子数据

### 前端后台开发

- 新增会员管理菜单与路由
- 新增会员列表页：
  - 搜索手机号/昵称/姓名
  - 状态筛选
  - 查看绑定运单数
- 新增会员编辑弹窗或详情页：
  - 基础资料
  - 状态修改
  - 会员备注
- 新增会员绑定运单展示区
- API 层新增会员管理调用封装与类型声明

### 小程序可用性目标

- 小程序可独立完成会员注册、登录、取资料、改资料、查个人运单
- 返回结构稳定且与后台管理端 DTO 解耦
- 本阶段不交付小程序 UI，但接口需具备直接接入条件

### 测试与联调

- 后端至少通过 `mvn -DskipTests package`
- 前端至少通过 `npm run build`
- 手工验证以下流程：
  - 后台管理员查看会员列表
  - 后台新增或编辑会员
  - 会员注册并登录
  - 会员查看自己的运单列表和详情
  - 禁用会员后登录失败

## 当前重点

- 保持后台管理字段与前后台页面 schema 对齐
- 保持首页、线路模板、新闻、运单、会员五类模型职责清晰分离
- 保持公开接口、小程序会员接口、后台 JWT 接口相互隔离
- 保持基础枚举统一收敛到字典模块
- 保持 Flyway 迁移追加式演进，不修改历史迁移
