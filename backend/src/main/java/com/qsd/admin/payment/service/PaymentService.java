package com.qsd.admin.payment.service;

import com.qsd.admin.common.exception.NotFoundException;
import com.qsd.admin.member.entity.MemberUser;
import com.qsd.admin.member.mapper.MemberUserMapper;
import com.qsd.admin.payment.dto.MemberPaymentPrepareRequest;
import com.qsd.admin.payment.dto.MemberPaymentPrepareResponse;
import com.qsd.admin.payment.dto.MemberPayOrderSummaryResponse;
import com.qsd.admin.payment.dto.PaymentAdminCreateRequest;
import com.qsd.admin.payment.dto.PaymentAdminDetailResponse;
import com.qsd.admin.payment.dto.PaymentAdminSummaryResponse;
import com.qsd.admin.payment.dto.PaymentNotifyLogResponse;
import com.qsd.admin.payment.dto.PaymentStatusUpdateRequest;
import com.qsd.admin.payment.dto.PaymentTransactionResponse;
import com.qsd.admin.payment.dto.ReconcileCreateRequest;
import com.qsd.admin.payment.dto.ReconcileRecordResponse;
import com.qsd.admin.payment.dto.RefundCallbackRequest;
import com.qsd.admin.payment.dto.RefundCreateRequest;
import com.qsd.admin.payment.dto.RefundNotifyLogResponse;
import com.qsd.admin.payment.dto.RefundOrderResponse;
import com.qsd.admin.payment.dto.WechatMiniProgramPayParams;
import com.qsd.admin.payment.dto.WechatPayCallbackRequest;
import com.qsd.admin.payment.dto.WechatRefundResult;
import com.qsd.admin.payment.entity.PayNotifyLog;
import com.qsd.admin.payment.entity.PayMerchantConfig;
import com.qsd.admin.payment.entity.PayOrder;
import com.qsd.admin.payment.entity.PayReconcileRecord;
import com.qsd.admin.payment.entity.PayTransaction;
import com.qsd.admin.payment.entity.RefundOrder;
import com.qsd.admin.payment.entity.RefundNotifyLog;
import com.qsd.admin.payment.mapper.PayNotifyLogMapper;
import com.qsd.admin.payment.mapper.PayOrderMapper;
import com.qsd.admin.payment.mapper.PayReconcileRecordMapper;
import com.qsd.admin.payment.mapper.PayTransactionMapper;
import com.qsd.admin.payment.mapper.RefundNotifyLogMapper;
import com.qsd.admin.payment.mapper.RefundOrderMapper;
import com.qsd.admin.waybill.entity.WaybillOrder;
import com.qsd.admin.waybill.mapper.WaybillOrderMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PaymentService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<String> ALLOWED_STATUSES = List.of("pending", "paying", "paid", "refunding", "refunded", "closed", "exception");

    private final PayOrderMapper payOrderMapper;
    private final PayTransactionMapper payTransactionMapper;
    private final RefundOrderMapper refundOrderMapper;
    private final RefundNotifyLogMapper refundNotifyLogMapper;
    private final PayNotifyLogMapper payNotifyLogMapper;
    private final PayReconcileRecordMapper payReconcileRecordMapper;
    private final MemberUserMapper memberUserMapper;
    private final WaybillOrderMapper waybillOrderMapper;
    private final WechatPayGateway wechatPayGateway;
    private final PaymentMerchantService paymentMerchantService;
    private final PaymentReconcileService paymentReconcileService;

    public PaymentService(
        PayOrderMapper payOrderMapper,
        PayTransactionMapper payTransactionMapper,
        RefundOrderMapper refundOrderMapper,
        RefundNotifyLogMapper refundNotifyLogMapper,
        PayNotifyLogMapper payNotifyLogMapper,
        PayReconcileRecordMapper payReconcileRecordMapper,
        MemberUserMapper memberUserMapper,
        WaybillOrderMapper waybillOrderMapper,
        WechatPayGateway wechatPayGateway,
        PaymentMerchantService paymentMerchantService,
        PaymentReconcileService paymentReconcileService
    ) {
        this.payOrderMapper = payOrderMapper;
        this.payTransactionMapper = payTransactionMapper;
        this.refundOrderMapper = refundOrderMapper;
        this.refundNotifyLogMapper = refundNotifyLogMapper;
        this.payNotifyLogMapper = payNotifyLogMapper;
        this.payReconcileRecordMapper = payReconcileRecordMapper;
        this.memberUserMapper = memberUserMapper;
        this.waybillOrderMapper = waybillOrderMapper;
        this.wechatPayGateway = wechatPayGateway;
        this.paymentMerchantService = paymentMerchantService;
        this.paymentReconcileService = paymentReconcileService;
    }

    public List<PaymentAdminSummaryResponse> listAdminPayOrders(String keyword, String status, String channel) {
        return payOrderMapper.selectAdminList(trimToNull(keyword), trimToNull(status), trimToNull(channel)).stream()
            .map(this::toAdminSummary)
            .toList();
    }

    public PaymentAdminDetailResponse getAdminPayOrderDetail(Long id) {
        PayOrder order = requirePayOrder(id);
        return toAdminDetail(order);
    }

    @Transactional
    public PaymentAdminDetailResponse createAdminPayOrder(PaymentAdminCreateRequest request) {
        MemberUser member = memberUserMapper.selectActiveById(request.memberId());
        if (member == null) {
            throw new IllegalArgumentException("member not found");
        }

        WaybillOrder waybill = null;
        if (request.waybillId() != null) {
            waybill = waybillOrderMapper.selectActiveById(request.waybillId());
            if (waybill == null) {
                throw new IllegalArgumentException("waybill not found");
            }
        }

        PayMerchantConfig merchantConfig = resolveMerchantForAdminCreate(request.merchantConfigId());
        LocalDateTime now = LocalDateTime.now();
        PayOrder order = new PayOrder();
        order.setOrderNo(generateOrderNo(now));
        order.setMemberId(member.getId());
        applyMerchantSnapshot(order, merchantConfig);
        order.setWaybillId(waybill == null ? null : waybill.getId());
        order.setBusinessType(trimToDefault(request.businessType(), "waybill"));
        order.setSceneType(trimToDefault(request.sceneType(), "mini_program"));
        order.setChannel(trimToDefault(request.channel(), "wechat_pay"));
        order.setCurrency(trimToDefault(request.currency(), "CNY"));
        order.setAmountTotal(normalizeAmount(request.amountTotal()));
        order.setAmountPaid(BigDecimal.ZERO);
        order.setStatus("pending");
        order.setDescription(trimToEmpty(request.description()));
        order.setRemark(trimToEmpty(request.remark()));
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        payOrderMapper.insert(order);

        createTransactionLog(order.getId(), "manual_create", "created", "{}", "{\"source\":\"admin\"}", null, order.getOrderNo(), null);
        return toAdminDetail(requirePayOrder(order.getId()));
    }

    @Transactional
    public PaymentAdminDetailResponse updateAdminPayOrderStatus(Long id, PaymentStatusUpdateRequest request) {
        PayOrder order = requirePayOrder(id);
        String status = trimToNull(request.status());
        if (status == null || !ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("payment status is invalid");
        }

        order.setStatus(status);
        order.setExternalTransactionNo(trimToEmpty(request.externalTransactionNo()));
        order.setUpdatedAt(LocalDateTime.now());

        if ("paid".equals(status)) {
            order.setAmountPaid(order.getAmountTotal());
            order.setPaidAt(LocalDateTime.now());
        }
        if ("closed".equals(status)) {
            order.setClosedAt(LocalDateTime.now());
        }
        if ("refunded".equals(status)) {
            order.setRefundedAt(LocalDateTime.now());
        }

        payOrderMapper.updateById(order);
        createTransactionLog(
            order.getId(),
            "admin_status_update",
            status,
            "{}",
            "{\"status\":\"" + status + "\"}",
            trimToNull(request.externalTransactionNo()),
            order.getOrderNo(),
            "paid".equals(status) ? LocalDateTime.now() : null
        );

        return toAdminDetail(requirePayOrder(order.getId()));
    }

    public List<MemberPayOrderSummaryResponse> listMemberPayOrders(Long memberId) {
        MemberUser member = memberUserMapper.selectActiveById(memberId);
        if (member == null) {
            throw new NotFoundException("member not found");
        }

        return payOrderMapper.selectByMemberId(memberId).stream()
            .map(this::toMemberSummary)
            .toList();
    }

    @Transactional
    public MemberPaymentPrepareResponse prepareMemberPayment(Long memberId, MemberPaymentPrepareRequest request) {
        MemberUser member = memberUserMapper.selectActiveById(memberId);
        if (member == null) {
            throw new NotFoundException("member not found");
        }
        if (trimToNull(member.getWechatOpenid()) == null) {
            throw new IllegalArgumentException("member wechat identity is not bound");
        }

        WaybillOrder waybill = waybillOrderMapper.selectAccessibleDetailByMember(request.waybillId(), memberId, member.getPhone());
        if (waybill == null) {
            throw new NotFoundException("member waybill not found");
        }

        PayMerchantConfig merchantConfig = paymentMerchantService.requireCurrentMerchant();
        LocalDateTime now = LocalDateTime.now();
        PayOrder order = new PayOrder();
        order.setOrderNo(generateOrderNo(now));
        order.setMemberId(memberId);
        applyMerchantSnapshot(order, merchantConfig);
        order.setWaybillId(waybill.getId());
        order.setBusinessType("waybill");
        order.setSceneType("mini_program");
        order.setChannel(trimToDefault(request.channel(), "wechat_pay"));
        order.setCurrency("CNY");
        order.setAmountTotal(normalizeAmount(request.amountTotal()));
        order.setAmountPaid(BigDecimal.ZERO);
        order.setStatus("paying");
        order.setDescription(trimToDefault(request.description(), "Waybill payment " + waybill.getMainTrackingNo()));
        order.setRemark("");
        order.setDeleted(0);
        order.setCreatedAt(now);
        order.setUpdatedAt(now);
        order.setExpiredAt(now.plusMinutes(30));
        payOrderMapper.insert(order);

        WechatMiniProgramPayParams payParams = wechatPayGateway.prepareMiniProgramPayment(order, member.getWechatOpenid(), merchantConfig);
        order.setExternalTransactionNo(trimToEmpty(payParams.transactionNo()));
        payOrderMapper.updateById(order);

        createTransactionLog(
            order.getId(),
            "mini_program_prepare",
            "paying",
            "{\"memberId\":" + memberId + ",\"waybillId\":" + waybill.getId() + "}",
            "{\"prepayId\":\"" + payParams.prepayId() + "\"}",
            payParams.transactionNo(),
            order.getOrderNo(),
            null
        );

        return new MemberPaymentPrepareResponse(
            order.getId(),
            order.getOrderNo(),
            order.getStatus(),
            order.getMerchantConfigId(),
            safe(order.getMerchantName()),
            payParams.appId(),
            payParams.timeStamp(),
            payParams.nonceStr(),
            payParams.packageValue(),
            payParams.signType(),
            payParams.paySign()
        );
    }

    @Transactional
    public void handleWechatCallback(WechatPayCallbackRequest request) {
        PayOrder order = payOrderMapper.selectByOrderNo(request.orderNo());
        if (order == null) {
            throw new NotFoundException("payment order not found");
        }

        String callbackStatus = trimToDefault(request.status(), "paid");
        recordNotifyLog(order.getId(), "wechat_pay", request.transactionNo(), callbackStatus, trimToEmpty(request.payload()));

        if ("SUCCESS".equalsIgnoreCase(callbackStatus) || "paid".equalsIgnoreCase(callbackStatus)) {
            if ("paid".equals(order.getStatus())) {
                return;
            }
            order.setStatus("paid");
            order.setAmountPaid(order.getAmountTotal());
            order.setPaidAt(LocalDateTime.now());
            order.setExternalTransactionNo(trimToDefault(request.transactionNo(), order.getExternalTransactionNo()));
            order.setUpdatedAt(LocalDateTime.now());
            payOrderMapper.updateById(order);
            createTransactionLog(
                order.getId(),
                "wechat_callback",
                "paid",
                trimToEmpty(request.payload()),
                "{\"status\":\"paid\"}",
                request.transactionNo(),
                order.getOrderNo(),
                LocalDateTime.now()
            );
            return;
        }

        if ("CLOSED".equalsIgnoreCase(callbackStatus) || "closed".equalsIgnoreCase(callbackStatus)) {
            if ("closed".equals(order.getStatus())) {
                return;
            }
            order.setStatus("closed");
            order.setClosedAt(LocalDateTime.now());
            order.setUpdatedAt(LocalDateTime.now());
            payOrderMapper.updateById(order);
            createTransactionLog(
                order.getId(),
                "wechat_callback",
                "closed",
                trimToEmpty(request.payload()),
                "{\"status\":\"closed\"}",
                request.transactionNo(),
                order.getOrderNo(),
                null
            );
            return;
        }

        order.setStatus("exception");
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.updateById(order);
        createTransactionLog(
            order.getId(),
            "wechat_callback",
            "exception",
            trimToEmpty(request.payload()),
            "{\"status\":\"exception\"}",
            request.transactionNo(),
            order.getOrderNo(),
            null
        );
    }

    @Transactional
    public RefundOrderResponse createRefund(Long payOrderId, RefundCreateRequest request) {
        PayOrder order = requirePayOrder(payOrderId);
        if (!"paid".equals(order.getStatus()) && !"refunding".equals(order.getStatus())) {
            throw new IllegalArgumentException("only paid order can be refunded");
        }
        if (request.amountRefund().compareTo(order.getAmountPaid()) > 0) {
            throw new IllegalArgumentException("refund amount cannot exceed paid amount");
        }

        LocalDateTime now = LocalDateTime.now();
        RefundOrder refund = new RefundOrder();
        refund.setRefundNo(generateRefundNo(now));
        refund.setPayOrderId(order.getId());
        refund.setAmountRefund(normalizeAmount(request.amountRefund()));
        refund.setStatus("processing");
        refund.setReason(trimToEmpty(request.reason()));
        refund.setCreatedAt(now);
        refund.setUpdatedAt(now);

        PayMerchantConfig merchantConfig = requireMerchantForOrder(order);
        WechatRefundResult refundResult = wechatPayGateway.createRefund(order, refund, merchantConfig);
        refund.setExternalRefundNo(trimToDefault(refundResult.externalRefundNo(), "mock-refund-" + order.getOrderNo()));
        refundOrderMapper.insert(refund);

        order.setStatus("refunding");
        order.setUpdatedAt(now);
        payOrderMapper.updateById(order);
        createTransactionLog(
            order.getId(),
            "refund_apply",
            "refunding",
            "{\"refundNo\":\"" + refund.getRefundNo() + "\"}",
            trimToDefault(refundResult.rawResponse(), "{\"status\":\"processing\"}"),
            order.getExternalTransactionNo(),
            order.getOrderNo(),
            null
        );

        return toRefundResponse(refund);
    }

    @Transactional
    public RefundOrderResponse retryRefund(Long refundOrderId) {
        RefundOrder failedRefund = refundOrderMapper.selectByIdValue(refundOrderId);
        if (failedRefund == null) {
            throw new NotFoundException("refund order not found");
        }
        if (!"failed".equals(failedRefund.getStatus())) {
            throw new IllegalArgumentException("only failed refund can be retried");
        }

        PayOrder order = requirePayOrder(failedRefund.getPayOrderId());
        if (!"paid".equals(order.getStatus()) && !"refunding".equals(order.getStatus()) && !"refunded".equals(order.getStatus())) {
            throw new IllegalArgumentException("payment order status does not allow refund retry");
        }

        LocalDateTime now = LocalDateTime.now();
        RefundOrder retryRefund = new RefundOrder();
        retryRefund.setRefundNo(generateRefundNo(now));
        retryRefund.setPayOrderId(order.getId());
        retryRefund.setAmountRefund(normalizeAmount(failedRefund.getAmountRefund()));
        retryRefund.setStatus("processing");
        retryRefund.setReason(trimToEmpty(failedRefund.getReason()));
        retryRefund.setCreatedAt(now);
        retryRefund.setUpdatedAt(now);

        PayMerchantConfig merchantConfig = requireMerchantForOrder(order);
        WechatRefundResult refundResult = wechatPayGateway.createRefund(order, retryRefund, merchantConfig);
        retryRefund.setExternalRefundNo(trimToDefault(refundResult.externalRefundNo(), "mock-refund-" + retryRefund.getRefundNo()));
        refundOrderMapper.insert(retryRefund);

        order.setStatus("refunding");
        order.setUpdatedAt(now);
        payOrderMapper.updateById(order);
        createTransactionLog(
            order.getId(),
            "refund_retry",
            "refunding",
            "{\"retryFromRefundNo\":\"" + failedRefund.getRefundNo() + "\"}",
            trimToDefault(refundResult.rawResponse(), "{\"status\":\"processing\"}"),
            order.getExternalTransactionNo(),
            order.getOrderNo(),
            null
        );

        return toRefundResponse(retryRefund);
    }

    @Transactional
    public void handleRefundCallback(RefundCallbackRequest request) {
        RefundOrder refund = requireRefundByNo(request.refundNo());
        PayOrder order = requirePayOrder(refund.getPayOrderId());
        String status = trimToDefault(request.status(), "succeeded");
        recordRefundNotifyLog(
            refund.getId(),
            "wechat_refund",
            trimToDefault(request.externalRefundNo(), refund.getExternalRefundNo()),
            status,
            trimToEmpty(request.payload())
        );

        if ("SUCCESS".equalsIgnoreCase(status) || "succeeded".equalsIgnoreCase(status)) {
            if ("succeeded".equals(refund.getStatus())) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            refund.setStatus("succeeded");
            refund.setExternalRefundNo(trimToDefault(request.externalRefundNo(), refund.getExternalRefundNo()));
            refund.setRefundedAt(now);
            refund.setUpdatedAt(now);
            refundOrderMapper.updateById(refund);

            order.setStatus("refunded");
            order.setRefundedAt(now);
            order.setUpdatedAt(now);
            payOrderMapper.updateById(order);
            return;
        }

        refund.setStatus("failed");
        refund.setExternalRefundNo(trimToDefault(request.externalRefundNo(), refund.getExternalRefundNo()));
        refund.setUpdatedAt(LocalDateTime.now());
        refundOrderMapper.updateById(refund);

        order.setStatus("paid");
        order.setUpdatedAt(LocalDateTime.now());
        payOrderMapper.updateById(order);
    }

    public List<ReconcileRecordResponse> listReconcileRecords(String channel) {
        return payReconcileRecordMapper.selectListByChannel(trimToNull(channel)).stream()
            .map(this::toReconcileResponse)
            .toList();
    }

    @Transactional
    public ReconcileRecordResponse createReconcileRecord(ReconcileCreateRequest request) {
        if ("wechat_pay".equals(trimToDefault(request.channel(), "wechat_pay")) && canUseRealReconcile()) {
            try {
                PayReconcileRecord generated = paymentReconcileService.buildTradeBillReconcileRecord(request.reconcileDate(), request.channel().trim());
                return saveReconcileRecord(generated);
            } catch (IllegalStateException ex) {
                // Fall back to manual reconcile record when merchant credentials are incomplete.
            }
        }

        PayReconcileRecord existing = payReconcileRecordMapper.selectByDateAndChannel(request.reconcileDate(), request.channel().trim());
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            existing = new PayReconcileRecord();
            existing.setReconcileDate(request.reconcileDate());
            existing.setChannel(request.channel().trim());
            existing.setCreatedAt(now);
        }
        existing.setReconcileStatus(request.reconcileStatus().trim());
        existing.setDiffCount(request.diffCount() == null ? 0 : request.diffCount());
        existing.setSummary(trimToEmpty(request.summary()));
        existing.setUpdatedAt(now);

        if (existing.getId() == null) {
            payReconcileRecordMapper.insert(existing);
        } else {
            payReconcileRecordMapper.updateById(existing);
        }
        return toReconcileResponse(existing);
    }

    public void validatePayCallbackMerchant(String orderNo, String merchantMchId) {
        PayOrder order = payOrderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new WechatCallbackException("order_not_found", "payment order not found", false);
        }
        if (trimToNull(merchantMchId) == null) {
            throw new WechatCallbackException("merchant_missing", "callback merchant mch id is missing", false);
        }
        if (!merchantMchId.trim().equals(trimToDefault(order.getMerchantMchId(), ""))) {
            throw new WechatCallbackException("merchant_mismatch", "callback merchant does not match payment order merchant", false);
        }
    }

    public void validateRefundCallbackMerchant(String refundNo, String merchantMchId) {
        RefundOrder refund = requireRefundByNo(refundNo);
        PayOrder order = requirePayOrder(refund.getPayOrderId());
        if (trimToNull(merchantMchId) == null) {
            throw new WechatCallbackException("merchant_missing", "callback merchant mch id is missing", false);
        }
        if (!merchantMchId.trim().equals(trimToDefault(order.getMerchantMchId(), ""))) {
            throw new WechatCallbackException("merchant_mismatch", "callback merchant does not match refund order merchant", false);
        }
    }

    public void recordPaymentCallbackFailure(String rawPayload, String category, String message) {
        PayNotifyLog log = new PayNotifyLog();
        log.setPayOrderId(null);
        log.setNotifyType("wechat_pay");
        log.setResourceId("");
        log.setNotifyStatus(trimToDefault(category, "callback_failed"));
        log.setRawPayload(trimToEmpty(rawPayload));
        log.setProcessResult(trimToDefault(message, "callback failed"));
        log.setNotifiedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        payNotifyLogMapper.insert(log);
    }

    public void recordRefundCallbackFailure(String rawPayload, String category, String message) {
        RefundNotifyLog log = new RefundNotifyLog();
        log.setRefundOrderId(null);
        log.setNotifyType("wechat_refund");
        log.setResourceId("");
        log.setNotifyStatus(trimToDefault(category, "callback_failed"));
        log.setRawPayload(trimToEmpty(rawPayload));
        log.setProcessResult(trimToDefault(message, "callback failed"));
        log.setNotifiedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        refundNotifyLogMapper.insert(log);
    }

    private PaymentAdminSummaryResponse toAdminSummary(PayOrder order) {
        MemberUser member = memberUserMapper.selectActiveById(order.getMemberId());
        WaybillOrder waybill = order.getWaybillId() == null ? null : waybillOrderMapper.selectActiveById(order.getWaybillId());
        return new PaymentAdminSummaryResponse(
            order.getId(),
            order.getOrderNo(),
            order.getMemberId(),
            order.getMerchantConfigId(),
            safe(order.getMerchantName()),
            safe(order.getMerchantMchId()),
            safe(order.getMerchantAppId()),
            member == null ? "" : safe(member.getPhone()),
            order.getWaybillId(),
            waybill == null ? "" : safe(waybill.getMainTrackingNo()),
            order.getBusinessType(),
            order.getSceneType(),
            order.getChannel(),
            order.getCurrency(),
            order.getAmountTotal(),
            order.getAmountPaid(),
            order.getStatus(),
            safe(order.getExternalTransactionNo()),
            formatDateTime(order.getPaidAt()),
            formatDateTime(order.getCreatedAt())
        );
    }

    private PaymentAdminDetailResponse toAdminDetail(PayOrder order) {
        MemberUser member = memberUserMapper.selectActiveById(order.getMemberId());
        WaybillOrder waybill = order.getWaybillId() == null ? null : waybillOrderMapper.selectActiveById(order.getWaybillId());
        return new PaymentAdminDetailResponse(
            order.getId(),
            order.getOrderNo(),
            order.getMemberId(),
            order.getMerchantConfigId(),
            safe(order.getMerchantName()),
            safe(order.getMerchantMchId()),
            safe(order.getMerchantAppId()),
            member == null ? "" : safe(member.getPhone()),
            member == null ? "" : safe(member.getNickname()),
            order.getWaybillId(),
            waybill == null ? "" : safe(waybill.getMainTrackingNo()),
            order.getBusinessType(),
            order.getSceneType(),
            order.getChannel(),
            order.getCurrency(),
            order.getAmountTotal(),
            order.getAmountPaid(),
            order.getStatus(),
            safe(order.getDescription()),
            safe(order.getExternalTransactionNo()),
            formatDateTime(order.getPaidAt()),
            formatDateTime(order.getExpiredAt()),
            formatDateTime(order.getClosedAt()),
            formatDateTime(order.getRefundedAt()),
            safe(order.getRemark()),
            formatDateTime(order.getCreatedAt()),
            formatDateTime(order.getUpdatedAt()),
            payTransactionMapper.selectByPayOrderId(order.getId()).stream().map(this::toTransactionResponse).toList(),
            refundOrderMapper.selectByPayOrderId(order.getId()).stream().map(this::toRefundResponse).toList(),
            payNotifyLogMapper.selectByPayOrderId(order.getId()).stream().map(this::toNotifyLogResponse).toList(),
            refundOrderMapper.selectByPayOrderId(order.getId()).stream()
                .flatMap(refund -> refundNotifyLogMapper.selectByRefundOrderId(refund.getId()).stream())
                .map(this::toRefundNotifyLogResponse)
                .toList()
        );
    }

    private MemberPayOrderSummaryResponse toMemberSummary(PayOrder order) {
        WaybillOrder waybill = order.getWaybillId() == null ? null : waybillOrderMapper.selectActiveById(order.getWaybillId());
        return new MemberPayOrderSummaryResponse(
            order.getId(),
            order.getOrderNo(),
            order.getMerchantConfigId(),
            safe(order.getMerchantName()),
            order.getWaybillId(),
            waybill == null ? "" : safe(waybill.getMainTrackingNo()),
            order.getChannel(),
            order.getAmountTotal(),
            order.getAmountPaid(),
            order.getStatus(),
            safe(order.getDescription()),
            formatDateTime(order.getPaidAt()),
            formatDateTime(order.getCreatedAt())
        );
    }

    private PaymentTransactionResponse toTransactionResponse(PayTransaction transaction) {
        return new PaymentTransactionResponse(
            transaction.getId(),
            transaction.getTransactionType(),
            transaction.getTransactionStatus(),
            safe(transaction.getExternalTransactionNo()),
            safe(transaction.getExternalOutTradeNo()),
            formatDateTime(transaction.getSuccessTime()),
            formatDateTime(transaction.getCreatedAt())
        );
    }

    private RefundOrderResponse toRefundResponse(RefundOrder refund) {
        return new RefundOrderResponse(
            refund.getId(),
            refund.getRefundNo(),
            refund.getAmountRefund(),
            refund.getStatus(),
            safe(refund.getReason()),
            safe(refund.getExternalRefundNo()),
            formatDateTime(refund.getRefundedAt()),
            formatDateTime(refund.getCreatedAt())
        );
    }

    private PaymentNotifyLogResponse toNotifyLogResponse(PayNotifyLog log) {
        return new PaymentNotifyLogResponse(
            log.getId(),
            log.getNotifyType(),
            safe(log.getResourceId()),
            log.getNotifyStatus(),
            safe(log.getProcessResult()),
            formatDateTime(log.getNotifiedAt()),
            formatDateTime(log.getCreatedAt())
        );
    }

    private RefundNotifyLogResponse toRefundNotifyLogResponse(RefundNotifyLog log) {
        return new RefundNotifyLogResponse(
            log.getId(),
            log.getNotifyType(),
            safe(log.getResourceId()),
            log.getNotifyStatus(),
            safe(log.getProcessResult()),
            formatDateTime(log.getNotifiedAt()),
            formatDateTime(log.getCreatedAt())
        );
    }

    private ReconcileRecordResponse toReconcileResponse(PayReconcileRecord record) {
        return new ReconcileRecordResponse(
            record.getId(),
            record.getReconcileDate() == null ? null : record.getReconcileDate().toString(),
            record.getChannel(),
            record.getReconcileStatus(),
            record.getDiffCount(),
            safe(record.getSummary()),
            formatDateTime(record.getCreatedAt()),
            formatDateTime(record.getUpdatedAt())
        );
    }

    private void createTransactionLog(
        Long payOrderId,
        String transactionType,
        String transactionStatus,
        String requestPayload,
        String responsePayload,
        String externalTransactionNo,
        String externalOutTradeNo,
        LocalDateTime successTime
    ) {
        PayTransaction transaction = new PayTransaction();
        transaction.setPayOrderId(payOrderId);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionStatus(transactionStatus);
        transaction.setRequestPayload(requestPayload);
        transaction.setResponsePayload(responsePayload);
        transaction.setExternalTransactionNo(trimToEmpty(externalTransactionNo));
        transaction.setExternalOutTradeNo(trimToEmpty(externalOutTradeNo));
        transaction.setSuccessTime(successTime);
        transaction.setCreatedAt(LocalDateTime.now());
        payTransactionMapper.insert(transaction);
    }

    private void recordNotifyLog(Long payOrderId, String notifyType, String resourceId, String notifyStatus, String rawPayload) {
        PayNotifyLog log = new PayNotifyLog();
        log.setPayOrderId(payOrderId);
        log.setNotifyType(notifyType);
        log.setResourceId(trimToEmpty(resourceId));
        log.setNotifyStatus(trimToDefault(notifyStatus, "received"));
        log.setRawPayload(rawPayload);
        log.setProcessResult("accepted");
        log.setNotifiedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        payNotifyLogMapper.insert(log);
    }

    private void recordRefundNotifyLog(Long refundOrderId, String notifyType, String resourceId, String notifyStatus, String rawPayload) {
        RefundNotifyLog log = new RefundNotifyLog();
        log.setRefundOrderId(refundOrderId);
        log.setNotifyType(notifyType);
        log.setResourceId(trimToEmpty(resourceId));
        log.setNotifyStatus(trimToDefault(notifyStatus, "received"));
        log.setRawPayload(rawPayload);
        log.setProcessResult("accepted");
        log.setNotifiedAt(LocalDateTime.now());
        log.setCreatedAt(LocalDateTime.now());
        refundNotifyLogMapper.insert(log);
    }

    private PayOrder requirePayOrder(Long id) {
        PayOrder order = payOrderMapper.selectActiveById(id);
        if (order == null) {
            throw new NotFoundException("payment order not found");
        }
        return order;
    }

    private RefundOrder requireRefundByNo(String refundNo) {
        RefundOrder refund = refundOrderMapper.selectByRefundNo(refundNo);
        if (refund != null) {
            return refund;
        }
        throw new NotFoundException("refund order not found");
    }

    private BigDecimal normalizeAmount(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("payment amount must be greater than 0");
        }
        return value.setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String generateOrderNo(LocalDateTime now) {
        return "PO" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + System.nanoTime() % 100000;
    }

    private String generateRefundNo(LocalDateTime now) {
        return "RF" + now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + System.nanoTime() % 100000;
    }

    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : DATE_TIME_FORMATTER.format(value);
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToEmpty(String value) {
        return value == null ? "" : value.trim();
    }

    private String trimToDefault(String value, String fallback) {
        String normalized = trimToNull(value);
        return normalized == null ? fallback : normalized;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private PayMerchantConfig resolveMerchantForAdminCreate(Long merchantConfigId) {
        if (merchantConfigId != null) {
            PayMerchantConfig config = paymentMerchantService.requireMerchantById(merchantConfigId);
            if (config.getEnabled() == null || config.getEnabled() != 1) {
                throw new IllegalArgumentException("selected payment merchant is disabled");
            }
            return config;
        }
        return paymentMerchantService.requireCurrentMerchant();
    }

    private void applyMerchantSnapshot(PayOrder order, PayMerchantConfig merchantConfig) {
        order.setMerchantConfigId(merchantConfig.getId());
        order.setMerchantName(trimToEmpty(merchantConfig.getMerchantName()));
        order.setMerchantMchId(trimToEmpty(merchantConfig.getMchId()));
        order.setMerchantAppId(trimToEmpty(merchantConfig.getAppId()));
    }

    private PayMerchantConfig requireMerchantForOrder(PayOrder order) {
        if (order.getMerchantConfigId() != null) {
            return paymentMerchantService.requireMerchantById(order.getMerchantConfigId());
        }
        return paymentMerchantService.requireCurrentMerchant();
    }

    private ReconcileRecordResponse saveReconcileRecord(PayReconcileRecord generated) {
        PayReconcileRecord existing = payReconcileRecordMapper.selectByDateAndChannel(generated.getReconcileDate(), generated.getChannel());
        LocalDateTime now = LocalDateTime.now();
        if (existing == null) {
            generated.setCreatedAt(now);
            if (generated.getUpdatedAt() == null) {
                generated.setUpdatedAt(now);
            }
            payReconcileRecordMapper.insert(generated);
            return toReconcileResponse(generated);
        }

        existing.setReconcileStatus(generated.getReconcileStatus());
        existing.setDiffCount(generated.getDiffCount());
        existing.setSummary(generated.getSummary());
        existing.setUpdatedAt(now);
        payReconcileRecordMapper.updateById(existing);
        return toReconcileResponse(existing);
    }

    private boolean canUseRealReconcile() {
        try {
            PayMerchantConfig merchant = paymentMerchantService.requireCurrentMerchant();
            return trimToNull(merchant.getMchId()) != null
                && trimToNull(merchant.getMerchantSerialNo()) != null
                && trimToNull(merchant.getPrivateKeyPath()) != null;
        } catch (Exception ex) {
            return false;
        }
    }
}
