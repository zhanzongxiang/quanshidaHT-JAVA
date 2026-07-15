# 集运订单系统会员端（uni-app 多端）实施方案

## 目标

在现有 `quanshidaHT-JAVA` 项目下新增一个会员端工程 `member-app`，用于支持手机优先的会员使用场景，并保留未来扩展到 H5 / 小程序 / App 的能力。

## 目录规划

- `backend/`：Java 后端
- `frontend/`：后台管理端
- `member-app/`：uni-app 会员端

## 会员端一期页面

- 首页 / 会员中心
- 登录
- 我的账户
- 收货地址
- 运单预报
- 我的库存
- 集运申请
- 我的订单

## 后端一期建议模块

- `member-auth`：会员登录、token、修改密码
- `member`：会员资料、分组、状态
- `member-address`：收货地址
- `package`：预报、认领、库存、问题件
- `shipment`：集运申请
- `order`：订单管理、支付状态
- `finance`：账单与支付记录
- `marketing`：推广链接

## 说明

当前先完成会员端骨架，下一步进入后端会员认证与业务接口设计。
