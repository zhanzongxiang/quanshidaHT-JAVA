package com.qsd.admin.payment.controller;

import com.qsd.admin.common.ApiResponse;
import com.qsd.admin.payment.dto.PaymentAdminCreateRequest;
import com.qsd.admin.payment.dto.PaymentAdminDetailResponse;
import com.qsd.admin.payment.dto.PaymentAdminSummaryResponse;
import com.qsd.admin.payment.dto.NotifyReplayResponse;
import com.qsd.admin.payment.dto.PaymentOpsOverviewResponse;
import com.qsd.admin.payment.dto.PaymentStatusUpdateRequest;
import com.qsd.admin.payment.dto.ReconcileCreateRequest;
import com.qsd.admin.payment.dto.ReconcileDiffDetailResponse;
import com.qsd.admin.payment.dto.ReconcileRecordResponse;
import com.qsd.admin.payment.dto.RefundCreateRequest;
import com.qsd.admin.payment.dto.RefundOrderResponse;
import com.qsd.admin.payment.dto.MerchantCertificateStatusResponse;
import com.qsd.admin.payment.service.PaymentOpsService;
import com.qsd.admin.payment.service.PaymentNotifyReplayService;
import com.qsd.admin.payment.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/payments")
public class PaymentAdminController {
    private final PaymentService paymentService;
    private final PaymentOpsService paymentOpsService;
    private final PaymentNotifyReplayService paymentNotifyReplayService;

    public PaymentAdminController(
        PaymentService paymentService,
        PaymentOpsService paymentOpsService,
        PaymentNotifyReplayService paymentNotifyReplayService
    ) {
        this.paymentService = paymentService;
        this.paymentOpsService = paymentOpsService;
        this.paymentNotifyReplayService = paymentNotifyReplayService;
    }

    @GetMapping
    public ApiResponse<List<PaymentAdminSummaryResponse>> list(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String status,
        @RequestParam(required = false) String channel
    ) {
        return ApiResponse.ok(paymentService.listAdminPayOrders(keyword, status, channel));
    }

    @GetMapping("/{id}")
    public ApiResponse<PaymentAdminDetailResponse> getById(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.getAdminPayOrderDetail(id));
    }

    @PostMapping
    public ApiResponse<PaymentAdminDetailResponse> create(@Valid @RequestBody PaymentAdminCreateRequest request) {
        return ApiResponse.ok(paymentService.createAdminPayOrder(request));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<PaymentAdminDetailResponse> updateStatus(
        @PathVariable Long id,
        @Valid @RequestBody PaymentStatusUpdateRequest request
    ) {
        return ApiResponse.ok(paymentService.updateAdminPayOrderStatus(id, request));
    }

    @PostMapping("/{id}/refunds")
    public ApiResponse<RefundOrderResponse> createRefund(
        @PathVariable Long id,
        @Valid @RequestBody RefundCreateRequest request
    ) {
        return ApiResponse.ok(paymentService.createRefund(id, request));
    }

    @PostMapping("/refunds/{id}/retry")
    public ApiResponse<RefundOrderResponse> retryRefund(@PathVariable Long id) {
        return ApiResponse.ok(paymentService.retryRefund(id));
    }

    @GetMapping("/reconcile-records")
    public ApiResponse<List<ReconcileRecordResponse>> listReconcileRecords(@RequestParam(required = false) String channel) {
        return ApiResponse.ok(paymentService.listReconcileRecords(channel));
    }

    @GetMapping("/ops/overview")
    public ApiResponse<PaymentOpsOverviewResponse> getOpsOverview() {
        return ApiResponse.ok(paymentOpsService.getOverview());
    }

    @PostMapping("/ops/certificate-refresh")
    public ApiResponse<MerchantCertificateStatusResponse> refreshCertificate() {
        return ApiResponse.ok(paymentOpsService.refreshCurrentMerchantCertificate());
    }

    @GetMapping("/reconcile-records/{id}/diffs")
    public ApiResponse<ReconcileDiffDetailResponse> getReconcileDiffDetail(@PathVariable Long id) {
        return ApiResponse.ok(paymentOpsService.getReconcileDiffDetail(id));
    }

    @PostMapping("/notify-logs/{id}/replay")
    public ApiResponse<NotifyReplayResponse> replayPaymentNotify(@PathVariable Long id) {
        return ApiResponse.ok(paymentNotifyReplayService.replayPaymentNotify(id));
    }

    @PostMapping("/refund-notify-logs/{id}/replay")
    public ApiResponse<NotifyReplayResponse> replayRefundNotify(@PathVariable Long id) {
        return ApiResponse.ok(paymentNotifyReplayService.replayRefundNotify(id));
    }

    @PostMapping("/reconcile-records")
    public ApiResponse<ReconcileRecordResponse> createReconcileRecord(@Valid @RequestBody ReconcileCreateRequest request) {
        return ApiResponse.ok(paymentService.createReconcileRecord(request));
    }
}
