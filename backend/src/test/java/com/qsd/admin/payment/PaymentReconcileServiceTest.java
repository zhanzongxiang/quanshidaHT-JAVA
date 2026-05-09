package com.qsd.admin.payment;

import com.qsd.admin.payment.dto.WechatReconcileDownloadResult;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import com.qsd.admin.payment.service.PaymentMerchantService;
import com.qsd.admin.payment.service.PaymentReconcileService;
import com.qsd.admin.payment.service.WechatPayGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentReconcileServiceTest {

    @Mock
    private PayOrderMapper payOrderMapper;

    @Mock
    private PaymentMerchantService paymentMerchantService;

    @Mock
    private WechatPayGateway wechatPayGateway;

    private PaymentReconcileService paymentReconcileService;

    @BeforeEach
    void setUp() {
        paymentReconcileService = new PaymentReconcileService(
            payOrderMapper,
            paymentMerchantService,
            wechatPayGateway
        );
    }

    @Test
    void shouldReturnMatchedWhenRemoteAndLocalBillsMatch() {
        LocalDate billDate = LocalDate.of(2026, 5, 8);

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setAppId("wx-app-001");
        merchant.setMchId("mch-001");

        PayOrder order = new PayOrder();
        order.setOrderNo("PO202605080001");
        order.setChannel("wechat_pay");
        order.setAmountTotal(new BigDecimal("100.00"));

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.downloadTradeBill(billDate, merchant)).thenReturn(
            new WechatReconcileDownloadResult(
                "2026-05-08",
                """
                `交易时间`,`公众账号ID`,`商户号`,`商户订单号`,`微信支付订单号`,`订单金额`,`申请退款金额`,`退款金额`,`订单状态`
                `2026-05-08 12:00:00`,`wx-app-001`,`mch-001`,`PO202605080001`,`wx-mock-001`,`10000`,`0`,`0`,`SUCCESS`
                """,
                "mock://trade-bill"
            )
        );
        when(payOrderMapper.selectByBillDate(billDate)).thenReturn(List.of(order));

        PayReconcileRecord record = paymentReconcileService.buildTradeBillReconcileRecord(billDate, "wechat_pay");

        assertEquals("matched", record.getReconcileStatus());
        assertEquals(0, record.getDiffCount());
        assertTrue(record.getSummary().contains("remoteCount=1"));
    }

    @Test
    void shouldDetectMissingLocalAndAmountMismatchDiffs() {
        LocalDate billDate = LocalDate.of(2026, 5, 8);

        PayMerchantConfig merchant = new PayMerchantConfig();
        merchant.setAppId("wx-app-001");
        merchant.setMchId("mch-001");

        PayOrder mismatchOrder = new PayOrder();
        mismatchOrder.setOrderNo("PO202605080001");
        mismatchOrder.setChannel("wechat_pay");
        mismatchOrder.setAmountTotal(new BigDecimal("100.00"));

        PayOrder localOnlyOrder = new PayOrder();
        localOnlyOrder.setOrderNo("PO202605080002");
        localOnlyOrder.setChannel("wechat_pay");
        localOnlyOrder.setAmountTotal(new BigDecimal("50.00"));

        when(paymentMerchantService.requireCurrentMerchant()).thenReturn(merchant);
        when(wechatPayGateway.downloadTradeBill(billDate, merchant)).thenReturn(
            new WechatReconcileDownloadResult(
                "2026-05-08",
                """
                `交易时间`,`公众账号ID`,`商户号`,`商户订单号`,`微信支付订单号`,`订单金额`,`申请退款金额`,`退款金额`,`订单状态`
                `2026-05-08 12:00:00`,`wx-app-001`,`mch-001`,`PO202605080001`,`wx-mock-001`,`9999`,`0`,`0`,`SUCCESS`
                `2026-05-08 12:05:00`,`wx-app-001`,`mch-001`,`PO202605080003`,`wx-mock-003`,`8800`,`0`,`0`,`SUCCESS`
                """,
                "mock://trade-bill"
            )
        );
        when(payOrderMapper.selectByBillDate(billDate)).thenReturn(List.of(mismatchOrder, localOnlyOrder));

        PayReconcileRecord record = paymentReconcileService.buildTradeBillReconcileRecord(billDate, "wechat_pay");

        assertEquals("diff_found", record.getReconcileStatus());
        assertEquals(3, record.getDiffCount());
        assertTrue(record.getSummary().contains("amount_mismatch:PO202605080001"));
        assertTrue(record.getSummary().contains("missing_local:PO202605080003"));
        assertTrue(record.getSummary().contains("missing_remote:PO202605080002"));
    }
}
