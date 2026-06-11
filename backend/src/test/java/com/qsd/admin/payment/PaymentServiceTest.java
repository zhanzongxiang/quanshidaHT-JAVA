package com.qsd.admin.payment;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.payment.dto.MemberPaymentPrepareRequest;
import com.qsd.admin.payment.dto.MemberPaymentPrepareResponse;
import com.qsd.admin.payment.dto.PaymentAdminCreateRequest;
import com.qsd.admin.payment.dto.PaymentAdminDetailResponse;
import com.qsd.admin.payment.dto.PaymentStatusUpdateRequest;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.RefundOrderResponse;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.dto.WechatRefundResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayNotifyLog;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.PayTransaction;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import com.qsd.admin.payment.entity.RefundOrder;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.PayTransactionMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.payment.mapper.RefundOrderMapper;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentReconcileService;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.payment.service.WechatCallbackException;
import com.qsd.admin.payment.service.WechatPayGateway;
import com.qsd.admin.tenant.TenantContext;
import com.qsd.admin.tenant.TenantContextHolder;
import com.qsd.admin.tenant.entity.Tenant;
import com.qsd.admin.tenant.mapper.TenantMapper;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    private static final long TENANT_ID = 1L;

    @Mock
    private PayOrderMapper payOrderMapper;

    @Mock
    private PayTransactionMapper payTransactionMapper;

    @Mock
    private RefundOrderMapper refundOrderMapper;

    @Mock
    private RefundNotifyLogMapper refundNotifyLogMapper;

    @Mock
    private PayNotifyLogMapper payNotifyLogMapper;

    @Mock
    private PayReconcileRecordMapper payReconcileRecordMapper;

    @Mock
    private MemberUserMapper memberUserMapper;

    @Mock
    private WaybillOrderMapper waybillOrderMapper;

    @Mock
    private WechatPayGateway wechatPayGateway;

    @Mock
    private PaymentMerchantService paymentMerchantService;

    @Mock
    private PaymentReconcileService paymentReconcileService;

    @Mock
    private TenantMapper tenantMapper;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        TenantContextHolder.set(new TenantContext(TENANT_ID, "default", "Default Tenant"));
        paymentService = new PaymentService(
            payOrderMapper,
            payTransactionMapper,
            refundOrderMapper,
            refundNotifyLogMapper,
            payNotifyLogMapper,
            payReconcileRecordMapper,
            memberUserMapper,
            waybillOrderMapper,
            wechatPayGateway,
            paymentMerchantService,
            paymentReconcileService,
            tenantMapper
        );
    }

    @Test
    void shouldPrepareMemberPaymentAndPersistOrderSnapshot() {
        MemberUser member = new MemberUser();
        member.setId(7L);
        member.setPhone("13800138000");
        member.setWechatOpenid("openid-001");
        member.setStatus("active");

        WaybillOrder waybill = new WaybillOrder();
        waybill.setId(15L);
        waybill.setMainTrackingNo("WB202605090001");

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(9L);
        merchant.setMerchantName("Acme Merchant");
        merchant.setMchId("mch-001");
        merchant.setAppId("wx-app-001");

        WechatMiniProgramPayParams payParams = new WechatMiniProgramPayParams(
            "wx-app-001",
            "1710000000",
            "nonce-123",
            "prepay_id=wx-prepay-001",
            "RSA",
            "sign-123",
            "wx-prepay-001",
            "wx-txn-001"
        );

        when(memberUserMapper.selectActiveById(TENANT_ID, 7L)).thenReturn(member);
        when(waybillOrderMapper.selectAccessibleDetailByMember(TENANT_ID, 15L, 7L, "13800138000")).thenReturn(waybill);
        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.prepareMiniProgramPayment(any(PayOrder.class), eq("openid-001"), eq(merchant))).thenReturn(payParams);

        doAnswer(invocation -> {
            PayOrder order = invocation.getArgument(0);
            order.setId(101L);
            return 1;
        }).when(payOrderMapper).insert(any(PayOrder.class));

        MemberPaymentPrepareResponse response = paymentService.prepareMemberPayment(
            7L,
            new MemberPaymentPrepareRequest(15L, new BigDecimal("88.50"), "Waybill payment", "wechat_pay")
        );

        ArgumentCaptor<PayOrder> insertedOrderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).insert(insertedOrderCaptor.capture());
        PayOrder insertedOrder = insertedOrderCaptor.getValue();
        assertEquals(TENANT_ID, insertedOrder.getTenantId());
        assertEquals(7L, insertedOrder.getMemberId());
        assertEquals(15L, insertedOrder.getWaybillId());
        assertEquals(9L, insertedOrder.getMerchantConfigId());
        assertEquals("Acme Merchant", insertedOrder.getMerchantName());
        assertEquals("mch-001", insertedOrder.getMerchantMchId());
        assertEquals("wx-app-001", insertedOrder.getMerchantAppId());
        assertEquals("paying", insertedOrder.getStatus());
        assertEquals(new BigDecimal("88.50"), insertedOrder.getAmountTotal());

        ArgumentCaptor<PayOrder> updatedOrderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(updatedOrderCaptor.capture());
        assertEquals("wx-txn-001", updatedOrderCaptor.getValue().getExternalTransactionNo());

        ArgumentCaptor<PayTransaction> transactionCaptor = ArgumentCaptor.forClass(PayTransaction.class);
        verify(payTransactionMapper).insert(transactionCaptor.capture());
        PayTransaction transaction = transactionCaptor.getValue();
        assertEquals(TENANT_ID, transaction.getTenantId());
        assertEquals(101L, transaction.getPayOrderId());
        assertEquals("mini_program_prepare", transaction.getTransactionType());
        assertEquals("paying", transaction.getTransactionStatus());
        assertEquals("wx-txn-001", transaction.getExternalTransactionNo());

        assertEquals(101L, response.payOrderId());
        assertEquals("wx-app-001", response.appId());
        assertEquals("nonce-123", response.nonceStr());
        assertEquals("prepay_id=wx-prepay-001", response.packageValue());
        assertEquals("sign-123", response.paySign());
    }

    @Test
    void shouldCreateAdminPayOrderWithMerchantSnapshotAndTransactionLog() {
        MemberUser member = new MemberUser();
        member.setId(8L);
        member.setPhone("13900139000");

        WaybillOrder waybill = new WaybillOrder();
        waybill.setId(18L);
        waybill.setMainTrackingNo("WB202606100001");

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(12L);
        merchant.setMerchantName("Default Merchant");
        merchant.setMchId("mch-admin-001");
        merchant.setAppId("wx-admin-001");
        merchant.setEnabled(1);

        PayOrder persistedOrder = new PayOrder();
        persistedOrder.setId(201L);
        persistedOrder.setTenantId(TENANT_ID);
        persistedOrder.setOrderNo("PO202606100001");
        persistedOrder.setMemberId(8L);
        persistedOrder.setWaybillId(18L);
        persistedOrder.setMerchantConfigId(12L);
        persistedOrder.setMerchantName("Default Merchant");
        persistedOrder.setMerchantMchId("mch-admin-001");
        persistedOrder.setMerchantAppId("wx-admin-001");
        persistedOrder.setBusinessType("waybill");
        persistedOrder.setSceneType("admin_manual");
        persistedOrder.setChannel("wechat_pay");
        persistedOrder.setCurrency("CNY");
        persistedOrder.setAmountTotal(new BigDecimal("199.90"));
        persistedOrder.setAmountPaid(BigDecimal.ZERO);
        persistedOrder.setStatus("pending");
        persistedOrder.setDescription("Manual order");
        persistedOrder.setRemark("ops");

        when(memberUserMapper.selectActiveById(TENANT_ID, 8L)).thenReturn(member);
        when(waybillOrderMapper.selectActiveById(TENANT_ID, 18L)).thenReturn(waybill);
        when(paymentMerchantService.requireMerchantById(12L)).thenReturn(merchant);
        doAnswer(invocation -> {
            PayOrder order = invocation.getArgument(0);
            order.setId(201L);
            order.setOrderNo("PO202606100001");
            return 1;
        }).when(payOrderMapper).insert(any(PayOrder.class));
        when(payOrderMapper.selectActiveById(TENANT_ID, 201L)).thenReturn(persistedOrder);
        when(memberUserMapper.selectActiveById(TENANT_ID, persistedOrder.getMemberId())).thenReturn(member);
        when(waybillOrderMapper.selectActiveById(TENANT_ID, persistedOrder.getWaybillId())).thenReturn(waybill);
        when(payTransactionMapper.selectByPayOrderId(TENANT_ID, 201L)).thenReturn(List.of());
        when(refundOrderMapper.selectByPayOrderId(TENANT_ID, 201L)).thenReturn(List.of());
        when(payNotifyLogMapper.selectByPayOrderId(TENANT_ID, 201L)).thenReturn(List.of());

        PaymentAdminDetailResponse response = paymentService.createAdminPayOrder(
            new PaymentAdminCreateRequest(
                8L,
                12L,
                18L,
                "waybill",
                "admin_manual",
                "wechat_pay",
                "CNY",
                new BigDecimal("199.90"),
                "Manual order",
                "ops"
            )
        );

        ArgumentCaptor<PayOrder> insertedOrderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).insert(insertedOrderCaptor.capture());
        PayOrder insertedOrder = insertedOrderCaptor.getValue();
        assertEquals(TENANT_ID, insertedOrder.getTenantId());
        assertEquals(8L, insertedOrder.getMemberId());
        assertEquals(18L, insertedOrder.getWaybillId());
        assertEquals(12L, insertedOrder.getMerchantConfigId());
        assertEquals("Default Merchant", insertedOrder.getMerchantName());
        assertEquals("mch-admin-001", insertedOrder.getMerchantMchId());
        assertEquals("wx-admin-001", insertedOrder.getMerchantAppId());
        assertEquals("pending", insertedOrder.getStatus());
        assertEquals(BigDecimal.ZERO, insertedOrder.getAmountPaid());

        ArgumentCaptor<PayTransaction> transactionCaptor = ArgumentCaptor.forClass(PayTransaction.class);
        verify(payTransactionMapper).insert(transactionCaptor.capture());
        PayTransaction transaction = transactionCaptor.getValue();
        assertEquals(TENANT_ID, transaction.getTenantId());
        assertEquals(201L, transaction.getPayOrderId());
        assertEquals("manual_create", transaction.getTransactionType());
        assertEquals("created", transaction.getTransactionStatus());
        assertEquals("PO202606100001", transaction.getExternalOutTradeNo());

        assertEquals(201L, response.id());
        assertEquals("Default Merchant", response.merchantName());
        assertEquals("WB202606100001", response.waybillTrackingNo());
    }

    @Test
    void shouldTreatPaidWechatCallbackAsIdempotent() {
        PayOrder order = new PayOrder();
        order.setId(33L);
        order.setOrderNo("PO202605090001");
        order.setStatus("paid");
        order.setAmountTotal(new BigDecimal("99.00"));
        order.setAmountPaid(new BigDecimal("99.00"));

        when(payOrderMapper.selectByOrderNo(TENANT_ID, "PO202605090001")).thenReturn(order);

        paymentService.handleWechatCallback(
            new WechatPayCallbackRequest("PO202605090001", "wx-txn-001", "SUCCESS", "{\"event\":\"pay\"}")
        );

        ArgumentCaptor<PayNotifyLog> notifyLogCaptor = ArgumentCaptor.forClass(PayNotifyLog.class);
        verify(payNotifyLogMapper).insert(notifyLogCaptor.capture());
        PayNotifyLog notifyLog = notifyLogCaptor.getValue();
        assertEquals(33L, notifyLog.getPayOrderId());
        assertEquals("wechat_pay", notifyLog.getNotifyType());
        assertEquals("SUCCESS", notifyLog.getNotifyStatus());

        verify(payOrderMapper, never()).updateById(any(PayOrder.class));
        verify(payTransactionMapper, never()).insert(any(PayTransaction.class));
    }

    @Test
    void shouldMarkWechatOrderClosedWhenCallbackStatusIsClosed() {
        PayOrder order = new PayOrder();
        order.setId(41L);
        order.setOrderNo("PO202606100041");
        order.setStatus("paying");
        order.setAmountTotal(new BigDecimal("39.90"));

        when(payOrderMapper.selectByOrderNo(TENANT_ID, "PO202606100041")).thenReturn(order);

        paymentService.handleWechatCallback(
            new WechatPayCallbackRequest("PO202606100041", "wx-txn-close", "CLOSED", "{\"event\":\"close\"}")
        );

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        assertEquals("closed", orderCaptor.getValue().getStatus());
        assertNotNull(orderCaptor.getValue().getClosedAt());

        ArgumentCaptor<PayTransaction> transactionCaptor = ArgumentCaptor.forClass(PayTransaction.class);
        verify(payTransactionMapper).insert(transactionCaptor.capture());
        assertEquals("wechat_callback", transactionCaptor.getValue().getTransactionType());
        assertEquals("closed", transactionCaptor.getValue().getTransactionStatus());
    }

    @Test
    void shouldMarkWechatOrderExceptionWhenCallbackStatusIsUnexpected() {
        PayOrder order = new PayOrder();
        order.setId(42L);
        order.setOrderNo("PO202606100042");
        order.setStatus("paying");
        order.setAmountTotal(new BigDecimal("49.90"));

        when(payOrderMapper.selectByOrderNo(TENANT_ID, "PO202606100042")).thenReturn(order);

        paymentService.handleWechatCallback(
            new WechatPayCallbackRequest("PO202606100042", "wx-txn-fail", "FAILED", "{\"event\":\"fail\"}")
        );

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        assertEquals("exception", orderCaptor.getValue().getStatus());

        ArgumentCaptor<PayTransaction> transactionCaptor = ArgumentCaptor.forClass(PayTransaction.class);
        verify(payTransactionMapper).insert(transactionCaptor.capture());
        assertEquals("exception", transactionCaptor.getValue().getTransactionStatus());
    }

    @Test
    void shouldMarkRefundAndOrderAsSucceededOnSuccessfulRefundCallback() {
        RefundOrder refund = new RefundOrder();
        refund.setId(55L);
        refund.setRefundNo("RF202605090001");
        refund.setPayOrderId(66L);
        refund.setStatus("processing");
        refund.setExternalRefundNo("wx-rf-old");

        PayOrder order = new PayOrder();
        order.setId(66L);
        order.setStatus("refunding");
        order.setAmountPaid(new BigDecimal("99.00"));

        when(refundOrderMapper.selectByRefundNo(TENANT_ID, "RF202605090001")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 66L)).thenReturn(order);
        when(refundOrderMapper.sumSucceededAmountByPayOrderId(TENANT_ID, 66L)).thenReturn(new BigDecimal("99.00"));

        paymentService.handleRefundCallback(
            new RefundCallbackRequest("RF202605090001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}")
        );

        ArgumentCaptor<RefundNotifyLog> refundNotifyLogCaptor = ArgumentCaptor.forClass(RefundNotifyLog.class);
        verify(refundNotifyLogMapper).insert(refundNotifyLogCaptor.capture());
        assertEquals(TENANT_ID, refundNotifyLogCaptor.getValue().getTenantId());
        assertEquals(55L, refundNotifyLogCaptor.getValue().getRefundOrderId());
        assertEquals("wechat_refund", refundNotifyLogCaptor.getValue().getNotifyType());

        ArgumentCaptor<RefundOrder> refundCaptor = ArgumentCaptor.forClass(RefundOrder.class);
        verify(refundOrderMapper).updateById(refundCaptor.capture());
        RefundOrder updatedRefund = refundCaptor.getValue();
        assertEquals("succeeded", updatedRefund.getStatus());
        assertEquals("wx-rf-001", updatedRefund.getExternalRefundNo());
        assertNotNull(updatedRefund.getRefundedAt());

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        PayOrder updatedOrder = orderCaptor.getValue();
        assertEquals("refunded", updatedOrder.getStatus());
        assertNotNull(updatedOrder.getRefundedAt());
    }

    @Test
    void shouldRestoreOrderToPaidOnFailedRefundCallback() {
        RefundOrder refund = new RefundOrder();
        refund.setId(77L);
        refund.setRefundNo("RF202605090002");
        refund.setPayOrderId(88L);
        refund.setStatus("processing");

        PayOrder order = new PayOrder();
        order.setId(88L);
        order.setStatus("refunding");

        when(refundOrderMapper.selectByRefundNo(TENANT_ID, "RF202605090002")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 88L)).thenReturn(order);
        when(refundOrderMapper.countProcessingByPayOrderId(TENANT_ID, 88L)).thenReturn(0);

        paymentService.handleRefundCallback(
            new RefundCallbackRequest("RF202605090002", "FAILED", "wx-rf-002", "{\"event\":\"refund\"}")
        );

        ArgumentCaptor<RefundOrder> refundCaptor = ArgumentCaptor.forClass(RefundOrder.class);
        verify(refundOrderMapper).updateById(refundCaptor.capture());
        RefundOrder updatedRefund = refundCaptor.getValue();
        assertEquals("failed", updatedRefund.getStatus());
        assertEquals("wx-rf-002", updatedRefund.getExternalRefundNo());
        assertNull(updatedRefund.getRefundedAt());

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        assertEquals("paid", orderCaptor.getValue().getStatus());
        assertTrue(orderCaptor.getValue().getUpdatedAt() != null);
    }

    @Test
    void shouldKeepOrderRefundingWhenFailedRefundCallbackStillHasProcessingRefunds() {
        RefundOrder refund = new RefundOrder();
        refund.setId(79L);
        refund.setRefundNo("RF202606100079");
        refund.setPayOrderId(89L);
        refund.setStatus("processing");

        PayOrder order = new PayOrder();
        order.setId(89L);
        order.setStatus("refunding");

        when(refundOrderMapper.selectByRefundNo(TENANT_ID, "RF202606100079")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 89L)).thenReturn(order);
        when(refundOrderMapper.countProcessingByPayOrderId(TENANT_ID, 89L)).thenReturn(2);

        paymentService.handleRefundCallback(
            new RefundCallbackRequest("RF202606100079", "FAILED", "wx-rf-079", "{\"event\":\"refund\"}")
        );

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        assertEquals("refunding", orderCaptor.getValue().getStatus());
    }

    @Test
    void shouldRejectMemberPaymentPrepareWhenWechatIdentityIsMissing() {
        MemberUser member = new MemberUser();
        member.setId(7L);
        member.setPhone("13800138000");
        member.setStatus("active");
        member.setWechatOpenid(null);

        when(memberUserMapper.selectActiveById(TENANT_ID, 7L)).thenReturn(member);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentService.prepareMemberPayment(
                7L,
                new MemberPaymentPrepareRequest(15L, new BigDecimal("88.50"), "Waybill payment", "wechat_pay")
            )
        );

        assertEquals("当前会员未绑定微信身份，无法发起小程序支付", ex.getMessage());
        verify(payOrderMapper, never()).insert(any(PayOrder.class));
    }

    @Test
    void shouldRejectMemberPaymentPrepareWhenWaybillIsNotAccessible() {
        MemberUser member = new MemberUser();
        member.setId(7L);
        member.setPhone("13800138000");
        member.setStatus("active");
        member.setWechatOpenid("openid-001");

        when(memberUserMapper.selectActiveById(TENANT_ID, 7L)).thenReturn(member);
        when(waybillOrderMapper.selectAccessibleDetailByMember(TENANT_ID, 15L, 7L, "13800138000")).thenReturn(null);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> paymentService.prepareMemberPayment(
                7L,
                new MemberPaymentPrepareRequest(15L, new BigDecimal("88.50"), "Waybill payment", "wechat_pay")
            )
        );

        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
        verify(paymentMerchantService, never()).requireCurrentMerchant();
        verify(payOrderMapper, never()).insert(any(PayOrder.class));
    }

    @Test
    void shouldRejectRefundWhenAmountExceedsPaidAmount() {
        PayOrder order = new PayOrder();
        order.setId(66L);
        order.setStatus("paid");
        order.setAmountPaid(new BigDecimal("19.90"));

        when(payOrderMapper.selectActiveById(TENANT_ID, 66L)).thenReturn(order);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentService.createRefund(
                66L,
                new com.qsd.admin.payment.dto.RefundCreateRequest(new BigDecimal("20.00"), "customer request")
            )
        );

        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
        verify(wechatPayGateway, never()).createRefund(any(PayOrder.class), any(RefundOrder.class), any(PayMerchantConfig.class));
        verify(refundOrderMapper, never()).insert(any(RefundOrder.class));
    }

    @Test
    void shouldRejectRetryRefundWhenSourceRefundIsNotFailed() {
        RefundOrder refund = new RefundOrder();
        refund.setId(77L);
        refund.setRefundNo("RF202605090003");
        refund.setPayOrderId(88L);
        refund.setStatus("processing");

        when(refundOrderMapper.selectByIdValue(TENANT_ID, 77L)).thenReturn(refund);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentService.retryRefund(77L)
        );

        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
        verify(payOrderMapper, never()).selectActiveById(eq(TENANT_ID), any(Long.class));
        verify(wechatPayGateway, never()).createRefund(any(PayOrder.class), any(RefundOrder.class), any(PayMerchantConfig.class));
    }

    @Test
    void shouldRejectRetryRefundWhenPayOrderStatusIsInvalid() {
        RefundOrder refund = new RefundOrder();
        refund.setId(78L);
        refund.setRefundNo("RF202605090004");
        refund.setPayOrderId(89L);
        refund.setStatus("failed");
        refund.setAmountRefund(new BigDecimal("9.90"));

        PayOrder order = new PayOrder();
        order.setId(89L);
        order.setStatus("closed");

        when(refundOrderMapper.selectByIdValue(TENANT_ID, 78L)).thenReturn(refund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 89L)).thenReturn(order);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentService.retryRefund(78L)
        );

        assertTrue(ex.getMessage() != null && !ex.getMessage().isBlank());
        verify(wechatPayGateway, never()).createRefund(any(PayOrder.class), any(RefundOrder.class), any(PayMerchantConfig.class));
        verify(refundOrderMapper, never()).insert(any(RefundOrder.class));
    }

    @Test
    void shouldResetPaidFieldsWhenAdminClosesPayment() {
        PayOrder order = new PayOrder();
        order.setId(90L);
        order.setOrderNo("PO202605090123");
        order.setStatus("paid");
        order.setAmountTotal(new BigDecimal("19.90"));
        order.setAmountPaid(new BigDecimal("19.90"));
        order.setExternalTransactionNo("wx-old-001");
        order.setPaidAt(java.time.LocalDateTime.of(2026, 5, 9, 10, 0));
        order.setRefundedAt(java.time.LocalDateTime.of(2026, 5, 9, 11, 0));

        when(payOrderMapper.selectActiveById(TENANT_ID, 90L)).thenReturn(order);

        paymentService.updateAdminPayOrderStatus(90L, new PaymentStatusUpdateRequest("closed", ""));

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        PayOrder updatedOrder = orderCaptor.getValue();
        assertEquals("closed", updatedOrder.getStatus());
        assertEquals(BigDecimal.ZERO, updatedOrder.getAmountPaid());
        assertNull(updatedOrder.getPaidAt());
        assertNull(updatedOrder.getRefundedAt());
        assertNotNull(updatedOrder.getClosedAt());
        assertEquals("", updatedOrder.getExternalTransactionNo());
    }

    @Test
    void shouldCreateRetryRefundFromFailedRefund() {
        RefundOrder failedRefund = new RefundOrder();
        failedRefund.setId(91L);
        failedRefund.setRefundNo("RF202605090005");
        failedRefund.setPayOrderId(92L);
        failedRefund.setStatus("failed");
        failedRefund.setAmountRefund(new BigDecimal("9.90"));
        failedRefund.setReason("manual retry");

        PayOrder order = new PayOrder();
        order.setId(92L);
        order.setOrderNo("PO202605090124");
        order.setStatus("paid");
        order.setExternalTransactionNo("wx-txn-009");
        order.setMerchantConfigId(13L);

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setId(13L);
        merchant.setMerchantName("Acme Merchant");

        WechatRefundResult refundResult = new WechatRefundResult("wx-rf-009", "processing", "{\"status\":\"processing\"}");

        when(refundOrderMapper.selectByIdValue(TENANT_ID, 91L)).thenReturn(failedRefund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 92L)).thenReturn(order);
        when(paymentMerchantService.requireMerchantById(13L)).thenReturn(merchant);
        when(wechatPayGateway.createRefund(eq(order), any(RefundOrder.class), eq(merchant))).thenReturn(refundResult);

        RefundOrderResponse response = paymentService.retryRefund(91L);

        ArgumentCaptor<RefundOrder> refundCaptor = ArgumentCaptor.forClass(RefundOrder.class);
        verify(refundOrderMapper).insert(refundCaptor.capture());
        RefundOrder insertedRefund = refundCaptor.getValue();
        assertEquals(TENANT_ID, insertedRefund.getTenantId());
        assertEquals(92L, insertedRefund.getPayOrderId());
        assertEquals(new BigDecimal("9.90"), insertedRefund.getAmountRefund());
        assertEquals("processing", insertedRefund.getStatus());
        assertEquals("manual retry", insertedRefund.getReason());
        assertEquals("wx-rf-009", insertedRefund.getExternalRefundNo());

        ArgumentCaptor<PayOrder> orderCaptor = ArgumentCaptor.forClass(PayOrder.class);
        verify(payOrderMapper).updateById(orderCaptor.capture());
        assertEquals("refunding", orderCaptor.getValue().getStatus());

        ArgumentCaptor<PayTransaction> transactionCaptor = ArgumentCaptor.forClass(PayTransaction.class);
        verify(payTransactionMapper).insert(transactionCaptor.capture());
        assertEquals(TENANT_ID, transactionCaptor.getValue().getTenantId());
        assertEquals("refund_retry", transactionCaptor.getValue().getTransactionType());
        assertEquals("refunding", transactionCaptor.getValue().getTransactionStatus());
        assertEquals("wx-txn-009", transactionCaptor.getValue().getExternalTransactionNo());

        assertEquals("processing", response.status());
        assertEquals("manual retry", response.reason());
        assertEquals("wx-rf-009", response.externalRefundNo());
    }

    @Test
    void shouldRejectPayCallbackWhenMerchantDoesNotMatchOrder() {
        PayOrder order = new PayOrder();
        order.setId(44L);
        order.setOrderNo("PO202605090099");
        order.setMerchantMchId("mch-expected");

        when(payOrderMapper.selectByOrderNo(TENANT_ID, "PO202605090099")).thenReturn(order);

        WechatCallbackException ex = assertThrows(
            WechatCallbackException.class,
            () -> paymentService.validatePayCallbackMerchant("PO202605090099", "mch-actual")
        );

        assertEquals("merchant_mismatch", ex.category());
        assertEquals("callback merchant does not match payment order merchant", ex.getMessage());
    }

    @Test
    void shouldRejectRefundCallbackWhenMerchantDoesNotMatchOrder() {
        RefundOrder refund = new RefundOrder();
        refund.setId(78L);
        refund.setRefundNo("RF202605090099");
        refund.setPayOrderId(89L);

        PayOrder order = new PayOrder();
        order.setId(89L);
        order.setMerchantMchId("mch-expected");

        when(refundOrderMapper.selectByRefundNo(TENANT_ID, "RF202605090099")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(TENANT_ID, 89L)).thenReturn(order);

        WechatCallbackException ex = assertThrows(
            WechatCallbackException.class,
            () -> paymentService.validateRefundCallbackMerchant("RF202605090099", "mch-actual")
        );

        assertEquals("merchant_mismatch", ex.category());
        assertEquals("callback merchant does not match refund order merchant", ex.getMessage());
    }

    @Test
    void shouldRejectPayCallbackWhenMerchantMchIdIsMissing() {
        PayOrder order = new PayOrder();
        order.setId(45L);
        order.setOrderNo("PO202606100045");
        order.setMerchantMchId("mch-expected");

        when(payOrderMapper.selectByOrderNo(TENANT_ID, "PO202606100045")).thenReturn(order);

        WechatCallbackException ex = assertThrows(
            WechatCallbackException.class,
            () -> paymentService.validatePayCallbackMerchant("PO202606100045", " ")
        );

        assertEquals("merchant_missing", ex.category());
        assertEquals("callback merchant mch id is missing", ex.getMessage());
    }

    @Test
    void shouldRecordPaymentCallbackFailureWithDefaultTenantWhenContextIsMissing() {
        TenantContextHolder.clear();
        Tenant defaultTenant = new Tenant();
        defaultTenant.setId(9L);
        when(tenantMapper.selectDefaultTenant()).thenReturn(defaultTenant);

        paymentService.recordPaymentCallbackFailure(null, null, null);

        ArgumentCaptor<PayNotifyLog> logCaptor = ArgumentCaptor.forClass(PayNotifyLog.class);
        verify(payNotifyLogMapper).insert(logCaptor.capture());
        PayNotifyLog log = logCaptor.getValue();
        assertEquals(9L, log.getTenantId());
        assertEquals("callback_failed", log.getNotifyStatus());
        assertEquals("", log.getRawPayload());
        assertEquals("callback failed", log.getProcessResult());
    }

    @Test
    void shouldRecordRefundCallbackFailureWithDefaultTenantWhenContextIsMissing() {
        TenantContextHolder.clear();
        Tenant defaultTenant = new Tenant();
        defaultTenant.setId(10L);
        when(tenantMapper.selectDefaultTenant()).thenReturn(defaultTenant);

        paymentService.recordRefundCallbackFailure("", "", "");

        ArgumentCaptor<RefundNotifyLog> logCaptor = ArgumentCaptor.forClass(RefundNotifyLog.class);
        verify(refundNotifyLogMapper).insert(logCaptor.capture());
        RefundNotifyLog log = logCaptor.getValue();
        assertEquals(10L, log.getTenantId());
        assertEquals("callback_failed", log.getNotifyStatus());
        assertEquals("callback failed", log.getProcessResult());
    }
}
