package com.qsd.admin.payment;

import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.payment.dto.MemberPaymentPrepareRequest;
import com.qsd.admin.payment.dto.MemberPaymentPrepareResponse;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
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
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
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
            paymentReconcileService
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

        when(memberUserMapper.selectActiveById(7L)).thenReturn(member);
        when(waybillOrderMapper.selectAccessibleDetailByMember(15L, 7L, "13800138000")).thenReturn(waybill);
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
    void shouldTreatPaidWechatCallbackAsIdempotent() {
        PayOrder order = new PayOrder();
        order.setId(33L);
        order.setOrderNo("PO202605090001");
        order.setStatus("paid");
        order.setAmountTotal(new BigDecimal("99.00"));
        order.setAmountPaid(new BigDecimal("99.00"));

        when(payOrderMapper.selectByOrderNo("PO202605090001")).thenReturn(order);

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

        when(refundOrderMapper.selectByRefundNo("RF202605090001")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(66L)).thenReturn(order);

        paymentService.handleRefundCallback(
            new RefundCallbackRequest("RF202605090001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}")
        );

        ArgumentCaptor<RefundNotifyLog> refundNotifyLogCaptor = ArgumentCaptor.forClass(RefundNotifyLog.class);
        verify(refundNotifyLogMapper).insert(refundNotifyLogCaptor.capture());
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

        when(refundOrderMapper.selectByRefundNo("RF202605090002")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(88L)).thenReturn(order);

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
    void shouldRejectMemberPaymentPrepareWhenWechatIdentityIsMissing() {
        MemberUser member = new MemberUser();
        member.setId(7L);
        member.setPhone("13800138000");
        member.setStatus("active");
        member.setWechatOpenid(null);

        when(memberUserMapper.selectActiveById(7L)).thenReturn(member);

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> paymentService.prepareMemberPayment(
                7L,
                new MemberPaymentPrepareRequest(15L, new BigDecimal("88.50"), "Waybill payment", "wechat_pay")
            )
        );

        assertEquals("member wechat identity is not bound", ex.getMessage());
        verify(payOrderMapper, never()).insert(any(PayOrder.class));
    }

    @Test
    void shouldRejectPayCallbackWhenMerchantDoesNotMatchOrder() {
        PayOrder order = new PayOrder();
        order.setId(44L);
        order.setOrderNo("PO202605090099");
        order.setMerchantMchId("mch-expected");

        when(payOrderMapper.selectByOrderNo("PO202605090099")).thenReturn(order);

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

        when(refundOrderMapper.selectByRefundNo("RF202605090099")).thenReturn(refund);
        when(payOrderMapper.selectActiveById(89L)).thenReturn(order);

        WechatCallbackException ex = assertThrows(
            WechatCallbackException.class,
            () -> paymentService.validateRefundCallbackMerchant("RF202605090099", "mch-actual")
        );

        assertEquals("merchant_mismatch", ex.category());
        assertEquals("callback merchant does not match refund order merchant", ex.getMessage());
    }
}
