package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.NotifyReplayResponse;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.entity.PayNotifyLog;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentNotifyReplayService {
    private final PayNotifyLogMapper payNotifyLogMapper;
    private final RefundNotifyLogMapper refundNotifyLogMapper;
    private final WechatPayCallbackParser wechatPayCallbackParser;
    private final PaymentService paymentService;

    public PaymentNotifyReplayService(
        PayNotifyLogMapper payNotifyLogMapper,
        RefundNotifyLogMapper refundNotifyLogMapper,
        WechatPayCallbackParser wechatPayCallbackParser,
        PaymentService paymentService
    ) {
        this.payNotifyLogMapper = payNotifyLogMapper;
        this.refundNotifyLogMapper = refundNotifyLogMapper;
        this.wechatPayCallbackParser = wechatPayCallbackParser;
        this.paymentService = paymentService;
    }

    @Transactional
    public NotifyReplayResponse replayPaymentNotify(Long logId) {
        PayNotifyLog log = payNotifyLogMapper.selectById(logId);
        if (log == null) {
            throw new NotFoundException("payment notify log not found");
        }
        if (log.getRawPayload() == null || log.getRawPayload().isBlank()) {
            throw new IllegalArgumentException("payment notify log raw payload is empty");
        }

        try {
            WechatPayCallbackRequest request = wechatPayCallbackParser.parsePaymentCallback(log.getRawPayload());
            paymentService.handleWechatCallback(request);
            return new NotifyReplayResponse(logId, "payment", "replayed", "payment callback replayed successfully");
        } catch (Exception ex) {
            paymentService.recordPaymentCallbackFailure(log.getRawPayload(), "replay_failed", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public NotifyReplayResponse replayRefundNotify(Long logId) {
        RefundNotifyLog log = refundNotifyLogMapper.selectById(logId);
        if (log == null) {
            throw new NotFoundException("refund notify log not found");
        }
        if (log.getRawPayload() == null || log.getRawPayload().isBlank()) {
            throw new IllegalArgumentException("refund notify log raw payload is empty");
        }

        try {
            RefundCallbackRequest request = wechatPayCallbackParser.parseRefundCallback(log.getRawPayload());
            paymentService.handleRefundCallback(request);
            return new NotifyReplayResponse(logId, "refund", "replayed", "refund callback replayed successfully");
        } catch (Exception ex) {
            paymentService.recordRefundCallbackFailure(log.getRawPayload(), "replay_failed", ex.getMessage());
            throw ex;
        }
    }
}
