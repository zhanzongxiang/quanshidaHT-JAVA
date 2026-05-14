package com.qsd.admin.payment;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.NotifyReplayResponse;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.entity.PayNotifyLog;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.payment.service.PaymentNotifyReplayService;
import com.qsd.admin.payment.service.PaymentService;
import com.qsd.admin.payment.service.WechatPayCallbackParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentNotifyReplayServiceTest {

    @Mock
    private PayNotifyLogMapper payNotifyLogMapper;

    @Mock
    private RefundNotifyLogMapper refundNotifyLogMapper;

    @Mock
    private WechatPayCallbackParser wechatPayCallbackParser;

    @Mock
    private PaymentService paymentService;

    private PaymentNotifyReplayService paymentNotifyReplayService;

    @BeforeEach
    void setUp() {
        paymentNotifyReplayService = new PaymentNotifyReplayService(
            payNotifyLogMapper,
            refundNotifyLogMapper,
            wechatPayCallbackParser,
            paymentService
        );
    }

    @Test
    void shouldReplayPaymentNotifySuccessfully() {
        PayNotifyLog log = new PayNotifyLog();
        log.setId(10L);
        log.setRawPayload("{\"event\":\"pay\"}");

        WechatPayCallbackRequest request = new WechatPayCallbackRequest("PO202605130001", "wx-txn-001", "SUCCESS", "{\"event\":\"pay\"}");

        when(payNotifyLogMapper.selectById(10L)).thenReturn(log);
        when(wechatPayCallbackParser.parsePaymentCallback(log.getRawPayload())).thenReturn(request);
        doNothing().when(paymentService).handleWechatCallback(request);

        NotifyReplayResponse response = paymentNotifyReplayService.replayPaymentNotify(10L);

        assertEquals(10L, response.sourceLogId());
        assertEquals("payment", response.replayType());
        assertEquals("replayed", response.replayStatus());
        assertEquals("支付回调重放成功", response.message());
        verify(paymentService, never()).recordPaymentCallbackFailure(any(), any(), any());
    }

    @Test
    void shouldRejectPaymentReplayWhenLogDoesNotExist() {
        when(payNotifyLogMapper.selectById(10L)).thenReturn(null);

        NotFoundException ex = assertThrows(
            NotFoundException.class,
            () -> paymentNotifyReplayService.replayPaymentNotify(10L)
        );

        assertEquals("支付回调日志不存在", ex.getMessage());
        verify(wechatPayCallbackParser, never()).parsePaymentCallback(any());
    }

    @Test
    void shouldRejectRefundReplayWhenPayloadIsBlank() {
        RefundNotifyLog log = new RefundNotifyLog();
        log.setId(11L);
        log.setRawPayload("   ");

        when(refundNotifyLogMapper.selectById(11L)).thenReturn(log);

        BusinessException ex = assertThrows(
            BusinessException.class,
            () -> paymentNotifyReplayService.replayRefundNotify(11L)
        );

        assertEquals("退款回调日志缺少原始报文，无法重放", ex.getMessage());
        verify(wechatPayCallbackParser, never()).parseRefundCallback(any());
    }

    @Test
    void shouldRecordFailureWhenRefundReplayThrows() {
        RefundNotifyLog log = new RefundNotifyLog();
        log.setId(12L);
        log.setRawPayload("{\"event\":\"refund\"}");

        RefundCallbackRequest request = new RefundCallbackRequest("RF202605130001", "SUCCESS", "wx-rf-001", "{\"event\":\"refund\"}");

        when(refundNotifyLogMapper.selectById(12L)).thenReturn(log);
        when(wechatPayCallbackParser.parseRefundCallback(log.getRawPayload())).thenReturn(request);
        doThrow(new RuntimeException("refund replay failed")).when(paymentService).handleRefundCallback(request);

        RuntimeException ex = assertThrows(
            RuntimeException.class,
            () -> paymentNotifyReplayService.replayRefundNotify(12L)
        );

        assertEquals("refund replay failed", ex.getMessage());
        verify(paymentService).recordRefundCallbackFailure(
            eq(log.getRawPayload()),
            eq("replay_failed"),
            eq("退款回调重放失败")
        );
    }
}
