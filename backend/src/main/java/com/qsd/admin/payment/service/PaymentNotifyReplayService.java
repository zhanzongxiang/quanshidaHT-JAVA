package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.BusinessException;
import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.payment.dto.NotifyReplayResponse;
import com.qsd.admin.payment.entity.PayNotifyLog;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;
import java.util.function.Function;

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
        return replayNotify(
            logId,
            payNotifyLogMapper::selectById,
            PayNotifyLog::getRawPayload,
            "支付回调日志不存在",
            "支付回调日志缺少原始报文，无法重放",
            payload -> paymentService.handleWechatCallback(wechatPayCallbackParser.parsePaymentCallback(payload)),
            payload -> paymentService.recordPaymentCallbackFailure(payload, "replay_failed", "支付回调重放失败"),
            "payment",
            "支付回调重放成功"
        );
    }

    @Transactional
    public NotifyReplayResponse replayRefundNotify(Long logId) {
        return replayNotify(
            logId,
            refundNotifyLogMapper::selectById,
            RefundNotifyLog::getRawPayload,
            "退款回调日志不存在",
            "退款回调日志缺少原始报文，无法重放",
            payload -> paymentService.handleRefundCallback(wechatPayCallbackParser.parseRefundCallback(payload)),
            payload -> paymentService.recordRefundCallbackFailure(payload, "replay_failed", "退款回调重放失败"),
            "refund",
            "退款回调重放成功"
        );
    }

    private <T> NotifyReplayResponse replayNotify(
        Long logId,
        Function<Long, T> logLoader,
        Function<T, String> payloadReader,
        String notFoundMessage,
        String emptyPayloadMessage,
        Consumer<String> replayAction,
        Consumer<String> failureRecorder,
        String replayType,
        String successMessage
    ) {
        T log = logLoader.apply(logId);
        if (log == null) {
            throw new NotFoundException(notFoundMessage);
        }

        String rawPayload = payloadReader.apply(log);
        if (rawPayload == null || rawPayload.isBlank()) {
            throw new BusinessException(emptyPayloadMessage);
        }

        try {
            replayAction.accept(rawPayload);
            return new NotifyReplayResponse(logId, replayType, "replayed", successMessage);
        } catch (Exception ex) {
            failureRecorder.accept(rawPayload);
            throw ex;
        }
    }
}
