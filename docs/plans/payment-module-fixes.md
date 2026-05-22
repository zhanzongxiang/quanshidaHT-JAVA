# 支付模块问题修复计划

> 审查日期: 2026-05-22
> 状态: 🔴 进行中

---

## P0 — 严重问题（必须修复）

### ✅ FIX-01: 回调签名验证可被绕过
- **文件**: `PaymentCallbackController.java`, `WechatPayCallbackParser.java`
- **问题**: 回调接口在缺少 `Wechatpay-Timestamp/Nonce/Signature` headers 时跳过验签，攻击者可伪造支付成功回调
- **修复方案**:
  - `PaymentCallbackController`: 缺少任一 header 时直接返回失败，不处理回调
  - `WechatPayCallbackParser.verifySignatureIfPresent()`: 改为 `verifySignature()`，缺少参数时抛异常而非跳过
- [ ] 未开始

### ✅ FIX-02: 退款金额可超额退款
- **文件**: `PaymentService.java` (`createRefund`, `retryRefund`)
- **问题**: 退款校验只与 `amountPaid` 比较，未扣除已退款成功的总额，多次退款可超额
- **修复方案**:
  - 新增方法 `calculateRefundedAmount(payOrderId)`: 查询所有 `status=succeeded` 的退款单总额
  - 校验逻辑: `新退款金额 + 已退款总额 ≤ amountPaid`
  - `createRefund()` 和 `retryRefund()` 均需加入此校验
- [ ] 未开始

### ✅ FIX-03: 退款成功后订单状态逻辑错误
- **文件**: `PaymentService.java` (`handleRefundCallback`)
- **问题**: 任何一笔退款成功就将订单标记为 `refunded`，不支持部分退款语义
- **修复方案**:
  - 退款成功后，检查该订单下所有退款单:
    - 若退款总额 == amountPaid → `refunded`
    - 若退款总额 < amountPaid → 恢复为 `paid`（或新增 `partially_refunded` 状态）
  - 退款失败时，检查是否还有其他进行中的退款:
    - 有 → 保持 `refunding`
    - 无 → 恢复为 `paid`
- [ ] 未开始

---

## P1 — 重要问题（强烈建议修复）

### ✅ FIX-04: 订单号生成存在碰撞风险
- **文件**: `PaymentService.java` (`generateOrderNo`, `generateRefundNo`)
- **问题**: `System.nanoTime() % 100000` 只有 5 位随机数，高并发下可能碰撞
- **修复方案**: 使用 UUID 或数据库序列确保全局唯一，格式保持 `PO` / `RF` 前缀 + 时间戳 + 充分随机后缀
- [ ] 未开始

### ✅ FIX-05: 缺少并发控制/幂等保护
- **文件**: `PaymentService.java` (`handleWechatCallback`, `handleRefundCallback`)
- **问题**: 重复回调可能导致并发状态不一致
- **修复方案**:
  - 回调处理加 Redis 分布式锁（key: `pay:callback:{orderNo}`）
  - 或使用数据库乐观锁（pay_order 表加 version 字段）
- [ ] 未开始

### ✅ FIX-06: 缺少订单超时关闭机制
- **文件**: 新增 `PaymentScheduler.java`
- **问题**: 有 `expiredAt` 字段但无定时任务关闭过期订单
- **修复方案**:
  - 新增定时任务，每 5 分钟扫描 `status=paying` 且 `expired_at < now` 的订单
  - 调用微信关单接口 `POST /v3/pay/transactions/out-trade-no/{out_trade_no}/close`
  - 将订单状态更新为 `closed`
- [ ] 未开始

### ✅ FIX-07: RestTemplate 没有超时配置
- **文件**: `RealWechatPayGateway.java`
- **问题**: 默认无超时，微信 API 不可达时线程无限阻塞
- **修复方案**: 注入配置了 connectTimeout(5s) + readTimeout(30s) 的 RestTemplate Bean
- [ ] 未开始

### ✅ FIX-08: 管理员手动改状态无审计保护
- **文件**: `PaymentService.java` (`updateAdminPayOrderStatus`)
- **问题**: 管理员可直接将订单改为 `paid`，无二次确认
- **修复方案**:
  - 手动改为 `paid` 时要求填写 `externalTransactionNo`（外部交易凭证号）
  - 记录操作人信息（需要从 SecurityContext 获取当前用户）
  - 在 transaction log 中记录 `admin_status_update` 的操作人
- [ ] 未开始

---

## P2 — 中等问题（建议修复）

### ✅ FIX-09: 回调解密遍历所有商户配置
- **文件**: `WechatPayCallbackParser.java` (`resolveMerchantForNotify`)
- **问题**: mchId 匹配不上时遍历所有商户逐一尝试验签，性能差且有时序攻击风险
- **修复方案**: 优先用 body 中的 `mchid` 精确匹配商户，匹配失败时再尝试逐个解密（保留回退但记录警告日志）
- [ ] 未开始

### ✅ FIX-10: 敏感信息明文存储
- **文件**: `PaymentMerchantService.java`, `WechatPayCryptoService.java`
- **问题**: apiV3Key、appSecret 明文存储在数据库
- **修复方案**: 使用 AES 加密存储，读取时解密（可先用应用配置中的 master key）
- [ ] 未开始

### ✅ FIX-11: 退款通知 URL 拼接逻辑脆弱
- **文件**: `RealWechatPayGateway.java` (`resolveRefundNotifyUrl`)
- **问题**: 只在 URL 以 `/wechat` 结尾时才拼接退款 URL
- **修复方案**: 增加专门的退款通知 URL 配置字段 `refundNotifyUrl`，或改为基于支付 URL 自动推导的更健壮逻辑
- [ ] 未开始

---

## 执行顺序

```
FIX-01 (验签) → FIX-02 (退款金额) → FIX-03 (退款状态) → FIX-04 (订单号) 
→ FIX-05 (并发控制) → FIX-07 (超时) → FIX-06 (超时关单) 
→ FIX-08 (管理员审计) → FIX-09 (商户匹配) → FIX-11 (退款URL) → FIX-10 (加密存储)
```
